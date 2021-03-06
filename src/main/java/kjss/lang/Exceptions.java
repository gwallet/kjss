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

/**
 * Make some exceptions more quiet with {@link Exceptions#unchecked(Throwable)}.
 *
 * <h3>Usage</h3>
 * <pre>
 * public void methodNotSupposedToThrowAnyCheckedException() {
 *     try {
 *         // ... do something deadly dangerous ...
 *     } catch (Exception cause) {
 *         throw {@link Exceptions#unchecked(Throwable) unchecked}(cause);
 *     }
 * }
 * </pre>
 */
public class Exceptions {

    public static RuntimeException unchecked(Throwable error) throws RuntimeException {
        throw new RuntimeException(error);
    }

    public static <T> T unchecked(DangerousRunnable<T> code) throws RuntimeException {
        try {
            return code.execute();
        } catch (Throwable error) {
            throw new RuntimeException(error);
        }
    }

    @FunctionalInterface
    public interface DangerousRunnable<R> {
        R execute() throws Throwable;
    }
}
