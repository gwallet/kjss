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

import static java.util.function.Function.identity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class EitherUnitTests {
    @Test public void should_return_left() throws Exception {
        Either<String, Integer> either = Either.left("42");
        assertTrue(either.isLeft());
        assertThat(either.left()).isEqualTo("42");
        assertThat(either.map(identity(), integer -> integer.toString())).isEqualTo("42");
    }

    @Test public void should_return_right() throws Exception {
        Either<String, Integer> either = Either.right(42);
        assertTrue(either.isRight());
        assertThat(either.right()).isEqualTo(42);
        assertThat(either.map(identity(), integer -> integer.toString())).isEqualTo("42");
    }

    @Test(expected = IllegalStateException.class)
    public void should_throw_when_is_right_and_asking_for_left() throws Exception {
        Either<String, Integer> either = Either.right(42);
        assertFalse(either.isLeft());
        assertThat(either.left()).isEqualTo(42);
    }

    @Test(expected = IllegalStateException.class)
    public void should_throw_when_is_left_and_asking_for_right() throws Exception {
        Either<String, Integer> either = Either.left("42");
        assertFalse(either.isRight());
        assertThat(either.right()).isEqualTo("42");
    }
}
