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

import java.util.Collection;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static kjss.lang.PreConditions.when;

public class PreConditionsUnitTests {
    public static final Object NULL_OBJECT = null;
    public static final Object NOT_NULL_OBJECT = new Object();
    public static final String EMPTY_STRING = "";
    public static final String NOT_EMPTY_STRING = "NOT EMPTY STRING";
    public static final Collection<?> EMPTY_COLLECTION = emptyList();
    public static final Collection<?> NOT_EMPTY_COLLECTION = singletonList(NOT_NULL_OBJECT);
    public static final Object[] EMPTY_ARRAY = new Object[0];
    public static final Object[] NOT_EMPTY_ARRAY = new Object[] {NOT_NULL_OBJECT};
    public static final int MAGIC_NUMBER = 42;
    public static final String ERROR_MESSAGE = "message";

    @Rule public ExpectedException expectedException = ExpectedException.none();

    @Test public void should_throw_IllegalArgumentException_on_null_object() throws Exception {
        expectExceptionAndMessage(IllegalArgumentException.class, ERROR_MESSAGE);
        when(NULL_OBJECT).isNull().throwIllegalArgument(ERROR_MESSAGE);
    }

    @Test public void should_throw_IllegalArgumentException_on_empty_collection() throws Exception {
        expectExceptionAndMessage(IllegalArgumentException.class, ERROR_MESSAGE);
        when(EMPTY_COLLECTION).isEmpty().throwIllegalArgument(ERROR_MESSAGE);
    }

    @Test public void should_throw_IllegalArgumentException_on_empty_array() throws Exception {
        expectExceptionAndMessage(IllegalArgumentException.class, ERROR_MESSAGE);
        when(EMPTY_ARRAY).isEmpty().throwIllegalArgument(ERROR_MESSAGE);
    }

    @Test public void should_throw_IllegalArgumentException_on_empty_string() throws Exception {
        expectExceptionAndMessage(IllegalArgumentException.class, ERROR_MESSAGE);
        when(EMPTY_STRING).isEmpty().throwIllegalArgument(ERROR_MESSAGE);
    }

    @Test public void should_throw_IllegalArgumentException_on_too_big_numbers() throws Exception {
        expectExceptionAndMessage(IllegalArgumentException.class, ERROR_MESSAGE);
        when(MAGIC_NUMBER + 1).isGreaterThan(MAGIC_NUMBER).throwIllegalArgument(ERROR_MESSAGE);
    }

    @Test public void should_throw_IllegalArgumentException_on_edge_too_big_numbers() throws Exception {
        expectExceptionAndMessage(IllegalArgumentException.class, ERROR_MESSAGE);
        when(MAGIC_NUMBER).isGreaterThanOrEqualTo(MAGIC_NUMBER).throwIllegalArgument(ERROR_MESSAGE);
    }

    @Test public void should_throw_IllegalArgumentException_on_too_small_numbers() throws Exception {
        expectExceptionAndMessage(IllegalArgumentException.class, ERROR_MESSAGE);
        when(MAGIC_NUMBER).isLowerThan(MAGIC_NUMBER + 1).throwIllegalArgument(ERROR_MESSAGE);
    }

    @Test public void should_throw_IllegalArgumentException_on_edge_too_small_numbers() throws Exception {
        expectExceptionAndMessage(IllegalArgumentException.class, ERROR_MESSAGE);
        when(MAGIC_NUMBER).isLowerThanOrEqualTo(MAGIC_NUMBER).throwIllegalArgument(ERROR_MESSAGE);
    }

    @Test public void should_throw_IllegalStateException_on_enum_equals() throws Exception {
        expectExceptionAndMessage(IllegalStateException.class, ERROR_MESSAGE);
        when(Enum.This).isEqualTo(Enum.This).throwIllegalState(ERROR_MESSAGE);
    }

    @Test public void should_not_throw_IllegalStateException_on_enum_equals() throws Exception {
        when(Enum.That).isEqualTo(Enum.This).throwIllegalState(ERROR_MESSAGE);
    }

    enum Enum {
        This, That
    }

    @Test public void should_not_throw_anything() throws Exception {
        when(NOT_EMPTY_STRING).isEmpty()
                .or(when(NOT_NULL_OBJECT).isNull())
                .or(when(NOT_EMPTY_STRING).isEmpty())
                .or(when(MAGIC_NUMBER).isGreaterThan(MAGIC_NUMBER))
                .or(when(MAGIC_NUMBER).isEqualTo(MAGIC_NUMBER + 1))
                .or(when(NOT_EMPTY_COLLECTION).isEmpty())
                .or(when(NOT_EMPTY_ARRAY).isEmpty())
                .throwIllegalArgument(ERROR_MESSAGE);
    }

    private void expectExceptionAndMessage(Class<? extends Throwable> exceptionClass, String message) {
        expectedException.expect(exceptionClass);
        expectedException.expectMessage(message);
    }
}
