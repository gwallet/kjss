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

import java.util.IllegalFormatException;
import java.util.function.Supplier;

import static java.lang.String.format;

/**
 * Pre conditions are responsible to throw (or not) exceptions based on runtime conditions.
 *
 * Use {@link #throwIllegalArgument(String, Object...)} or {@link #throwIllegalArgument(String)} to check input
 * arguments at the beginning of a method.
 *
 * Use {@link #throwIllegalState(String, Object...)} or {@link #throwIllegalState(String)} to check object or component
 * state at the beginning of a method.
 *
 * @see kjss.lang.PreConditions
 */
public abstract class PreCondition {
    /**
     * When something went wrong, throw an {@linkplain IllegalArgumentException} with the given message.
     *
     * @param message the message to be printed to humans about what just happened.
     */
    public abstract void throwIllegalArgument(String message);

    /**
     * When something went wrong, throw an {@linkplain IllegalArgumentException} with the given formatted message.
     *
     * @param format the message format to be printed to humans about what just happened.
     * @param args objects use to build the message.
     * @see String#format(String, Object...) for more details about formatted messages.
     */
    public abstract void throwIllegalArgument(String format, Object... args);

    /**
     * When something went wrong, throw an {@linkplain IllegalArgumentException} with the given formatted message.
     *
     * @param format the message format to be printed to humans about what just happened.
     * @param argsSupplier Provide the message argument when needed
     * @see String#format(String, Object...) for more details about formatted messages.
     */
    public abstract void throwIllegalArgument(String format, Supplier<Object[]> argsSupplier);

    /**
     * When something went wrong, throw an {@linkplain IllegalStateException} with the given message.
     *
     * @param message the message to be printed to humans about what just happened.
     */
    public abstract void throwIllegalState(String message);

    /**
     * When something went wrong, throw an {@linkplain IllegalStateException} with the given formatted message.
     *
     * @param format the message format to be printed to humans about what just happened.
     * @param args objects use to build the message.
     * @see String#format(String, Object...) for more details about formatted messages.
     */
    public abstract void throwIllegalState(String format, Object... args);

    /**
     * When something went wrong, throw an {@linkplain IllegalStateException} with the given formatted message.
     *
     * @param format the message format to be printed to humans about what just happened.
     * @param argsSupplier Provide the message argument when needed
     * @see String#format(String, Object...) for more details about formatted messages.
     */
    public abstract void throwIllegalState(String format, Supplier<Object[]> argsSupplier);

    /**
     * Combine this pre condition with a new one according to OR logical operator.
     *
     * @param somethingWentWrong Boolean expression representing an illegal condition, i.e. what is considered as NOT wanted at runtime.
     * @return Returns a new {@link PreCondition} that is the combination of both.
     */
    public abstract PreCondition or(boolean somethingWentWrong);

    /**
     * Combine this pre condition with a new one according to OR logical operator.
     *
     * @param precondition an other precondition.
     * @return Returns a new {@link PreCondition} that is the combination of both.
     */
    public abstract PreCondition or(PreCondition precondition);

    /* package */ static PreCondition whenNot(boolean expectedCondition) {
        return when(!expectedCondition);
    }

    /* package */ static PreCondition when(boolean somethingWentWrong) {
        return somethingWentWrong
                ? ILLEGAL_CONDITION
                : EXPECTED_CONDITION;
    }

    private static final PreCondition ILLEGAL_CONDITION = new IllegalCondition();
    private static final PreCondition EXPECTED_CONDITION = new ExpectedCondition();

    private static final class ExpectedCondition extends PreCondition {
        @Override public void throwIllegalArgument(String message) {}
        @Override public void throwIllegalArgument(String format, Object... args) {}
        @Override public void throwIllegalArgument(String format, Supplier<Object[]> argsSupplier) {}
        @Override public void throwIllegalState(String message) {}
        @Override public void throwIllegalState(String format, Object... args) {}
        @Override public void throwIllegalState(String format, Supplier<Object[]> argsSupplier) {}
        @Override public PreCondition or(boolean somethingWentWrong) {
            return when(somethingWentWrong);
        }
        @Override public PreCondition or(PreCondition precondition) {
            return precondition;
        }
    }

    private static final class IllegalCondition extends PreCondition {
        @Override public void throwIllegalArgument(String message) {
            throw new IllegalArgumentException(message);
        }
        @Override public void throwIllegalArgument(String format, Object... args) {
            throwIllegalArgument(safeFormat(format, args));
        }
        @Override public void throwIllegalArgument(String format, Supplier<Object[]> argsSupplier) {
            throwIllegalArgument(safeFormat(format, argsSupplier.get()));
        }
        private String safeFormat(String format, Object[] args) {
            try {
                return format(format, args);
            } catch (IllegalFormatException cause) {
                return format;
            }
        }
        @Override public void throwIllegalState(String message) {
            throw new IllegalStateException(message);
        }
        @Override public void throwIllegalState(String format, Object... args) {
            throwIllegalState(safeFormat(format, args));
        }
        @Override public void throwIllegalState(String format, Supplier<Object[]> argsSupplier) {
            throwIllegalState(safeFormat(format, argsSupplier.get()));
        }
        @Override public PreCondition or(boolean somethingWentWrong) {
            return this;
        }
        @Override public PreCondition or(PreCondition precondition) {
            return this;
        }
    }
}
