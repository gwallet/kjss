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
package kjss.lang;

import org.junit.Test;

import java.util.Collections;

import static kjss.lang.CollectionSupport.isNullOrEmpty;
import static org.junit.Assert.assertTrue;

public class CollectionSupportUnitTests {
    @Test public void should_return_true_on_empty_collection() throws Exception {
        assertTrue(isNullOrEmpty(Collections.emptyList()));
        assertTrue(isNullOrEmpty(Collections.emptySet()));
        assertTrue(isNullOrEmpty(Collections.emptyMap()));
        assertTrue(isNullOrEmpty(new Object[0]));
    }
}
