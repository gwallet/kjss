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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class IntegerRangeUnitTests {
    @Test public void inclusive_finite_range() throws Exception {
        Range<Integer> range = Range.<Integer>builder()
                .lowInclusive(1)
                .highInclusive(3)
                .build();
        assertFalse (range.contains(0));
        assertTrue  (range.contains(1));
        assertTrue  (range.contains(2));
        assertTrue  (range.contains(3));
        assertFalse (range.contains(4));
    }

    @Test public void exclusive_finite_range() throws Exception {
        Range<Integer> range = Range.<Integer>builder()
                .lowExclusive(1)
                .highExclusive(3)
                .build();
        assertFalse(range.contains(1));
        assertTrue (range.contains(2));
        assertFalse(range.contains(3));
    }

    @Test public void infinite_ranges() throws Exception {
        assertTrue(Range.<Integer>builder().lowInclusive(1).build().contains(1));
        assertTrue(Range.<Integer>builder().highInclusive(2).build().contains(2));
        assertTrue(Range.<Integer>builder().build().contains(0));
    }

    @Test public void out_of_ranges() throws Exception {
        Range<Integer> finiteRange = Range.<Integer>builder()
                .lowInclusive(1)
                .highInclusive(2)
                .build();
        assertFalse(finiteRange.contains(0));
        assertFalse(finiteRange.contains(3));
        assertFalse(Range.<Integer>builder().lowInclusive(2).build().contains(1));
        assertFalse(Range.<Integer>builder().highInclusive(1).build().contains(2));
    }

//    @Test public void should_intersect_each_other() throws Exception {
//        Range<Integer> r1 = Range.<Integer>builder()
//                .lowInclusive(0)
//                .highInclusive(2)
//                .build();
//        Range<Integer> r2 = Range.<Integer>builder()
//                .lowInclusive(1)
//                .highInclusive(3)
//                .build();
//        Range<Integer> intersection = r1.inter(r2);  //  ??
//        Range.Boundary<Integer> low = intersection.lowerBoundary();
//        assertTrue(low.isFinite());
//        Range.Boundary<Integer> high = intersection.higherBoundary();
//        assertTrue(high.isFinite());
//    }
}
