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

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.ToIntFunction;

import static kjss.lang.PreConditions.when;

/**
 * Stupid simple utility class for accessing values in a row.
 *
 * The default {@link #get(String)} (or {@link #get(int)}) retrieves the default raw data read from the file.
 * In addition, {@link #getInt(String)} (or {@link #getInt(int)}) retrieves the int value, 0 (zero) when null.
 * For anything else, {@link #getAs(String, Function)} (or {@link #getAs(int, Function)}) retrieves and map the value,
 * or returns null if none present.
 *
 * @see CsvStream
 */
public class CsvRow {
    private final String[] columns;
    private final String[] values;

    /* package */ CsvRow(String[] columns, String[] values) {
        when(columns.length).isLowerThan(values.length)
            .throwIllegalState("Can not build a CSV row with less columns than values, expected %d was %d", columns.length, values.length);
        when(columns.length).isGreaterThan(values.length)
            .throwIllegalState("Can not build a CSV row with more columns than values, expected %d was %d", columns.length, values.length);
        this.columns = columns;
        this.values = values;
    }

    public String[] columns() {
        return columns;
    }

    /**
     * @param columnName Name of the column we want the index.
     * @return Returns the index of the given column.
     *
     * @throws IllegalArgumentException The given <code>columnName</code> does not match any known columns in the current {@link CsvStream}.
     */
    public int indexOf(String columnName) {
        for (int i = 0; i < columns.length; i++) {
            if (columns[i].equals(columnName))
                return i;
        }
        throw new IllegalArgumentException("Unknown column " + columnName);
    }

    public boolean isNull(String columnName) {
        return isNull(indexOf(columnName));
    }

    public boolean isNull(int columnIndex) {
        return values[columnIndex] == null;
    }

    public @Nullable String get(String columnName) {
        return get(indexOf(columnName));
    }

    public @Nullable String get(int columnIndex) {
        return values[columnIndex];
    }

    public int getInt(String columnName) {
        return getInt(indexOf(columnName));
    }

    public int getInt(int columnIndex) {
        return getAsInt(columnIndex, Integer::parseInt);
    }

    public int getAsInt(String columnName, ToIntFunction<String> mapper) {
        return getAsInt(indexOf(columnName), mapper);
    }

    public int getAsInt(int columnIndex, ToIntFunction<String> mapper) {
        String raw = get(columnIndex);
        if (raw != null)
            return mapper.applyAsInt(raw);
        return 0;
    }

    public @Nullable <T> T getAs(String columnName, Function<String, T> mapper) {
        return getAs(indexOf(columnName), mapper);
    }

    public @Nullable <T> T getAs(int columnIndex, Function<String, T> mapper) {
        return Optional.ofNullable(get(columnIndex))
            .map(mapper)
            .orElse(null);
    }
}
