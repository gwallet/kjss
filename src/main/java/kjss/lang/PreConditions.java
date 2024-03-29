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

import java.util.Collection;
import java.util.Optional;

/**
 * Armored Harm of defensive programming.
 *
 * <h3>Usage</h3>
 * <pre>
 * public void methodWithSensitiveArgs(String notNullNorEmptyString, int rangeValidIntValue) {
 *     //  First: check the input
 *     when(notNullNorEmptyString).isEmpty() // Wil also fail if it's null
 *         .throwIllegalArgument("Expected not null nor empty string as input, but was null or empty :'(");
 *     when(rangeValidInValue).isLowerThan(low)
 *         .or(when(rangeValidInValue).isGreaterThan(high))
 *         .throwIllegalState("Exceeded the range values, expected to be in [%d, %d] but was out :'(", low, high);
 * }
 * </pre>
 *
 * @see <a href="http://eclipsesource.com/blogs/2013/07/01/when-true-throwillegalargument-something-went-wrong/">
 *     when( true ).throwIllegalArgument( “something went wrong” );
 *     </a>
 */
public class PreConditions {

    /**
     * Pre condition entry point.
     *
     * @param somethingWentWrong Boolean expression representing an illegal condition, i.e. what is considered as NOT wanted at runtime.
     * @return Returns a new {@link PreCondition} ready to be checked.
     */
    public static PreCondition when(boolean somethingWentWrong) {
        return PreCondition.when(somethingWentWrong);
    }

    /**
     * Pre condition entry point.
     *
     * @param expectedCondition Boolean expression representing an expected condition, i.e. what is considered as wanted at runtime.
     * @return Returns a new {@link PreCondition} ready to be checked.
     */
    public static PreCondition whenNot(boolean expectedCondition) {
        return PreCondition.when(!expectedCondition);
    }

    /**
     * Pre condition entry point on {@code object}s.
     *
     * @param object the object to be checked.
     * @return Returns the {@link ObjectPreCondition} ready to be checked.
     */
    public static ObjectPreCondition when(Object object) {
        return new ObjectPreCondition(object);
    }

    public static class ObjectPreCondition {
        private Object object;

        ObjectPreCondition(Object object) {
            this.object = object;
        }

        public PreCondition isNull() {
            return PreCondition.when(object == null);
        }

        public PreCondition isNotNull() {
            return PreCondition.when(object != null);
        }

        public PreCondition isEqualTo(Object other) {
            return PreConditions.when(object).isNotNull()
                                .and(when(other).isNotNull())
                                .and(PreCondition.when(object.equals(other)));
        }

        public PreCondition isNotEqualTo(Object other) {
            return PreConditions.when(object).isNotNull()
                                .and(when(other).isNotNull())
                                .and(PreCondition.whenNot(object.equals(other)));
        }
    }

    /**
     * Pre condition entry point on {@code string}s.
     *
     * @param string the {@link String} to be checked.
     * @return Returns the {@link StringPreCondition} ready to be checked.
     */
    public static StringPreCondition when(String string) {
        return new StringPreCondition(string);
    }

    public static class StringPreCondition extends ObjectPreCondition {
        private final String string;

        StringPreCondition(String string) {
            super(string);
            this.string = string;
        }

        public PreCondition isEmpty() {
            return isNull().or(string.isEmpty());
        }
    }

    /**
     * Pre condition entry point on {@code collection}s.
     *
     * @param collection the {@link Collection} to be checked.
     * @return Returns the {@link CollectionPreCondition} ready to be checked.
     */
    public static CollectionPreCondition when(Collection<?> collection) {
        return new CollectionPreCondition(collection);
    }

    public static class CollectionPreCondition extends ObjectPreCondition {
        private final Collection<?> collection;

        CollectionPreCondition(Collection<?> collection) {
            super(collection);
            this.collection = collection;
        }

        public PreCondition isEmpty() {
            return isNull().or(collection.isEmpty());
        }

        public PreCondition contains(Object object) {
            return isNull().or(collection.contains(object));
        }

        public PreCondition sizeGreaterThan(int expectedSize) {
            return isNull().or(collection.size() > expectedSize);
        }

        public PreCondition sizeLowerThan(int expectedSize) {
            return isNull().or(collection.size() < expectedSize);
        }

