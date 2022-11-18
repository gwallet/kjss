/*
 *    Copyright 2023 Guillaume Wallet <wallet (dot) guillaume (at) gmail (dot) com>
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

package kjss.json;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonTest {

    @Test void should_interpret_Json_value_as_will() throws Exception {
        //  Given
        String string = "1955-11-05T01:35:07";
        LocalDateTime expectedDateTime = LocalDateTime.parse(string);
        Json.String stringValue = Json.String.of(string);

        //  When
        LocalDateTime actualDateTime = LocalDateTime.parse(stringValue.rawString());

        //  Then
        assertThat(actualDateTime)
            .isEqualTo(expectedDateTime);
    }

    @Test void should_iterate_over_document_fields() throws Exception {
        // Given
        Json.Object document = Json.Object.of(
            Json.Object.Member.of("true", Json.TRUE),
            Json.Object.Member.of("false", Json.FALSE),
            Json.Object.Member.of("null", Json.NULL),
            Json.Object.Member.of("number", Json.Number.of(42)),
            Json.Object.Member.of("string", Json.String.of("forty two")),
            Json.Object.Member.of("empty_document", Json.Object.of()),
            Json.Object.Member.of("empty_array", Json.Array.of())
        );

        {
            // When
            AtomicInteger counter = new AtomicInteger();
            document.forEach(field -> counter.incrementAndGet());

            // Then
            assertThat(counter)
                .hasValue(7);
        }
        {
            // When
            int counter = 0;
            for (Json.Object.Member field : document) {
                counter++;
            }

            // Then
            assertThat(counter)
                .isEqualTo(7);
        }
    }

    @Test void should_iterate_over_array_values() throws Exception {
        // Given
        Json.Array array = Json.Array.of(
            Json.TRUE,
            Json.FALSE,
            Json.NULL,
            Json.Number.of(42),
            Json.String.of("forty two"),
            Json.Object.of(),
            Json.Array.of()
        );

        {
            // When
            AtomicInteger counter = new AtomicInteger();
            array.forEach(value -> counter.incrementAndGet());

            // Then
            assertThat(counter)
                .hasValue(7);
        }
        {
            // When
            int counter = 0;
            for (Json.Value value : array) {
                counter++;
            }

            // Then
            assertThat(counter)
                .isEqualTo(7);
        }
    }

}
