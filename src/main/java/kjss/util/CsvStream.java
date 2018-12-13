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

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.ObjIntConsumer;
import java.util.stream.Stream;

import static kjss.lang.PreConditions.when;

/**
 * Stupid simple utility class for parsing <b>C</b>omma <b>S</b>eparated <b>V</b>alues files, a.k.a CSV files.
 *
 * <h3>Usage</h3>
 * <pre>
 * {@code
 * new CsvStream(Files.lines(Paths.get(pathToFile)))
 *    .withSeparator(',')  // Optional, default ',' but can be anything else
 *    .withDelimiter('\"') // Optional, default '"' but can be anything else
 *    .noHeader()          // Optional, by default the first line is considered as header and give the name for each column
 *    .forEach((row, idx) -> {
 *       String stringValue = row.get("string column name").rawString(); // Retrieve raw string value from named column
 *       int intValue       = row.getInt("int column name").toInt();     // Retrieve native int value from named column
 *       int columnId       = 42;
 *       String moreString  = row.get(columnId).toString();              // Retrieve clean string value from indexed column
 *       UUID uuid          = row.get("uuid").as(UUID::fromString);      // Retrieve an object according to the given type mapper
 *       long timestamp     = row.get("timestamp").asLong(c -> c.isNull() ? Long.MIN_VALUE : Long.parseLong(c.toString()));
 *       // ... doing stuff with values ...
 *    });
 * }
 * </pre>
 */
public class CsvStream {
    public static final char DEFAULT_FIELD_SEPARATOR = ',';
    public static final char DEFAULT_FIELD_DELIMITER = '\"';
    private CsvRow header;
    char fieldSeparator = DEFAULT_FIELD_SEPARATOR;
    char fieldDelimiter = DEFAULT_FIELD_DELIMITER;
    private boolean parseHeader = true;
    private Stream<String> lines;

    public CsvStream(Stream<String> lines) {
        this.lines = lines;
    }

    public void forEach(ObjIntConsumer<CsvRow> block) {
        AtomicInteger rowIndex = new AtomicInteger(0);
        lines.forEach(line -> {
            if (line.isEmpty())
                return; //  Skip empty lines
            if (rowIndex.get() == 0 && parseHeader) {
                header = new CsvRow(this, 0, null).parse(line);
            } else {
                block.accept(new CsvRow(this, rowIndex.get(), header).parse(line), rowIndex.get());
            }
            rowIndex.incrementAndGet();
        });
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

    public CsvStream noHeader() {
        setParseHeader(false);
        return this;
    }

    public void setParseHeader(boolean parseHeader) {
        this.parseHeader = parseHeader;
    }
}
