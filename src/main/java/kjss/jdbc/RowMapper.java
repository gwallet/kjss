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

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * {@link Database#stream(String, RowMapper, Object...)} row mapping object.
 *
 * @param <T> the type of object extracted from the DB.
 */
@FunctionalInterface
public interface RowMapper<T> {

    /**
     * Transforms data from a {@link ResultSet} into an instance of type {@code T}.
     *
     * DO NOT MAKE CALL TO {@link ResultSet#next()} IN THIS METHOD.
     */
    T mapRow(ResultSet row) throws SQLException;

}
