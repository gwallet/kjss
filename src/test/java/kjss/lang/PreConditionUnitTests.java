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

import static kjss.lang.PreCondition.when;
import static kjss.lang.PreCondition.whenNot;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PreConditionUnitTests {
    public static final boolean ILLEGAL_CONDITION = true;
    public static final boolean LEGAL_CONDITION = false;

    public static final String EXPECTED_FORMATTED_MESSAGE = "condition should be false but was true";
    public static final String ERROR_MESSAGE_FORMAT = "condition should be false but was %b";
    public static final String ERROR_MESSAGE = "message";

    @Test public void should_throw_IllegalArgumentException_with_message() throws Exception {
        expectExceptionAndMessage(IllegalArgumentException.class, ERROR_MESSAGE, () -> {
            when(ILLEGAL_CONDITION).throwIllegalArgument(ERROR_MESSAGE);
        });
    }

    @Test public void should_also_throw_IllegalArgumentException_with_message() throws Exception {
        expectExceptionAndMessage(IllegalArgumentException.class, ERROR_MESSAGE, () -> {
            whenNot(LEGAL_CONDITION).throwIllegalArgument(ERROR_MESSAGE);
        });
    }

    @Test public void should_throw_IllegalArgumentException_with_formatted_message() throws Exception {
        expectExceptionAndMessage(IllegalArgumentException.class, EXPECTED_FORMATTED_MESSAGE, () -> {
            boolean condition = ILLEGAL_CONDITION;
            when(condition).throwIllegalArgument(ERROR_MESSAGE_FORMAT, condition);
        });
    }

    @Test public void should_throw_IllegalArgumentException_with_bad_formatted_message() throws Exception {
        expectExceptionAndMessage(IllegalArgumentException.class, ERROR_MESSAGE_FORMAT, () -> {
            when(ILLEGAL_CONDITION).throwIllegalArgument(ERROR_MESSAGE_FORMAT, new Object[0]);
        });
    }

    @Test public void should_throw_IllegalStateException_with_message() throws Exception {
        expectExceptionAndMessage(IllegalStateException.class, ERROR_MESSAGE, () -> {
            when(ILLEGAL_CONDITION).throwIllegalState(ERROR_MESSAGE);
        });
    }

    @Test public void should_throw_IllegalStateException_with_formatted_message() throws Exception {
        expectExceptionAndMessage(IllegalStateException.class, EXPECTED_FORMATTED_MESSAGE, () -> {
            boolean condition = ILLEGAL_CONDITION;
            when(condition).throwIllegalState(ERROR_MESSAGE_FORMAT, condition);
        });
    }

    @Test public void should_throw_IllegalStateException_with_bad_formatted_message() throws Exception {
        expectExceptionAndMessage(IllegalStateException.class, ERROR_MESSAGE_FORMAT, () -> {
            when(ILLEGAL_CONDITION).throwIllegalState(ERROR_MESSAGE_FORMAT, new Object[0]);
        });
    }

    @Test public void should_not_throw_IllegalArgumentException() throws Exception {
        when(LEGAL_CONDITION).throwIllegalArgument(ERROR_MESSAGE);
        when(LEGAL_CONDITION).throwIllegalArgument(ERROR_MESSAGE_FORMAT, LEGAL_CONDITION);
    }

    @Test public void should_combine_conditions() throws Exception {
        expectExceptionAndMessage(IllegalArgumentException.class, ERROR_MESSAGE, () -> {
            when(LEGAL_CONDITION).or(ILLEGAL_CONDITION).throwIllegalArgument(ERROR_MESSAGE);
        });
    }

    @Test public void should_combine_conditions_inverse() throws Exception {
        expectExceptionAndMessage(IllegalArgumentException.class, ERROR_MESSAGE, () -> {
            when(ILLEGAL_CONDITION).or(LEGAL_CONDITION).throwIllegalArgument(ERROR_MESSAGE);
        });
    }

    @Test public void should_not_throw_IllegalStateException() throws Exception {
        when(LEGAL_CONDITION).throwIllegalState(ERROR_MESSAGE);
        when(LEGAL_CONDITION).throwIllegalState(ERROR_MESSAGE_FORMAT, LEGAL_CONDITION);
    }

    private void expectExceptionAndMessage(Class<? extends Throwable> exceptionClass, String expectedMessage, Executable block) {
        Throwable actualThrowable = assertThrows(exceptionClass, block);
        assertEquals(actualThrowable.getMessage(), expectedMessage);
    }

}
