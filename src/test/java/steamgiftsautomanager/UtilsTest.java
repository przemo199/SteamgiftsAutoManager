package steamgiftsautomanager;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class UtilsTest {
    private static final Method customFilter = getPublicCustomFilter();

    static Method getPublicCustomFilter() {
        try {
            Class<?> utils = Utils.class;
            Method customFilter = utils.getDeclaredMethod("customFilter", String.class, RequestsFileContent.class);
            customFilter.setAccessible(true);
            return customFilter;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @BeforeAll
    static void init() {
        assert customFilter != null;
    }

    @Test
    void filterGiveawaysAddsExactMatchTitleTest() {
        Giveaway[] giveaways = new Giveaway[]{new Giveaway("test", "", 0)};

        RequestsFileContent requestsFileContent = new RequestsFileContent("", "", "",
                new String[]{"test"},
                new String[]{},
                new String[]{});

        assertArrayEquals(Utils.filterGiveaways(giveaways, requestsFileContent), giveaways);
    }

    @Test
    void filterGiveawaysAddsExactMatchTitlesTest() {
        Giveaway result = new Giveaway("test", "", 0);
        Giveaway[] giveaways = new Giveaway[]{
                result,
                new Giveaway("test2", "", 0)
        };

        RequestsFileContent requestsFileContent = new RequestsFileContent("", "", "",
                new String[]{"test"},
                new String[]{},
                new String[]{});

        assertArrayEquals(Utils.filterGiveaways(giveaways, requestsFileContent),
                new Giveaway[]{result});
    }

    @Test
    void filterGiveawaysAddsAnyMatchTitleTest() {
        Giveaway[] giveaways = new Giveaway[]{
                new Giveaway("test", "", 0),
                new Giveaway("test123", "", 0),
                new Giveaway("123test123", "", 0),
                new Giveaway("123test", "", 0)
        };

        RequestsFileContent requestsFileContent = new RequestsFileContent("", "", "",
                new String[]{},
                new String[]{"test"},
                new String[]{});

        assertArrayEquals(Utils.filterGiveaways(giveaways, requestsFileContent), giveaways);
    }

    @Test
    void filterGiveawaysDoesntAddAnyMatchTitleTest() {
        Giveaway[] giveaways = new Giveaway[]{
                new Giveaway("tes", "", 0),
                new Giveaway("est123", "", 0),
                new Giveaway("123te123", "", 0),
                new Giveaway("123tst", "", 0)
        };

        RequestsFileContent requestsFileContent = new RequestsFileContent("", "", "",
                new String[]{},
                new String[]{"test"},
                new String[]{});

        assertArrayEquals(Utils.filterGiveaways(giveaways, requestsFileContent), new Giveaway[]{});
    }

    @Test
    void filterGiveawaysRejectsNoMatchTitlesTest() {
        Giveaway[] giveaways = new Giveaway[]{
                new Giveaway("test", "", 0),
                new Giveaway("test2", "", 0)
        };

        RequestsFileContent requestsFileContent = new RequestsFileContent("", "", "",
                new String[]{"test", "test2"},
                new String[]{},
                new String[]{"test", "test2"});

        assertArrayEquals(Utils.filterGiveaways(giveaways, requestsFileContent), new Giveaway[]{});
    }

    @Test
    void customFilterExactMatchTest() {
        RequestsFileContent requestsFileContent = new RequestsFileContent("", "", "",
                new String[]{"test"},
                new String[]{},
                new String[]{});

        try {
            assertTrue((boolean) customFilter.invoke(null, "test", requestsFileContent));
            assertTrue((boolean) customFilter.invoke(null, "TEST", requestsFileContent));
            assertTrue((boolean) customFilter.invoke(null, "Test", requestsFileContent));
            assertTrue((boolean) customFilter.invoke(null, "test...", requestsFileContent));
            assertFalse((boolean) customFilter.invoke(null, "test123", requestsFileContent));
            assertFalse((boolean) customFilter.invoke(null, "123test123", requestsFileContent));
            assertFalse((boolean) customFilter.invoke(null, "123test", requestsFileContent));
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    void customFilterAnyMatchTest() {
        RequestsFileContent requestsFileContent = new RequestsFileContent("", "", "",
                new String[]{},
                new String[]{"test"},
                new String[]{});

        try {
            assertTrue((boolean) customFilter.invoke(null, "test", requestsFileContent));
            assertTrue((boolean) customFilter.invoke(null, "Test", requestsFileContent));
            assertTrue((boolean) customFilter.invoke(null, "TEST", requestsFileContent));
            assertTrue((boolean) customFilter.invoke(null, "test123", requestsFileContent));
            assertTrue((boolean) customFilter.invoke(null, "123test123", requestsFileContent));
            assertFalse((boolean) customFilter.invoke(null, "", requestsFileContent));
            assertFalse((boolean) customFilter.invoke(null, "tes", requestsFileContent));
            assertFalse((boolean) customFilter.invoke(null, "123", requestsFileContent));
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    void customFilterNoMatchTestWithExactMatch() {
        RequestsFileContent requestsFileContent = new RequestsFileContent("", "", "",
                new String[]{"test"},
                new String[]{},
                new String[]{"test"});

        try {
            assertFalse((boolean) customFilter.invoke(null, "test", requestsFileContent));
            assertFalse((boolean) customFilter.invoke(null, "Test", requestsFileContent));
            assertFalse((boolean) customFilter.invoke(null, "TEST", requestsFileContent));
            assertFalse((boolean) customFilter.invoke(null, "test123", requestsFileContent));
            assertFalse((boolean) customFilter.invoke(null, "123test123", requestsFileContent));
            assertFalse((boolean) customFilter.invoke(null, "", requestsFileContent));
            assertFalse((boolean) customFilter.invoke(null, "tes", requestsFileContent));
            assertFalse((boolean) customFilter.invoke(null, "123", requestsFileContent));
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }

    @Test
    void customFilterNoMatchTestWithAnyMatch() {
        RequestsFileContent requestsFileContent = new RequestsFileContent("", "", "",
                new String[]{},
                new String[]{"test"},
                new String[]{"test"});

        try {
            assertFalse((boolean) customFilter.invoke(null, "test", requestsFileContent));
            assertTrue((boolean) customFilter.invoke(null, "test123", requestsFileContent));
            assertTrue((boolean) customFilter.invoke(null, "123test123", requestsFileContent));
            assertFalse((boolean) customFilter.invoke(null, "", requestsFileContent));
            assertFalse((boolean) customFilter.invoke(null, "tes", requestsFileContent));
            assertFalse((boolean) customFilter.invoke(null, "123", requestsFileContent));
        } catch (Exception e) {
            e.printStackTrace();
            fail();
        }
    }
}
