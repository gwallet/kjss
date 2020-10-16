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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static kjss.lang.PreConditions.when;

public class CsvRow {
    private CsvStream stream;
    private CsvRow header;
    private CsvCell[] cells;
    private int number;

    /* package */ CsvRow(CsvStream stream, int number, CsvRow header) {
        this.stream = stream;
        this.number = number;
        this.header = header;
    }

    /* package */ CsvRow parse(String line) {
        List<CsvCell> cellList = new ArrayList<>();
        int start = 0,
            end = 0,
            index = 1;
        boolean escaped = false;
        for (; end < line.length(); end++) {
            char c = line.charAt(end);
            if (c == stream.fieldSeparator) {
                if (escaped) continue;
                if (start < end)
                    cellList.add(new CsvCell(this, index++, line.substring(start, end)));
                else
                    cellList.add(new CsvCell(this, index++, null));
                start = end + 1;
            }
            else if (c == stream.fieldDelimiter)
                if (end < line.length() - 1 && stream.fieldDelimiter == line.charAt(end + 1))
                    end++;
                else
                    escaped = !escaped;
        }
        if (start < end) 
            cellList.add(new CsvCell(this, index, line.substring(start, end)));
        else
            cellList.add(new CsvCell(this, index, null));
        cells = cellList.toArray(new CsvCell[0]);
        when(header != null && header.cells.length != cells.length)
            .throwIllegalState("Expects %d columns but %d was found on line %d%n%s", () -> Arrays.asList(header.cells.length, cells.length, number, line).toArray(new Object[0]));
        return this;
    }

    public CsvRow header() {
        return header;
    }

    public String[] columns() {
        return Arrays.stream(header.cells)
            .map(CsvCell::toString)
            .toArray(String[]::new);
    }

    public CsvCell[] cells() {
        return Arrays.copyOf(cells, cells.length);
    }

    public Stream<CsvCell> stream() {
        return Arrays.stream(cells);
    }

    /**
     * @param columnName Name of the column we want the index.
     * @return Returns the 1-based index of the given column.
     *
     * @throws IllegalArgumentException The given <code>columnName</code> does not match any known columns in the current {@link CsvStream}.
     *
     * @see #cellAtIndex(int)
     */
    public int indexOf(String columnName) {
        when(columnName).isNull()
            .throwIllegalArgument("Column name can not be null");
        when(header).isNull()
            .throwIllegalState("No header defined");
        for (int i = 0; i < header.cells.length; i++) {
            if (header.cells[i].toString().equals(columnName))
                return header.cells[i].index();
        }
        throw new IllegalArgumentException("Unknown column " + columnName);
    }

    public CsvCell get(String columnName) {
        return cellAtIndex(indexOf(columnName));
    }

    /**
     * @param cellIndex 1-based cell index.
     */
    public CsvCell cellAtIndex(int cellIndex) {
        return get(cellIndex - 1);
    }

    /**
     * @param columnIndex 0-based column index.
     */
    public CsvCell get(int columnIndex) {
        when(columnIndex).isLowerThan(0)
            .or(when(columnIndex).isGreaterThanOrEqualTo(cells.length))
            .throwIllegalArgument("No column at index " + columnIndex);
        return cells[columnIndex];
    }

}
