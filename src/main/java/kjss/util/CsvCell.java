/*
 *    Copyright 2018 Guillaume Wallet <wallet (dot) guillaume (at) gmail (dot) com>
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

package kjss.util;

import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.*;

public class CsvCell {
    private CsvRow row;
    private int index;
    private String rawString;

    /* package */ CsvCell(CsvRow row, int index, @Nullable String rawString) {
        this.row = row;
        this.index = index;
        this.rawString = rawString;
    }

    public int index() {
        return index;
    }

    public CsvCell header() {
        return row.header().get(index);
    }

    public @Nullable String rawString() {
        return rawString;
    }

    @Override public String toString() {
        if (isNull()) return "";
        if (rawString.startsWith("\""))
            return rawString.substring(1, rawString.length() - 1)
                    .replaceAll("\"\"", "\"");
        return rawString.replaceAll("\"\"", "\"");
    }

    public boolean isNull() {
        return rawString == null;
    }

    public int toInt() {
        if (isNull()) return 0;
        return asInt(Integer::parseInt);
    }

    public int asInt(ToIntFunction<String> mapper) {
        return mapper.applyAsInt(rawString);
    }

    public long toLong() {
        if (isNull()) return 0;
        return asLong(Long::parseLong);
    }

    public long asLong(ToLongFunction<String> mapper) {
        return mapper.applyAsLong(rawString);
    }

    public double toDouble() {
        if (isNull()) return 0;
        return asDouble(Double::parseDouble);
    }

    public double asDouble(ToDoubleFunction<String> mapper) {
        return mapper.applyAsDouble(rawString);
    }

    public boolean toBoolean() {
        return asBoolean(Boolean::parseBoolean);
    }

    public boolean asBoolean(Predicate<String> mapper) {
        return mapper.test(rawString);
    }

    public <T> @Nullable T as(Function<String, T> mapper) {
        return as(mapper, (T) null);
    }

    private <T> T as(Function<String, T> mapper, T defaultValue) {
        return Optional.ofNullable(rawString)
            .map(mapper)
            .orElse(defaultValue);
    }
}
