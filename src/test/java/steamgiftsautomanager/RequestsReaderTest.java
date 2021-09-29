package steamgiftsautomanager;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Method;

class RequestsReaderTest {
    private static final Method isValidCookie = getPublicIsValidCookie();

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

    @BeforeAll
    static void init() {
        assert isValidCookie != null;
    }

    @Test
    void isValidCookieTest() {
        try {
            assertFalse((boolean) isValidCookie.invoke(null, "cookie"));
            assertFalse((boolean) isValidCookie.invoke(null, "cookieName"));
            assertFalse((boolean) isValidCookie.invoke(null, "cookie=1234"));
            assertFalse((boolean) isValidCookie.invoke(null, "="));
            assertFalse((boolean) isValidCookie.invoke(null, "=" + "a".repeat(48)));
            assertTrue((boolean) isValidCookie.invoke(null, "cookie=" + "a".repeat(48)));
            assertTrue((boolean) isValidCookie.invoke(null, "cookie=" + "1".repeat(48)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
