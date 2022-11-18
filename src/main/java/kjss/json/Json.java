/*
 *    Copyright 2022 Guillaume Wallet <wallet (dot) guillaume (at) gmail (dot) com>
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

package kjss.json;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Stupid simple JSON implementation to minimise footprint.
 *
 * @see <a href="https://www.json.org/">Spec</a>
 */
public final class Json {

    public sealed interface Value {

        interface Visitor {

            void visit(Null nil);

            void visit(Boolean bool);

            void visit(String string);

            void visit(Number number);

            void visit(Array array);

            void visit(Object object);

        }

        default void accept(Visitor visitor) {
            switch (this) {
                //  @formatter:off
                case Null      nil -> visitor.visit(nil);
                case Boolean  bool -> visitor.visit(bool);
                case String string -> visitor.visit(string);
                case Number number -> visitor.visit(number);
                case Array   array -> visitor.visit(array);
                case Object object -> visitor.visit(object);
                //  @formatter:on
            }
        }

    }

    public static final Json.Null NULL = new Json.Null();

    public static final class Null implements Value {

        @Override public boolean equals(java.lang.Object obj) {
            return obj instanceof Null;
        }

        @Override public int hashCode() {
            return getClass().hashCode();
        }

        @Override public java.lang.String toString() {
            return "null";
        }

    }

    public static final Json.Boolean TRUE = new Boolean(true);

    public static final Json.Boolean FALSE = new Boolean(false);

    public static final class Boolean implements Value {

        private final boolean jBool;

        public static Boolean of(boolean jBool) {
            return jBool
                ? TRUE
                : FALSE;
        }

        @Override public int hashCode() {
            return Objects.hashCode(jBool);
        }

        @Override public boolean equals(java.lang.Object obj) {
            if (obj instanceof Boolean b) {
                return Objects.equals(jBool, b.jBool);
            }
            return false;

        }

        @Override public java.lang.String toString() {
            return "" + jBool;
        }

        private Boolean(boolean jBool) {
            this.jBool = jBool;
        }

    }

    public static final class String implements Value {

        private final java.lang.String jString;

        public static String of(java.lang.String jString) {
            return new String(jString);
        }

        public java.lang.String rawString() {
            return jString;
        }

        @Override public int hashCode() {
            return Objects.hashCode(jString);
        }

        @Override public boolean equals(java.lang.Object obj) {
            if (obj instanceof String s) {
                return Objects.equals(jString, s.jString);
            }
            return false;

        }

        @Override public java.lang.String toString() {
            return "\"" + jString + "\"";
        }

        private String(java.lang.String jString) {
            this.jString = jString;
        }

    }

    public static final class Number implements Value {

        private final java.lang.Number jNum;

        public static Number of(java.lang.Number jNum) {
            return new Number(jNum);
        }

        @Override public int hashCode() {
            return Objects.hashCode(jNum);
        }

        @Override public boolean equals(java.lang.Object obj) {
            if (obj instanceof Number n) {
                return Objects.equals(jNum, n.jNum);
            }
            return false;

        }

        @Override public java.lang.String toString() {
            return "" + jNum;
        }

        private Number(java.lang.Number jNum) {
            this.jNum = jNum;
        }

    }

    public static final class Array implements Value, Iterable<Json.Value> {

        private final Json.Value[] values;

        public static Array of(Json.Value... values) {
            return new Array(values);
        }

        public static Array of(Iterable<Json.Value> values) {
            return of(StreamSupport.stream(values.spliterator(), false).toArray(Json.Value[]::new));
        }

        @Override public Iterator<Value> iterator() {
            return Arrays.asList(values).iterator();
        }

        @Override public Spliterator<Value> spliterator() {
            return Arrays.spliterator(values);
        }

        @Override public int hashCode() {
            return Objects.hashCode(values);
        }

        @Override public boolean equals(java.lang.Object obj) {
            if (obj instanceof Array a) {
                return Arrays.equals(values, a.values);
            }
            return false;

        }

        @Override public java.lang.String toString() {
            return Stream.of(values)
                         .map(Json.Value::toString)
                         .collect(Collectors.joining(", ", "[", "]"));
        }

        private Array(Value... values) {
            this.values = values;
        }

    }

    public static final class Object implements Value, Iterable<Object.Member> {

        public record Member (String string, Value element) {

            public static Member of(java.lang.String jString, Value element) {
                return of(Json.String.of(jString), element);
            }

            public static Member of(String string, Value element) {
                return new Member(string, element);
            }

            @Override public java.lang.String toString() {
                return "%s: %s".formatted(string.toString(), element.toString());
            }

        }

        private final Member[] members;

        public static Object of(Member... members) {
            return new Object(members);
        }

        public static Object of(Iterable<Member> members) {
            return of(StreamSupport.stream(members.spliterator(), false).toArray(Member[]::new));
        }

        @Override public Spliterator<Member> spliterator() {
            return Arrays.asList(members).spliterator();
        }

        @Override public Iterator<Member> iterator() {
            return Arrays.asList(members).iterator();
        }

        @Override public int hashCode() {
            return Objects.hashCode(members);
        }

        @Override public boolean equals(java.lang.Object obj) {
            if (obj instanceof Object o) {
                return Arrays.equals(members, o.members);
            }
            return false;

        }

        @Override public java.lang.String toString() {
            return Arrays.stream(members)
                          .map(Member::toString)
                          .collect(Collectors.joining(", ", "{", "}"));
        }

        private Object(Member... members) {
            this.members = members;
        }

    }

    private Json() { /* 🙂 */ }

}
