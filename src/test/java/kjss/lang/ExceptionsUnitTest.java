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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static kjss.lang.Exceptions.unchecked;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.isA;

public class ExceptionsUnitTest {

    @Rule public ExpectedException expectedException = ExpectedException.none();

    @Test public void should_wrap_exception_in_runtime() throws Exception {
        expectedException.expect(RuntimeException.class);
        expectedException.expectCause(isA(CheckedException.class));
        die();
    }

    @Test public void should_wrap_exception_from_Runnable_in_runtime() throws Exception {
        expectedException.expect(RuntimeException.class);
        expectedException.expectCause(isA(CheckedException.class));
        Exceptions.unchecked(() -> {
            throw new CheckedException();
        });
    }

    private void die() {
        throw unchecked(new CheckedException());
    }

    class CheckedException extends Exception {
        CheckedException() {
            super();
        }
    }
}
