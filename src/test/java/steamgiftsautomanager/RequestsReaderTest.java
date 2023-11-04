package steamgiftsautomanager;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

class RequestsReaderTest {
    private static final Method isValidCookie = getPublicIsValidCookie();
    private static final Method getSortedAndUniqueTitlesByTag = getPublicGetSortedAndUniqueTitlesByTag();

    static Method getPublicIsValidCookie() {
        try {
            Class<?> utils = RequestsFileIO.class;
            Method customFilter = utils.getDeclaredMethod("isValidCookie", String.class);
            customFilter.setAccessible(true);
            return customFilter;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    static Method getPublicGetSortedAndUniqueTitlesByTag() {
        try {
            Class<?> utils = RequestsFileIO.class;
            Method getTitlesByTag = utils.getDeclaredMethod("getSortedAndUniqueTitlesByTag", MatchTag.class, List.class);
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
                MatchTag.EXACT_MATCH.toString(),
                exactMatchTest,
                MatchTag.ANY_MATCH.toString(),
                anyMatchTest,
                MatchTag.NO_MATCH.toString(),
                noMatchTest
        };
        List<String> contentList = Arrays.asList(content);

        try {
            assertArrayEquals((String[]) getSortedAndUniqueTitlesByTag.invoke(null, MatchTag.EXACT_MATCH, contentList), new String[]{exactMatchTest});
            assertArrayEquals((String[]) getSortedAndUniqueTitlesByTag.invoke(null, MatchTag.ANY_MATCH, contentList), new String[]{anyMatchTest});
            assertArrayEquals((String[]) getSortedAndUniqueTitlesByTag.invoke(null, MatchTag.NO_MATCH, contentList), new String[]{noMatchTest});
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    void tagTest() {
        assertTrue(MatchTag.contains("[exact_match]"));
        assertTrue(MatchTag.contains("[any_match]"));
        assertTrue(MatchTag.contains("[no_match]"));
        assertFalse(MatchTag.contains(null));
        assertFalse(MatchTag.contains(""));
        assertFalse(MatchTag.contains("match"));
        assertFalse(MatchTag.contains("[]"));
        assertEquals("[exact_match]", MatchTag.EXACT_MATCH.toString());
        assertEquals("[any_match]", MatchTag.ANY_MATCH.toString());
        assertEquals("[no_match]", MatchTag.NO_MATCH.toString());
    }
}
