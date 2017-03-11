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
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;

/**
 * Handful class to simply query a database.
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
}