        public PreCondition sizeDifferentThan(int expectedSize) {
            return isNull()
                .or(sizeGreaterThan(expectedSize))
                .or(sizeLowerThan(expectedSize));
        }
    }

    /**
     * Pre condition entry point on {@code array}s.
     *
     * @param <E> type of object inside the array.
     * @param array the array to be checked.
     * @return Returns the {@link ArrayPreCondition} ready to be checked.
     */
    public static <E> ArrayPreCondition when(E[] array) {
        return new ArrayPreCondition(array);
    }

    public static class ArrayPreCondition extends ObjectPreCondition {
        private Object[] array;

        public ArrayPreCondition(Object[] array) {
            super(array);
            this.array = array;
        }

        public PreCondition isEmpty() {
            return isNull().or(array.length == 0);
        }

        public PreCondition sizeLowerThan(int expectedSize) {
            return isNull().or(array.length < expectedSize);
        }

        public PreCondition sizeGreaterThan(int expectedSize) {
            return isNull().or(array.length > expectedSize);
        }

        public PreCondition sizeDifferentThan(int expectedSize) {
            return isNull()
                .or(sizeLowerThan(expectedSize))
                .or(sizeGreaterThan(expectedSize));
        }
    }

    /**
     * Pre condition entry point on {@code comparable}s.
     *
     * @param <C> the type of comparable.
     * @param comparable the comparable object to be checked.
     * @return Returns the {@link ComparablePreCondition} ready to be checked.
     */
    public static <C extends Comparable<C>> ComparablePreCondition<C> when(C comparable) {
        return new ComparablePreCondition<>(comparable);
    }

    public static class ComparablePreCondition<C extends Comparable<C>> extends ObjectPreCondition {
        private final C comparable;

        ComparablePreCondition(C comparable) {
            super(comparable);
            this.comparable = comparable;
        }

        public PreCondition isEqualTo(C other) {
            return isNull().or(comparable.compareTo(other) == 0);
        }

        public PreCondition isNotEqualTo(C other) {
            return isNull().or(comparable.compareTo(other) != 0);
        }

        public PreCondition isGreaterThan(C other) {
            return isNull().or(comparable.compareTo(other) > 0);
        }

        public PreCondition isGreaterThanOrEqualTo(C other) {
            return isNull().or(comparable.compareTo(other) >= 0);
        }

        public PreCondition isLowerThan(C other) {
            return isNull().or(comparable.compareTo(other) < 0);
        }

        public PreCondition isLowerThanOrEqualTo(C other) {
            return isNull().or(comparable.compareTo(other) <= 0);
        }
    }

    /**
     * Pre condition entry point on {@code long} values. Also works on {@code int} values.
     *
     * @param l the long value to be checked.
     * @return Returns the {@link LongPreCondition} ready to be checked.
     */
    public static LongPreCondition when(long l) {
        return new LongPreCondition(l);
    }

    public static class LongPreCondition {
        private final long value;

        LongPreCondition(long value) {
            this.value = value;
        }

        public PreCondition isEqualTo(long other) {
            return PreCondition.when(value == other);
        }

        public PreCondition isNotEqualTo(long other) {
            return PreCondition.when(value != other);
        }

        public PreCondition isGreaterThan(long other) {
            return PreCondition.when(value > other);
        }

        public PreCondition isGreaterThanOrEqualTo(long other) {
            return PreCondition.when(value >= other);
        }

        public PreCondition isLowerThan(long other) {
            return PreCondition.when(value < other);
        }

        public PreCondition isLowerThanOrEqualTo(long other) {
            return PreCondition.when(value <= other);
        }
    }

    /**
     * Pre condition entry point on {@code Optional} values.
     *
     * @param o the {@code Optional} value to be checked.
     * @return Returns the {@link }
     */
    public static <T> OptionalPreCondition<T> when(Optional<T> o) {
        return new OptionalPreCondition<>(o);
    }

    public static class OptionalPreCondition<T> extends ObjectPreCondition {

        private final Optional<T> value;

        public OptionalPreCondition(Optional<T> optional) {
            super(optional);
            this.value = optional;
        }

        public PreCondition isAbsent() { return isNotNull().and(PreConditions.whenNot(value.isPresent())); }

    }

    private PreConditions() {}
}
