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

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RangeOfLocalDateUnitTests {
    @Test public void out_of_finite_range() throws Exception {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(5);
        Range<LocalDate> range = Range.ofLocalDate()
                .lowInclusive(startDate)
                .highInclusive(endDate)
                .build();
        assertFalse(range.contains(startDate.minusDays(1)));
        assertFalse(range.contains(endDate.plusDays(1)));
    }

    @Test public void finite_range_inclusive_by_default() throws Exception {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(5);
        Range<LocalDate> range = Range.ofLocalDate()
                .lowInclusive(startDate)
                .highInclusive(endDate)
                .build();
        assertTrue(range.contains(startDate));
        assertTrue(range.contains(endDate));
    }

    @Test public void finite_range_exclusive() throws Exception {
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = startDate.plusDays(5);
        Range<LocalDate> range = Range.ofLocalDate()
                .lowExclusive(startDate)
                .highExclusive(endDate)
                .build();
        assertFalse(range.contains(startDate));
        assertFalse(range.contains(endDate));
    }

    @Test public void infinite_range() throws Exception {
        Range<LocalDate> range = Range.ofLocalDate().build();
        assertTrue(range.contains(LocalDate.now()));
    }

    @Test public void from_infinity_range() throws Exception {
        LocalDate date = LocalDate.now();
        Range<LocalDate> range = Range.ofLocalDate()
                .highInclusive(date)
                .build();
        assertTrue(range.contains(date));
    }

    @Test public void to_infinity_range() throws Exception {
        LocalDate date = LocalDate.now();
        Range<LocalDate> range = Range.ofLocalDate()
                .lowInclusive(date)
                .build();
        assertTrue(range.contains(date));
    }
}
