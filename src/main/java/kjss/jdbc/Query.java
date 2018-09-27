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

import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * {@link Database} execution bloc.
 *
 * @param <T> the type of object extracted from the DB.
 */
@FunctionalInterface
public interface Query<T> {
    /**
     * @param connection the connection use to communicate with the DB.
     * @return Return any object extracted from DB, {@code null} when of type {@link java.lang.Void}.
     * @throws SQLException the underlying exception thrown during the DB communication.
     */
    @Nullable T executeOnConnection(Connection connection) throws SQLException;
}
