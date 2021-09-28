package steamgiftsautomanager;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RequestsFileContentTest {
    @Test
    void requestFileContentValueTest() {
        final String cookieName = "testCookieName";
        final String cookieValue = "testCookieName";
        final String xsrfToken = "testXsrfToken";
        final String[] exactMatches = new String[]{"test"};
        final String[] anyMatches = new String[]{"test"};
        final String[] noMatches = new String[]{"test"};
        RequestsFileContent requestsFileContent =
                new RequestsFileContent(cookieName, cookieValue, xsrfToken, exactMatches, anyMatches, noMatches);
        assertNotNull(requestsFileContent);
        assertEquals(requestsFileContent.getCookieName(), cookieName);
        assertEquals(requestsFileContent.getCookieValue(), cookieValue);
        assertEquals(requestsFileContent.getXsrfToken(), xsrfToken);
        assertArrayEquals(requestsFileContent.getExactMatches(), exactMatches);
        assertArrayEquals(requestsFileContent.getAnyMatches(), anyMatches);
        assertArrayEquals(requestsFileContent.getNoMatches(), noMatches);
    }
}
