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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLTimeoutException;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DatabaseUnitTests {
    private Database database;
    @Mock protected Database.Listener listener;
    @Mock private DataSource dataSource;
    @Mock private Connection connection;
    @Mock private Statement statement;

    @BeforeEach public void setUp() throws Exception {
        database = new Database(dataSource)
                .withListener(listener);
        when(dataSource.getConnection())
                .thenReturn(connection);
    }

    @Test public void should_intercept_getConnection() throws Exception {
        when(connection.createStatement())
            .thenReturn(statement);
        database.setListener(new Database.Adapter(){
            @Override public void onGetConnection(Connection connection) throws SQLException {
                connection.setAutoCommit(false);
            }
        });

        executeQuery();

        verify(connection).setAutoCommit(false);
    }

    @Test public void should_report_login_timeout_exception() throws Exception {
        when(dataSource.getConnection())
                .thenThrow(new SQLTimeoutException("testing login timeout handling"));
        try {
            executeQuery();
            fail("Login timeout expected");
        } catch (SQLTimeoutException timeout) {
            verify(listener).onConnectionTimeout(timeout);
        } catch (SQLException error) {
            verify(listener, never()).onError(error);
        } catch (Exception anyOtherException) {
            fail("Login timeout expected but was " + anyOtherException.getClass());
        }
    }

    @Test public void should_report_connection_error() throws Exception {
        when(dataSource.getConnection())
                .thenThrow(new SQLException("testing connection error handling"));
        try {
            executeQuery();
            fail("Login error expected");
        } catch (SQLTimeoutException timeout) {
            verify(listener, never()).onConnectionTimeout(timeout);
        } catch (SQLException error) {
            verify(listener).onError(error);
        } catch (Exception anyOtherException) {
            fail("Login error expected but was " + anyOtherException.getClass());
        }
    }

    @Test public void should_report_query_timeout() throws Exception {
        when(connection.createStatement())
            .thenReturn(statement);
        when(statement.execute(anyString()))
                .thenThrow(new SQLTimeoutException("testing query timeout handling"));
        try {
            executeQuery();
            fail("Query timeout expected");
        } catch (SQLTimeoutException timeout) {
            verify(listener).onConnectionTimeout(timeout);
        } catch (SQLException error) {
            verify(listener, never()).onError(error);
        } catch (Exception anyOtherException) {
            fail("Query timeout expected but was " + anyOtherException.getClass());
        }
    }

    @Test public void should_report_query_error() throws Exception {
        when(connection.createStatement())
            .thenReturn(statement);
        when(statement.execute(anyString()))
                .thenThrow(new SQLException("testing query error handling"));
        try {
            executeQuery();
            fail("Query error expected");
        } catch (SQLTimeoutException timeout) {
            verify(listener, never()).onConnectionTimeout(timeout);
        } catch (SQLException error) {
            verify(listener).onError(error);
        } catch (Exception anyOtherException) {
            fail("Query error expected but was " + anyOtherException.getClass());
        }
    }

    private void executeQuery() throws SQLException {
        database.execute(connection -> {
            try (Statement statement = connection.createStatement()) {
                statement.execute("SELECT 1");
            }
            return null;
        });
    }
}
