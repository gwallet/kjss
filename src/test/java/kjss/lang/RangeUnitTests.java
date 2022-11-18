/*
 *    Copyright 2020 Guillaume Wallet <wallet (dot) guillaume (at) gmail (dot) com>
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

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RangeUnitTests {

    @Test
    void should_create_unbound_range_by_default() {
        /* ]-∞, +∞[ range */
        Range<Integer> range = Range.<Integer>builder()
                                    .build();
        assertTrue(range.lowerBoundary().isInfinite());
        assertTrue(range.higherBoundary().isInfinite());
    }

}
