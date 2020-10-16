package kjss.util;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class CsvStreamTests {
    @Test public void should_distinct_random_lines() {
        int maxSize = 35000;
        int batchSize = 300;
        IntStream randomize = IntStream.generate(() -> (int) ( Math.random() * batchSize));
        Set<Integer> integers = new CsvStream(randomize.limit(maxSize)
                                                       .mapToObj(Integer::toString))
            .noHeader()
            .map(row -> Integer.parseInt(row.get(0).toString()))
            .collect(Collectors.toSet());
        assertEquals("Should get 300 integers", integers.size(), batchSize);
    }

    @Test public void should_distinct_sequential_lines() {
        int maxSize = 35000;
        int batchSize = 300;
        AtomicInteger count = new AtomicInteger(0);
        IntStream sequential = IntStream.generate(() -> count.getAndUpdate(c -> (c + 1) % batchSize));
        Set<Integer> integers = new CsvStream(sequential.limit(maxSize)
                                                        .mapToObj(Integer::toString))
            .noHeader()
            .map(row -> Integer.parseInt(row.get(0).toString()))
            .collect(Collectors.toSet());
        assertEquals("Should get 300 integers", integers.size(), batchSize);
    }

    @Test public void should_distinct_sequential_batch_lines() {
        int maxSize = 35000;
        int batchSize = 300;
        AtomicInteger count = new AtomicInteger(0);
        IntStream sequential = IntStream.generate(count::getAndIncrement)
                                        .map(c -> c * batchSize / maxSize);
        Set<Integer> integers = new CsvStream(sequential.limit(maxSize)
                                                        .mapToObj(Integer::toString))
            .noHeader()
            .map(row -> Integer.parseInt(row.get(0).toString()))
            .collect(Collectors.toSet());
        assertEquals("Should get 300 integers", integers.size(), batchSize);
    }

    @Test public void should_parse_csv_file() throws Exception {
        Map<String, String> expected = mapOf(
                "1", "one",
                "2", "two",
                "3", "three"
        );
        AtomicInteger count = new AtomicInteger(0);
        stream("simple.csv")
            .forEach((row, at) -> {
                assertArrayEquals("Should list 'id' and 'name' as columns, in that order", new String[] {"id", "name"}, row.columns());
                assertEquals("Should get the expected row content", expected.get(row.get("id").toString()), row.get("name").toString());
                assertEquals("Should get the expected row content", expected.get(row.get(0).toString()), row.get(1).toString());
                count.incrementAndGet();
            });
        assertEquals(3, count.intValue());
    }

    @Test public void should_skip_header() throws Exception {
        Map<Integer, String> expected = mapOf(
                0, "one",
                1, "two",
                2, "three"
        );
        AtomicInteger count = new AtomicInteger(0);
        stream("no_header.csv")
            .noHeader()
            .forEach((row, at) -> {
                assertEquals("Row index should match row 'id' value", row.get(0).toInt(), at);
                assertNull("Should not have header", row.header());
                assertEquals("Should get the expected row content", expected.get(row.get(0).toInt()), row.get(1).toString());
                count.incrementAndGet();
            });
        assertEquals(3, count.intValue());
    }

    @Test public void should_parse_scsv_file() throws Exception {
        Map<Integer, String> expected = mapOf(
            1, "one",
            2, "two",
            3, "three"
        );
        AtomicInteger count = new AtomicInteger(0);
        stream("simple.scsv")
            .withSeparator(';')
            .forEach((row, at) -> {
                assertArrayEquals("Should list 'id' and 'name' as columns, in that order", new String[] {"id", "name"}, row.columns());
                assertEquals("Should get the expected row content", expected.get(row.get("id").toInt()), row.get("name").toString());
                assertEquals("Should get the expected row content", expected.get(row.get(0).toInt()), row.get(1).toString());
                count.incrementAndGet();
            });
        assertEquals(3, count.intValue());
    }

    @Test public void should_parse_null_values() throws Exception {
        AtomicBoolean checked = new AtomicBoolean(false);
        stream("null_values.csv")
            .forEach((row, at) -> {
                if (at == 1)
                    assertTrue("Should read null 'id' at row " + at, row.get("id").isNull());
                if (at == 2)
                    assertTrue("Should read null 'first_name' at row " + at, row.get("first_name").isNull());
                if (at == 3)
                    assertTrue("Should read null 'last_name' at row " + at, row.get("last_name").isNull());
                checked.set(true);
            });
        assertTrue(checked.get());
    }

    @Test public void should_parse_escaped_separator() throws Exception {
        AtomicBoolean checked = new AtomicBoolean(false);
        stream("field_delimiter.csv")
                .forEach((row, at) -> {
                    assertEquals("Should get the expected row content", "Name, Email", row.get("col").toString());
                    checked.set(true);
                });
        assertTrue(checked.get());
    }

    @Test public void should_parse_custom_types() throws Exception {
        AtomicBoolean checked = new AtomicBoolean(false);
        UUID expected = UUID.fromString("AE012D8C-EA99-4944-9DBD-EA3E2E743FDE");
        stream("uuid.csv")
                .forEach((row, at) -> {
                    assertEquals("Should get the expected row content", expected, row.get("uuid").as(UUID::fromString));
                    checked.set(true);
                });
        assertTrue(checked.get());
    }

    @Test public void should_parse_not_all_delimited_rows() throws Exception {
        stream("not_all_delimited.csv").forEach((row, at) -> {
            assertFalse(row.get(0).isNull());
            assertFalse(row.get(1).isNull());
            assertFalse(row.get(2).isNull());
        });
    }

    @Test public void should_parse_escaped_delimiter() throws Exception {
        stream("escaped_delimiter.csv").forEach((row, at) -> {
            assertEquals("John \"Hannibal\" Smith", row.get("name").toString());
            assertEquals("Team leader", row.get("position").toString());
        });
    }

    private CsvStream stream(String testFile) throws IOException {
        Path path = Paths.get(System.getProperty("user.dir"), "src", "test", "resources", "kjss", "util", testFile);
        return new CsvStream(Files.lines(path, Charset.defaultCharset()));
    }

    private <K, V> Map<K, V> mapOf(K k1, V v1, K k2, V v2, K k3, V v3) {
        HashMap<K, V> map = new HashMap<>();
        map.put(k1, v1);
        map.put(k2, v2);
        map.put(k3, v3);
        return Collections.unmodifiableMap(map);
    }
}
