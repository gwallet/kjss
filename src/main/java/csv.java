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

import kjss.util.CsvCell;
import kjss.util.CsvStream;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class csv {
    static char separatorChar = CsvStream.DEFAULT_FIELD_SEPARATOR;
    static char delimiterChar = CsvStream.DEFAULT_FIELD_DELIMITER;
    static InputStream in = System.in;
    static Predicate<CsvCell> headerCellSelector = (c) -> true;
    static Predicate<CsvCell> cellSelector = (c) -> true;

    public static void main(String[] args) throws IOException {
        setup(args);
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(in, Charset.defaultCharset()))) {
            new CsvStream(bufferedReader.lines()).forEach((row, index) -> {
                if (index == 1) {
                    System.out.printf("%s\r\n", row.header().stream().filter(headerCellSelector).map(CsvCell::rawString).collect(Collectors.joining(",")));
                }
                System.out.printf("%s\r\n", row.stream().filter(cellSelector).map(CsvCell::rawString).collect(Collectors.joining(",")));
            });
        }
    }

    static void setup(String[] args) {
        for (int index = 0; index < args.length; index++) {
            switch (args[index]) {
                case "--separator":
                case "-c": {
                    separatorChar = args[++index].charAt(0);
                } break;
                case "--delimiter":
                case "-d": {
                    delimiterChar = args[++index].charAt(0);
                } break;
                case "--fields":
                case "-f": {
                    String val = args[++index];
                    headerCellSelector = parseHeaderSelection(val);
                    cellSelector = parseFieldSelection(val);
                } break;
                case "--help":
                case "-?": {
                    usage(0);
                } break;
                default: try {
                    in = new FileInputStream(Paths.get(args[index]).toFile());
                } catch (IOException cause) { throw new UncheckedIOException(cause); }
            }
        }
    }

    static void usage(int exitCode) {
        System.out.println("Usage: [ -c|--separator , ] [ -d|--delimiter \" ] [ -f|--fields field-spec[,field-spec]* ] [ file ]");
        System.exit(exitCode);
    }

    static Predicate<CsvCell> parseHeaderSelection(String string) {
        if (string.contains(""+separatorChar)) {
            String left  = string.substring(0, string.indexOf(separatorChar)),
                   right = string.substring(string.indexOf(separatorChar) + 1);
            return parseHeaderSelection(left).or(parseHeaderSelection(right));
        }
        else if (string.matches("\\w*")) {
            return c -> c.toString().equals(string);
        }
        else {
            return parseFieldSelection(string);
        }
    }

    static Predicate<CsvCell> parseFieldSelection(String string) {
        if (string.contains(""+separatorChar)) {
            String left  = string.substring(0, string.indexOf(separatorChar)),
                   right = string.substring(string.indexOf(separatorChar) + 1);
            return parseFieldSelection(left).or(parseFieldSelection(right));
        }
        else if (string.startsWith("^")) {
            return parseFieldSelection(string.substring(1)).negate();
        }
        else if (string.startsWith("-")) {
            return c -> c.index() <= Integer.parseInt(string.substring(1));
        }
        else if (string.endsWith("-")) {
            return c -> c.index() >= Integer.parseInt(string.substring(0, string.length() - 1));
        }
        else if (string.contains("-")) {
            return c -> c.index() >= Integer.parseInt(string.substring(0, string.indexOf('-')))
                     && c.index() <= Integer.parseInt(string.substring(string.indexOf('-') + 1));
        }
        else if (string.matches("\\d*")) {
            return c -> c.index() == Integer.parseInt(string);
        }
        else {
            return c -> c.header().toString().equals(string);
        }
    }
}
