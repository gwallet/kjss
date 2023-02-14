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

package kjss.http;

import kjss.http.Http.MediaType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class MediaTypeUnitTests {

    @Test void foo() throws Exception {
        // Given
        String mimeString = "application/json;charset=UTF-8";

        // When
        MediaType mediaType = MediaType.fromString(mimeString);

        // Then
        assertThat(mediaType)
            .hasFieldOrPropertyWithValue("type", "application")
            .hasFieldOrPropertyWithValue("subtype", "json")
            .hasFieldOrPropertyWithValue("parameterName", "charset")
            .hasFieldOrPropertyWithValue("parameterValue", "UTF-8");
    }

}
