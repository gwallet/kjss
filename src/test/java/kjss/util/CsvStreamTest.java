package kjss.util;

import org.junit.Test;

import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class CsvStreamTest {
    @Test
    public void should_parse_csv_file() throws Exception {
        Map<String, String> expected = mapOf(
                "1", "one",
                "2", "two",
                "3", "three"
        );
        AtomicInteger count = new AtomicInteger(0);
        stream("simple.csv")
            .forEach(row -> {
                assertArrayEquals("Should list 'id' and 'name' as columns, in that order", new String[] {"id", "name"}, row.columns());
                assertEquals("Should get the expected row content", expected.get(row.get("id")), row.get("name"));
                assertEquals("Should get the expected row content", expected.get(row.get(0)), row.get(1));
                count.incrementAndGet();
            });
        assertEquals(3, count.intValue());
    }

    @Test
    public void should_skip_header() throws Exception {
        Map<Integer, String> expected = mapOf(
                1, "one",
                2, "two",
                3, "three"
        );
        AtomicInteger count = new AtomicInteger(0);
        stream("no_header.csv")
            .noHeader()
            .forEach(row -> {
                assertArrayEquals("Should list 'null' and 'null' as columns", new String[] {null, null}, row.columns());
                assertEquals("Should get the expected row content", expected.get(row.getInt(0)), row.get(1));
                count.incrementAndGet();
            });
        assertEquals(3, count.intValue());
    }

    @Test
    public void should_parse_scsv_file() throws Exception {
        Map<Integer, String> expected = mapOf(
            1, "one",
            2, "two",
            3, "three"
        );
        AtomicInteger count = new AtomicInteger(0);
        stream("simple.scsv")
            .withSeparator(';')
            .forEach(row -> {
                assertArrayEquals("Should list 'id' and 'name' as columns, in that order", new String[] {"id", "name"}, row.columns((col) -> true));
                assertEquals("Should get the expected row content", expected.get(row.getInt("id")), row.get("name"));
                assertEquals("Should get the expected row content", expected.get(row.getInt(0)), row.get(1));
                count.incrementAndGet();
            });
        assertEquals(3, count.intValue());
    }

    @Test
    public void should_parse_null_values() throws Exception {
        AtomicBoolean checked = new AtomicBoolean(false);
        stream("null_values.csv")
            .forEach(row -> {
                if ("first".equals(row.get("first_name")) && "last".equals(row.get("last_name")))
                    assertTrue("Should read null 'id'", row.isNull("id"));
                if ("2".equals(row.get("id")))
                    assertTrue("Should read null 'first_name'", row.isNull(1));
                if ("3".equals(row.get("id")))
                    assertTrue("Should read null 'last_name'", row.isNull("last_name"));
                checked.set(true);
            });
        assertTrue(checked.get());
    }

    @Test
    public void should_parse_escaped_separator() throws Exception {
        AtomicBoolean checked = new AtomicBoolean(false);
        stream("field_delimiter.csv")
                .forEach(row -> {
                    assertEquals("Should get the expected row content", "Name, Email", row.get("col"));
                    checked.set(true);
                });
        assertTrue(checked.get());
    }

    @Test
    public void should_parse_custom_types() throws Exception {
        AtomicBoolean checked = new AtomicBoolean(false);
        UUID expected = UUID.fromString("AE012D8C-EA99-4944-9DBD-EA3E2E743FDE");
        stream("uuid.csv")
                .forEach(row -> {
                    assertEquals("Should get the expected row content", expected, row.getAs("uuid", UUID::fromString));
                    checked.set(true);
                });
        assertTrue(checked.get());
    }

    private CsvStream stream(String testFile) {
        return new CsvStream(Paths.get(System.getProperty("user.dir"), "src", "test", "resources", "kjss", "util", testFile));
    }

    private <K, V> Map<K, V> mapOf(K k1, V v1, K k2, V v2, K k3, V v3) {
        HashMap<K, V> map = new HashMap<>();
        map.put(k1, v1);
        map.put(k2, v2);
        map.put(k3, v3);
        return Collections.unmodifiableMap(map);
    }
}
