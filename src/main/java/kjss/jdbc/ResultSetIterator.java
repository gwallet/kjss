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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;

import static kjss.lang.Exceptions.unchecked;

class ResultSetIterator<T> implements Iterator<T> {

    private static final int DEFAULT_FETCH_SIZE = 1_000;

    private final int fetchSize;
    private final RowMapper<T> rowMapper;

    private PreparedStatement preparedStatement;
    private ResultSet resultSet;

    public ResultSetIterator(Connection connection, String sql, RowMapper<T> rowMapper, Object...params) throws SQLException {
        this(DEFAULT_FETCH_SIZE, connection, sql, rowMapper, params);
    }
    public ResultSetIterator(int fetchSize, Connection connection, String sql, RowMapper<T> rowMapper, Object...params) throws SQLException {
        this.fetchSize = fetchSize;
        this.rowMapper = rowMapper;
        init(connection, sql, params);
    }

    private void init(Connection connection, String sql, Object...params) throws SQLException {
        preparedStatement = connection.prepareStatement(sql);
        preparedStatement.setFetchSize(fetchSize);
        int index = 0;
        for (Object param : params) {
            try {
                preparedStatement.setObject(++index, param);
            } catch (SQLException cause) {
                close();
                throw cause;
            }
        }
    }

    @Override public boolean hasNext() {
        if (preparedStatement == null) return false;
        if (resultSet == null) {
            try {
                resultSet = preparedStatement.executeQuery();
            } catch (SQLException cause) {
                close();
                throw unchecked(cause);
            }
        }
        boolean hasNext;
        try {
            hasNext = resultSet.next();
        } catch (SQLException cause) {
            close();
            throw unchecked(cause);
        }
        if (!hasNext) {
            close();
        }
        return hasNext;
    }

    private void close() {
        if (resultSet != null)
            try { resultSet.close(); } catch (SQLException ignored) { /* NO OP */ }
        resultSet = null;
        if (preparedStatement != null)
            try { preparedStatement.close(); } catch (SQLException ignored) { /* NO OP */ }
        preparedStatement = null;
    }

    @Override public T next() {
        try {
            return rowMapper.mapRow(resultSet);
        } catch (SQLException cause) {
            close();
            throw unchecked(cause);
        }
    }
}
