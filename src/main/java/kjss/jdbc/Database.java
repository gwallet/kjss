/*
 *    Copyright 2017 Guillaume Wallet <wallet (dot) guillaume (at) gmail (dot) com>
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */
package kjss.jdbc;

import javax.annotation.Nullable;
import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Spliterators.spliteratorUnknownSize;

/**
 * Handful class to simply query a database.
 *
 * To load all data at once in memory, consider using {@link #execute(Query)}.
 *
 * For best performance and low memory consumption, consider using {@link #stream(String, RowMapper, Object...)}
 * or {@link #parallelStream(String, RowMapper, Object...)}.
 */
public class Database {
    /**
     * {@link Database} {@link Listener} used to intercept and deals with {@link SQLTimeoutException} and {@link SQLException}.
     */
    public interface Listener {
        /**
         * @param timeout Timeout error that occurs during connection to database (either login or query).
         */
        void onConnectionTimeout(SQLTimeoutException timeout);

        /**
         * @param error SQL error that occurs during connection to database (either login or query).
         */
        void onError(SQLException error);
    }

    private final DataSource dataSource;
    private Listener listener;

    /**
     * Best way to create instance of this class by providing a {@link javax.sql.DataSource DataSource}.
     * It's warmly recommended to use connection pooling utility like Tomcat JDBC Pool, Commons DBCP, C3PO, ...
     *
     * @param dataSource the {@link javax.sql.DataSource} responsible for the database {@link java.sql.Connection}.
     */
    public Database(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * @param listener the listener to set, when {@code null} the listening mechanism is disabled.
     */
    public void setListener(@Nullable Database.Listener listener) {
        this.listener = listener;
    }

    /**
     * @param listener the listener to set, when {@code null} the listening mechanism is disabled.
     * @return Return this {@link Database} instance.
     */
    public Database withListener(@Nullable Database.Listener listener) {
        setListener(listener);
        return this;
    }

    /**
     * Execute the given query on one database {@link java.sql.Connection connection} with full JDBC power.
     *
     * Example:
     * {@code
     *  List<DomainObject> domainObjectList = database.execute(connection -> {
     *    try (PreparedStatement preparedStatement = connection.prepareStatement("SELECT ... FROM ... WHERE ...")) {
     *      //  ...
     *      preparedStatement.setObject(parameterIndex, object);
     *      //  ...
     *      List<DomainObject> list = new ArrayList<>();
     *      try (ResultSet resultSet = preparedStatement.executeQuery()) {
     *        while (resultSet.next()) {
     *          DomainObject domainObject = new DomainObject();
     *          //  ...
     *          domainObject.setProperty(resultSet.getObject(columnIndex));
     *          //  ...
     *          list.add(domainObject);
     *        }
     *      }
     *      return list;
     *    }
     *  });
     * }
     *
     * @param <T> the type of object produced by the {@code query}.
     * @param query the query bloc used to produce data.
     * @return Return the result provided by the given query, {@code null} in case of {@code Query<Void>}.
     * @throws SQLException Propagate the underlying exception to the client.
     */
    public @Nullable <T> T execute(Query<T> query) throws SQLException {
        try (Connection connection = dataSource.getConnection()) {
            return query.executeOnConnection(connection);
        } catch (SQLTimeoutException timeout) {
            if (listener != null) listener.onConnectionTimeout(timeout);
            throw timeout;
        } catch (SQLException error) {
            if (listener != null) listener.onError(error);
            throw error;
        }
    }

    /**
     * Execute the given query and returns all results as a list.
     *
     * Example:
     * {@code
     *  List<DomainObject> domainObjectList = database.query("SELECT ... FROM ... WHERE ...", DomainObject::new, param1, param2);
     * }
     * @param <T> the type of object build by the {@code rowMapper}.
     * @param sql the SQL statement to use on DB.
     * @param rowMapper the row mapping strategy.
     * @param params optional list of parameters to use in the query.
     * @return Return the {@link List} of all element map to the query results, might be empty if no results are found.
     * @throws SQLException Propagate the underlying exception to the client.
     */
    public <T> List<T> query(String sql, RowMapper<T> rowMapper, Object...params) throws SQLException {
        return execute(connection -> {
            ArrayList<T> results = new ArrayList<>();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                int index = 0;
                for (Object param : params) {
                    statement.setObject(++index, param);
                }
                try (ResultSet resultSet = statement.executeQuery()) {
                    while (resultSet.next()) {
                        results.add(rowMapper.mapRow(resultSet));
                    }
                }
            }
            return results;
        });
    }

    /**
     * Execute and stream the result of the given {@code sql} statement. Each row is mapped before use thanks to the
     * given {@link RowMapper}.
     *
     * Example:
     * {@code
     *  database.stream("SELECT ... FROM ... WHERE ...", row -> this::map, param1, param2)
     *      .foreach(this::consume);
     * }
     * @param <T> the type of object build by the {@code rowMapper}.
     * @param sql the SQL statement to use on DB.
     * @param rowMapper the row mapping strategy.
     * @param params optional list of parameters to use in the query.
     * @return Return a stream of objects build by the {@code rowMapper} with data coming from the database.
     * @throws SQLException Propagate the underlying exception to the client.
     */
    public <T> Stream<T> stream(String sql, RowMapper<T> rowMapper, Object...params) throws SQLException {
        return _stream(false, sql, rowMapper, params);
    }

    /**
     * Execute and stream the result of the given {@code sql} statement in parallel. Each row is mapped before use
     * thanks to the given {@link RowMapper}.
     *
     * Example:
     * {@code
     *  database.parallelStream("SELECT ... FROM ... WHERE ...", row -> this::map, param1, param2)
     *      .foreach(this::consume);
     * }
     * @param <T> the type of object build by the {@code rowMapper}.
     * @param sql the SQL statement to use on DB.
     * @param params optional list of parameters to use in the query.
     * @param rowMapper the row mapping strategy.
     * @return Return a stream of objects build by the {@code rowMapper} with data coming from the database.
     * @throws SQLException Propagate the underlying exception to the client.
     */
    public <T> Stream<T> parallelStream(String sql, RowMapper<T> rowMapper, Object...params) throws SQLException {
        return _stream(true, sql, rowMapper, params);
    }

    private <T> Stream<T> _stream(boolean parallel, String sql, RowMapper<T> rowMapper, Object...params) throws SQLException {
        return StreamSupport.stream(spliteratorUnknownSize(new ResultSetIterator<>(dataSource.getConnection(), sql, rowMapper, params), 0), parallel);
    }
}
