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

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static kjss.lang.PreConditions.when;

/**
 * Stupid simple utility class for parsing <b>C</b>omma <b>S</b>eparated <b>V</b>alues files, a.k.a CSV files.
 *
 * <h3>Usage</h3>
 * <pre>
 * {@code
 * new CsvStream(fileToParse)
 *    .withSeparator(',')  // Optional, default ',' but can be anything else
 *    .withDelimiter('\"') // Optional, default '"' but can be anything else
 *    .noHeader()          // Optional, by default the first line is considered as header and give the name for each column
 *    .forEach(row -> {
 *       String stringValue = row.get("string column name");       // Retrieve raw string value from named column
 *       int intValue       = row.getInt("int column name");       // Retrieve native int value from named column
 *       int columnId       = 42;
 *       String moreString  = row.get(columnId);                   // Retrieve raw string value from indexed column
 *       UUID uuid          = row.getAs("uuid", UUID::fromString); // Retrieve an object according to the given type mapper
 *       // ... doing stuff with values ...
 *    });
 * }
 * </pre>
 *
 * @see CsvRow
 * @see CsvRow#get(String)
 * @see CsvRow#get(int)
 * @see CsvRow#getAs(String, java.util.function.Function)
 * @see CsvRow#getAs(int, java.util.function.Function)
 */
public class CsvStream {
    public static final char DEFAULT_FIELD_SEPARATOR = ',';
    public static final char DEFAULT_FIELD_DELIMITER = '\"';
    public static final Charset DEFAULT_CHARSET = Charset.defaultCharset();
    private Path csvFile;
    private String[] columns;
    private char fieldSeparator = DEFAULT_FIELD_SEPARATOR;
    private char fieldDelimiter = DEFAULT_FIELD_DELIMITER;
    private Charset charset = DEFAULT_CHARSET;
    private boolean parseHeader = true;

    public CsvStream(Path file) {
        csvFile = file;
    }

    /**
     * Parsing of a CSV file:
     * <pre>
     * new CsvStream(fileToParse)
     *    .forEach({@link CsvRow row} -&gt; {
     *       String stringValue = {@link CsvRow#get(String) row.get}("string column name");       // Retrieve raw string value from named column
     *       int intValue       = {@link CsvRow#getInt(String) row.getInt}("int column name");       // Retrieve native int value from named column
     *       int columnId       = 42;
     *       String moreString  = {@link CsvRow#get(int) row.get}(columnId);                   // Retrieve raw string value from indexed column
     *       UUID uuid          = {@link CsvRow#getAs(String, java.util.function.Function) row.getAs}("uuid", UUID::fromString); // Retrieve an object according to the given type mapper
     *       // ... doing stuff with values ...
     *    });
     * </pre>
     *
     * @param block Code that plays with {@link CsvRow rows}.
     * @throws IOException Error thrown when something went wrong with source file.
     */
    public void forEach(Consumer<CsvRow> block) throws IOException {
        AtomicBoolean firstLine = new AtomicBoolean(parseHeader);
        Files.readAllLines(csvFile, charset).forEach(line -> {
            if (line.isEmpty())
                return; //  Skip the empty lines
            if (firstLine.compareAndSet(true, false)) {
                columns = parse(line);
            } else {
                String[] split = parse(line);
                if (columns == null)
                    columns = new String[split.length];
                String[] values = new String[columns.length];
                when(split.length).isGreaterThan(columns.length)
                    .throwIllegalState("Too many values for the number of known columns");
                System.arraycopy(split, 0, values, 0, split.length);
                block.accept(new CsvRow(columns, values));
            }
        });
    }

    private String[] parse(String line) {
        List<String> fields = new ArrayList<>();
        int fieldStart = -1,
            fieldEnd = 0;
        boolean delimited = false;
        char[] chars = line.toCharArray();
        for (; fieldEnd < line.length(); fieldEnd++) {
            char c = line.charAt(fieldEnd);
            if (!delimited && fieldDelimiter == c) {
                delimited = true;
                fieldStart = fieldEnd;
            }
            else if ((delimited && fieldDelimiter == c) || (!delimited && fieldSeparator == c)) {
                int offset = fieldStart + 1;
                int count = fieldEnd - offset;
                fields.add(count > 0 ? new String(chars, offset, count) : null);
                if (delimited) fieldEnd++;
                fieldStart = fieldEnd;
                delimited = false;
            }
        }
        int offset = fieldStart + 1;
        int count = fieldEnd - offset;
        if (count > 0) fields.add(new String(chars, offset, count));
        return fields.toArray(new String[0]);
    }

    public CsvStream withSeparator(char separator) {
        setFieldSeparator(separator);
        return this;
    }

    public void setFieldSeparator(char fieldSeparator) {
        this.fieldSeparator = fieldSeparator;
    }

    public CsvStream withFieldDelimiter(char delimiter) {
        setFieldDelimiter(delimiter);
        return this;
    }

    public void setFieldDelimiter(char fieldDelimiter) {
        this.fieldDelimiter = fieldDelimiter;
    }

    public CsvStream withCharset(Charset charset) {
        setCharset(charset);
        return this;
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    public CsvStream noHeader() {
        setParseHeader(false);
        return this;
    }

    public void setParseHeader(boolean parseHeader) {
        this.parseHeader = parseHeader;
    }
}
