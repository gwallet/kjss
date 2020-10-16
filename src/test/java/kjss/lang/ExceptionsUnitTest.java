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

import static kjss.lang.Exceptions.unchecked;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ExceptionsUnitTest {

    @Test public void should_wrap_exception_in_runtime() throws Exception {
        RuntimeException runtimeException = assertThrows(RuntimeException.class, () -> {
            throw unchecked(new CheckedException());
        });
        assertThat(runtimeException.getCause())
            .isInstanceOf(CheckedException.class);
    }

    @Test public void should_wrap_exception_from_Runnable_in_runtime() throws Exception {
        RuntimeException runtimeException = assertThrows(RuntimeException.class, () -> {
            Exceptions.unchecked(() -> {
                throw new CheckedException();
            });
        });
        assertThat(runtimeException.getCause())
            .isInstanceOf(CheckedException.class);
    }

    class CheckedException extends Exception {
        CheckedException() {
            super();
        }
    }
}
