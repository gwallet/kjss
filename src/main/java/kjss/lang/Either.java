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

import java.util.function.Function;

import static java.util.Objects.requireNonNull;

/**
 * Tired to not being able to return Apples XOR Bananas from some very specific method ?
 *
 * @param <A> Left hand type that can be shipped in this instance.
 * @param <B> Right hand type that can be shipped in this instance.
 */
public abstract class Either<A, B> {

    public static <L, R> Either<L, R> left(L l) {
        return new Left<>(requireNonNull(l));
    }

    public static <L, R> Either<L, R> right(R r) {
        return new Right<>(requireNonNull(r));
    }

    public boolean isLeft() {
        return false;
    }

    public A left() {
        throw new IllegalStateException("This is not a left side");
    }

    public boolean isRight() {
        return false;
    }

    public B right() {
        throw new IllegalStateException("This is not a right side");
    }

    public abstract Either<B, A> swap();

    public abstract <T> T map(Function<A, T> mapLeft, Function<B, T> mapRight);

    final static class Left<L, R> extends Either<L, R> {
        private final L value;
        Left(L l) {
            this.value = l;
        }
        public boolean isLeft() {
            return true;
        }
        public L left() {
            return value;
        }
        public Either<R, L> swap() {
            return right(value);
        }
        public <T> T map(Function<L, T> mapLeft, Function<R, T> mapRight) {
            return mapLeft.apply(value);
        }
    }

    final static class Right<L, R> extends Either<L, R> {
        private final R value;
        Right(R r) {
            this.value = r;
        }
        public boolean isRight() {
            return true;
        }
        public R right() {
            return value;
        }
        public Either<R, L> swap() {
            return left(value);
        }
        public <T> T map(Function<L, T> mapLeft, Function<R, T> mapRight) {
            return mapRight.apply(value);
        }
    }

    private Either(){}
}
