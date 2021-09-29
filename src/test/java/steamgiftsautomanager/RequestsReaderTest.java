package steamgiftsautomanager;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

class RequestsReaderTest {
    private static final Method isValidCookie = getPublicIsValidCookie();
    private static final Method getTitlesByTag = getPublicGetTitlesByTag();

    static Method getPublicIsValidCookie() {
        try {
            Class<?> utils = RequestsReader.class;
            Method customFilter = utils.getDeclaredMethod("isValidCookie", String.class);
            customFilter.setAccessible(true);
            return customFilter;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    static Method getPublicGetTitlesByTag() {
        try {
            Class<?> utils = RequestsReader.class;
            Method getTitlesByTag = utils.getDeclaredMethod("getTitlesByTag", Tag.class, List.class);
            getTitlesByTag.setAccessible(true);
            return getTitlesByTag;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @BeforeAll
    static void init() {
        assert isValidCookie != null;
    }

    @Test
    void isValidCookieTest() {
        try {
            assertFalse((boolean) isValidCookie.invoke(null, ""));
            assertFalse((boolean) isValidCookie.invoke(null, "cookie"));
            assertFalse((boolean) isValidCookie.invoke(null, "cookieName"));
            assertFalse((boolean) isValidCookie.invoke(null, "cookie=1234"));
            assertFalse((boolean) isValidCookie.invoke(null, "="));
            assertFalse((boolean) isValidCookie.invoke(null, "=" + "a".repeat(48)));
            assertTrue((boolean) isValidCookie.invoke(null, "cookie=" + "a".repeat(48)));
            assertTrue((boolean) isValidCookie.invoke(null, "cookie=" + "1".repeat(48)));
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    void setGetTitlesByTagTest() {
        String exactMatchTest = "exactMatchTest";
        String anyMatchTest = "anyMatchTest";
        String noMatchTest = "noMatchTest";
        String[] content = new String[]{
                Tag.EXACT_MATCH.toString(),
                exactMatchTest,
                Tag.ANY_MATCH.toString(),
                anyMatchTest,
                Tag.NO_MATCH.toString(),
                noMatchTest
        };
        List<String> contentList = Arrays.asList(content);

        try {
            assertArrayEquals((String[])getTitlesByTag.invoke(null, Tag.EXACT_MATCH, contentList), new String[]{exactMatchTest});
            assertArrayEquals((String[])getTitlesByTag.invoke(null, Tag.ANY_MATCH, contentList), new String[]{anyMatchTest});
            assertArrayEquals((String[])getTitlesByTag.invoke(null, Tag.NO_MATCH, contentList), new String[]{noMatchTest});
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    void tagTest() {
        assertTrue(Tag.contains("[exact_match]"));
        assertTrue(Tag.contains("[any_match]"));
        assertTrue(Tag.contains("[no_match]"));
        assertFalse(Tag.contains(null));
        assertFalse(Tag.contains(""));
        assertFalse(Tag.contains("match"));
        assertFalse(Tag.contains("[]"));
        assertEquals("[exact_match]", Tag.EXACT_MATCH.toString());
        assertEquals("[any_match]", Tag.ANY_MATCH.toString());
        assertEquals("[no_match]", Tag.NO_MATCH.toString());
    }
}
