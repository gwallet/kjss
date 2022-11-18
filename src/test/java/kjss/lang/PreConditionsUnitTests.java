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
import org.junit.jupiter.api.function.Executable;

import java.util.Collection;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static kjss.lang.PreConditions.when;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PreConditionsUnitTests {
    public static final Object NULL_OBJECT = null;
    public static final Object NOT_NULL_OBJECT = new Object();
    public static final Object OTHER_NOT_NULL_OBJECT = new Object();
    public static final String EMPTY_STRING = "";
    public static final String NOT_EMPTY_STRING = "NOT EMPTY STRING";
    public static final Collection<?> EMPTY_COLLECTION = emptyList();
    public static final Collection<?> NOT_EMPTY_COLLECTION = singletonList(NOT_NULL_OBJECT);
    public static final Object[] EMPTY_ARRAY = new Object[0];
    public static final Object[] NOT_EMPTY_ARRAY = new Object[] {NOT_NULL_OBJECT};
    public static final Optional<?> EMPTY_OPTIONAL = Optional.empty();
    public static final Optional<?> NOT_EMPTY_OPTIONAL = Optional.of(EMPTY_OPTIONAL);
    public static final int MAGIC_NUMBER = 42;
    public static final long BIG_MAGIC_NUMBER = ((long) Integer.MAX_VALUE) + 1;
    public static final String ERROR_MESSAGE = "message";

    @Test public void should_throw_IllegalArgumentException_on_null_object() throws Exception {
        expectExceptionAndMessage(IllegalArgumentException.class, ERROR_MESSAGE, () -> {
            when(NULL_OBJECT).isNull().throwIllegalArgument(ERROR_MESSAGE);
        });
    }

    @Test public void should_throw_IllegalArgumentException_on_non_null_object() throws Exception {
        expectExceptionAndMessage(IllegalArgumentException.class, ERROR_MESSAGE, () -> {
            when(NOT_NULL_OBJECT).isNotNull().throwIllegalArgument(ERROR_MESSAGE);
        });
    }

    @Test public void should_throw_IllegalArgumentException_on_non_equal_objects() throws Exception {
        expectExceptionAndMessage(IllegalArgumentException.class, ERROR_MESSAGE, () -> {
            when(NOT_NULL_OBJECT).isNotEqualTo(OTHER_NOT_NULL_OBJECT).throwIllegalArgument(ERROR_MESSAGE);
        });
    }

    @Test public void should_throw_IllegalArgumentException_on_equal_objects() throws Exception {
        expectExceptionAndMessage(IllegalArgumentException.class, ERROR_MESSAGE, () -> {
            when(NOT_NULL_OBJECT).isEqualTo(NOT_NULL_OBJECT).throwIllegalArgument(ERROR_MESSAGE);
        });
    }

    @Test public void should_throw_IllegalArgumentException_on_empty_collection() throws Exception {
        expectExceptionAndMessage(IllegalArgumentException.class, ERROR_MESSAGE, () -> {
            when(EMPTY_COLLECTION).isEmpty().throwIllegalArgument(ERROR_MESSAGE);
        });
    }

    @Test public void should_throw_IllegalArgumentException_on_empty_array() throws Exception {
        expectExceptionAndMessage(IllegalArgumentException.class, ERROR_MESSAGE, () -> {
            when(EMPTY_ARRAY).isEmpty().throwIllegalArgument(ERROR_MESSAGE);
        });
    }

    @Test public void should_throw_IllegalArgumentException_when_array_not_empty() throws Exception {
        expectExceptionAndMessage(IllegalArgumentException.class, ERROR_MESSAGE, () -> {
            when(NOT_EMPTY_ARRAY).sizeDifferentThan(0).throwIllegalArgument(ERROR_MESSAGE);
        });
    }

    @Test public void should_throw_IllegalArgumentException_on_empty_string() throws Exception {
        expectExceptionAndMessage(IllegalArgumentException.class, ERROR_MESSAGE, () -> {
            when(EMPTY_STRING).isEmpty().throwIllegalArgument(ERROR_MESSAGE);
        });
    }

    @Test public void should_throw_IllegalArgumentException_on_too_big_numbers() throws Exception {
        expectExceptionAndMessage(IllegalArgumentException.class, ERROR_MESSAGE, () -> {
            when(MAGIC_NUMBER + 1).isGreaterThan(MAGIC_NUMBER).throwIllegalArgument(ERROR_MESSAGE);
        });
    }

    @Test public void should_throw_IllegalArgumentException_on_edge_too_big_numbers() throws Exception {
        expectExceptionAndMessage(IllegalArgumentException.class, ERROR_MESSAGE, () -> {
            when(MAGIC_NUMBER).isGreaterThanOrEqualTo(MAGIC_NUMBER).throwIllegalArgument(ERROR_MESSAGE);
        });
    }

    @Test public void should_throw_IllegalArgumentException_on_too_small_numbers() throws Exception {
        expectExceptionAndMessage(IllegalArgumentException.class, ERROR_MESSAGE, () -> {
            when(MAGIC_NUMBER).isLowerThan(MAGIC_NUMBER + 1).throwIllegalArgument(ERROR_MESSAGE);
        });
    }

    @Test public void should_throw_IllegalArgumentException_on_edge_too_small_numbers() throws Exception {
        expectExceptionAndMessage(IllegalArgumentException.class, ERROR_MESSAGE, () -> {
            when(MAGIC_NUMBER).isLowerThanOrEqualTo(MAGIC_NUMBER).throwIllegalArgument(ERROR_MESSAGE);
        });
    }

    @Test public void should_throw_IllegalArgumentException_on_not_negative_big_numbers() throws Exception {
        expectExceptionAndMessage(IllegalArgumentException.class, ERROR_MESSAGE, () -> {
            when(BIG_MAGIC_NUMBER).isGreaterThan(0).throwIllegalArgument(ERROR_MESSAGE);
        });
    }

    @Test public void should_throw_IllegalArgumentException_on_absent_optional() throws Exception {
        expectExceptionAndMessage(IllegalArgumentException.class, ERROR_MESSAGE, () -> {
            when(EMPTY_OPTIONAL).isAbsent().throwIllegalArgument(ERROR_MESSAGE);
        });
    }

    @Test public void should_throw_IllegalStateException_on_enum_equals() throws Exception {
        expectExceptionAndMessage(IllegalStateException.class, ERROR_MESSAGE, () -> {
            when(Enum.This).isEqualTo(Enum.This).throwIllegalState(ERROR_MESSAGE);
        });
    }

    @Test public void should_throw_IllegalStateException_on_enum_not_equal() throws Exception {
        expectExceptionAndMessage(IllegalStateException.class, ERROR_MESSAGE, () -> {
            when(Enum.This).isNotEqualTo(Enum.That).throwIllegalState(ERROR_MESSAGE);
        });
    }

    @Test public void should_not_throw_IllegalStateException_on_enum_equals() throws Exception {
        when(Enum.That).isEqualTo(Enum.This).throwIllegalState(ERROR_MESSAGE);
    }

    @Test public void should_throw_IllegalStateException_on_collection_containing_object() throws Exception {
        expectExceptionAndMessage(IllegalStateException.class, ERROR_MESSAGE, () -> {
            when(NOT_EMPTY_COLLECTION).contains(NOT_NULL_OBJECT).throwIllegalState(ERROR_MESSAGE);
        });
    }

    @Test public void should_throw_IllegalStateException_on_collection_with_size_different_than_expected() throws Exception {
        expectExceptionAndMessage(IllegalStateException.class, ERROR_MESSAGE, () -> {
            when(NOT_EMPTY_COLLECTION).sizeDifferentThan(0).throwIllegalState(ERROR_MESSAGE);
        });
    }

    @Test public void should_not_throw_IllegalStateException_on_collection_not_containing_object() throws Exception {
        when(EMPTY_COLLECTION).contains(NOT_NULL_OBJECT).throwIllegalState(ERROR_MESSAGE);
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
                .or(when(NOT_EMPTY_OPTIONAL).isAbsent())
                .throwIllegalArgument(ERROR_MESSAGE);
    }

    private void expectExceptionAndMessage(Class<? extends Throwable> exceptionClass, String expectedMessage, Executable block) {
        Throwable actualThrowable = assertThrows(exceptionClass, block);
        assertEquals(actualThrowable.getMessage(), expectedMessage);
    }

}
