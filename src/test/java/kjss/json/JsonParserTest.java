/*
 *    Copyright 2022 Guillaume Wallet <wallet (dot) guillaume (at) gmail (dot) com>
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

import static org.assertj.core.api.Assertions.assertThat;

public class JsonParserTest {

    @Test void should_return_Json_NULL_when_empty_string() throws Exception {
        assertThat(new JsonParser("").parse())
            .isEqualTo(Json.NULL);
    }

    @Test void should_parse_Json_NULL() throws Exception {
        assertThat(new JsonParser("null").parse())
            .isEqualTo(Json.NULL);
    }

    @Test void should_parse_Json_Boolean() throws Exception {
        assertThat(new JsonParser("true").parse())
            .isEqualTo(Json.TRUE);
        assertThat(new JsonParser("false").parse())
            .isEqualTo(Json.FALSE);
    }

    @Test void should_parse_Json_String() throws Exception {
        assertThat(new JsonParser("\"\"").parse())
            .isEqualTo(Json.String.of(""));
        assertThat(new JsonParser("\"Hello, World!\"").parse())
            .isEqualTo(Json.String.of("Hello, World!"));
        assertThat(new JsonParser("\"Hello,\nWorld!\"").parse())
            .isEqualTo(Json.String.of("Hello,\nWorld!"));
    }

    @Test void should_parse_Json_Number() throws Exception {
        assertThat(new JsonParser("42").parse())
            .isEqualTo(Json.Number.of(42L));
        assertThat(new JsonParser("4.2").parse())
            .isEqualTo(Json.Number.of(4.2D));
        assertThat(new JsonParser("6.62607015E-34").parse())
            .isEqualTo(Json.Number.of(6.62607015E-34));
    }

    @Test void should_parse_Json_Array() throws Exception {
        assertThat(new JsonParser("[]").parse())
            .isEqualTo(Json.Array.of());
        assertThat(new JsonParser("[1, 2, 3]").parse())
            .isEqualTo(Json.Array.of(Json.Number.of(1L),
                                       Json.Number.of(2L),
                                       Json.Number.of(3L)));
    }

    @Test void should_parse_Json_Object() throws Exception {
        assertThat(new JsonParser("{}").parse())
            .isEqualTo(Json.Object.of());
        assertThat(new JsonParser("{\"Hello\": \"World!\"}").parse())
            .isEqualTo(Json.Object.of(Json.Object.Member.of(Json.String.of("Hello"), Json.String.of("World!"))));
    }

}
