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

import com.ninja_squad.dbsetup.DbSetup;
import com.ninja_squad.dbsetup.destination.DataSourceDestination;
import org.h2.jdbcx.JdbcConnectionPool;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Statement;
import java.util.List;

import static com.ninja_squad.dbsetup.Operations.*;
import static org.assertj.core.api.Assertions.assertThat;

public class DatabaseIntegrationTests {
    private Database database;

    @Before public void setUp() throws Exception {
        DataSource datasource = datasource();
        database = new Database(datasource);
        new DbSetup(new DataSourceDestination(datasource),
            sequenceOf(
                sql("CREATE TABLE IF NOT EXISTS city (name TEXT, country TEXT, country_code TEXT)"),
                truncate("city"),
                insertInto("city")
                    .columns("name", "country", "country_code")
                    .values("Lille", "France", "FR")
                    .values("Marseille", "France", "FR")
                    .values("Paris", "France", "FR")
                    .values("New-York", "United States of America", "US")
                    .values("London", "England", "UK")
                    .build()
            )
        ).launch();
    }

    private DataSource datasource() {
        return JdbcConnectionPool.create("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", "user", "password");
    }

    @Test public void should_execute_query() throws Exception {
        database.execute(connection -> {
            try (Statement statement = connection.createStatement()) {
                statement.execute("SELECT 1");
            }
            return null;
        });
    }

    @Test public void should_query_database() throws Exception {
        List<Integer> results = database.query("SELECT 1", row -> row.getInt(1));
        assertThat(results).isNotEmpty();
        assertThat(results).containsExactly(1);
    }

    @Test public void should_stream_query() throws Exception {
        database.stream("SELECT name FROM city WHERE country_code = ?", row -> row.getString("name"), "FR")
            .forEach(System.out::println);
    }

    @Test public void should_parallel_stream_query() throws Exception {
        database.parallelStream("SELECT name FROM city WHERE country_code = ?", row -> row.getString("name"), "FR")
            .forEach(System.out::println);
    }
}
