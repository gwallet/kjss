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

import javax.annotation.Nullable;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Objects;
import java.util.Optional;

import static kjss.lang.PreConditions.when;

/**
 * {@link Range} of element like {@code [ low .. high ]} where {@code low} and {@code high} are inclusives,
 * or {@code ( low .. high )} where {@code low} and {@code high} are exclusives, or any combinations in between.
 * <ul>
 *   <li>Use {@link Range#builder(Comparator)} to create unsupported {@link Range}s.</li>
 *   <li>Use {@link Range#builder()} to create {@link Range}s of {@link Comparable}s.</li>
 * </ul>
 *
 * @see Range#builder(Comparator)
 * @see Range#builder()
 * @param <T> Type of elements composing the {@link Range}.
 */
public final class Range<T> {

    public static final class Builder<U> {

        public Builder<U> lowInclusive(@Nullable U low) { return low(low, true); }
        public Builder<U> lowExclusive(@Nullable U low) { return low(low, false); }
        public Builder<U> low(@Nullable U low, boolean inclusive) {
            this.low = low;
            this.lowInclusive = inclusive;
            return this;
        }

        public Builder<U> highInclusive(@Nullable U high) { return high(high, true); }
        public Builder<U> highExclusive(@Nullable U high) { return high(high, false); }
        public Builder<U> high(@Nullable U high, boolean inclusive) {
            this.high = high;
            this.highInclusive = inclusive;
            return this;
        }

        public Range<U> build() { return new Range<>(low, lowInclusive, high, highInclusive, comparator); }

        private U low;
        private boolean lowInclusive;
        private U high;
        private boolean highInclusive;
        private Comparator<U> comparator;
        private Builder(Comparator<U> comparator) { this.comparator = comparator; }
    }

    private Boundary<T> low;
    private Boundary<T> high;

    public static <V> Range.Builder<V> builder(Comparator<V> comparator) {
        Objects.requireNonNull(comparator);
        return new Range.Builder<>(comparator);
    }

    public static <C extends Comparable<C>> Range.Builder<C> builder() {
        return builder(C::compareTo);
    }

    public static Range.Builder<LocalDate> ofLocalDate() {
        return builder(LocalDate::compareTo);
    }

    /**
     * Use {@link Range#builder(Comparator)} to create unsupported {@link Range}s.
     *
     * @see {@link Range#builder(Comparator)}
     * @see {@link Range#builder()}
     * @see {@link Range#ofLocalDate()}
     */
    private Range(@Nullable T low, boolean lowInclusive, @Nullable T high, boolean highInclusive, Comparator<T> comparator) {
        Objects.requireNonNull(comparator);
        this.low = Boundary.of(low, lowInclusive, comparator).orElse(Boundary.lowInfinity());
        this.high = Boundary.of(high, highInclusive, comparator).orElse(Boundary.highInfinity());
        when(this.low).isGreaterThan(this.high)
                .throwIllegalArgument("Low value (%s) can not be greater than high value (%s)", low, high);
    }

    public Boundary<T> lowerBoundary()  {
        return this.low;
    }

    public Boundary<T> higherBoundary()  {
        return this.high;
    }

    public boolean contains(T candidate) {
        Objects.requireNonNull(candidate);
        return low.lowerThan(candidate) && high.higherThan(candidate);
    }

    static public abstract class Boundary<U> implements Comparable<Boundary<U>> {
        abstract boolean higherThan(U u);
        abstract boolean lowerThan(U u);

        public abstract boolean isFinite();

        public boolean isInfinite() {
            return !isFinite();
        }

        static final <V> Boundary<V> lowInfinity() {
            return new LowerInfiniteBoundary<>();
        }
        static final <V> Boundary<V> highInfinity() {
            return new HigherInfiniteBoundary<>();
        }
        static final <V> Optional<Boundary<V>> of(@Nullable V v, boolean inclusive, Comparator<V> c) {
            return v == null
                    ? Optional.empty()
                    : inclusive
                            ? Optional.of(new InclusiveBoundary<>(v, c))
                            : Optional.of(new ExclusiveBoundary<>(v, c));
        }
    }

    static abstract class InfiniteBoundary<U> extends Boundary<U> {
        @Override public boolean isFinite() {
            return false;
        }
    }

    static final class LowerInfiniteBoundary<U> extends InfiniteBoundary<U> {
        @Override boolean higherThan(U u) {
            return false;
        }

        @Override boolean lowerThan(U u) {
            return true;
        }

        @Override public int compareTo(Boundary<U> o) {
            if (o instanceof LowerInfiniteBoundary)
                return 0;
            return -1;
        }
    }

    static final class HigherInfiniteBoundary<U> extends InfiniteBoundary<U> {
        @Override boolean higherThan(U u) {
            return true;
        }

        @Override boolean lowerThan(U u) {
            return false;
        }

        @Override public int compareTo(Boundary<U> o) {
            if (o instanceof HigherInfiniteBoundary)
                return 0;
            return 1;
        }
    }

    static abstract class FiniteBoundary<U> extends Boundary<U> {
        U value;
        Comparator<U> comparator;

        FiniteBoundary(U value, Comparator<U> comparator) {
            this.value = value;
            this.comparator = comparator;
        }

        @Override public boolean isFinite() {
            return true;
        }

        @Override public int compareTo(Boundary<U> o) {
            if (o instanceof FiniteBoundary) {
                return lowerThan(((FiniteBoundary<U>)o).value)
                        ? -1
                        : higherThan(((FiniteBoundary<U>)o).value)
                                ? 1
                                : 0;
            } else {
                return -1 * o.compareTo(this);
            }
        }
    }

    static final class InclusiveBoundary<U> extends FiniteBoundary<U> {
        public InclusiveBoundary(U value, Comparator<U> comparator) { super(value, comparator); }

        @Override boolean higherThan(U u) {
            return comparator.compare(u, value) <= 0;
        }

        @Override boolean lowerThan(U u) {
            return comparator.compare(value, u) <= 0;
        }
    }

    static final class ExclusiveBoundary<U> extends FiniteBoundary<U> {
        public ExclusiveBoundary(U value, Comparator<U> comparator) { super(value, comparator); }

        @Override boolean higherThan(U u) {
            return comparator.compare(u, value) < 0;
        }

        @Override boolean lowerThan(U u) {
            return comparator.compare(value, u) < 0;
        }
    }
}
