package steamgiftsautomanager;

import java.util.HashSet;
import java.util.Set;

public final class RequestsFileContent {
    private final String cookieName;
    private final String cookieValue;
    private final String xsrfToken;
    private String[] exactMatches;
    private final String[] anyMatches;
    private final String[] noMatches;

    RequestsFileContent(String cookieName, String cookieValue, String xsrfToken, String[] exactMatches, String[] anyMatches, String[] noMatches) {
        this.cookieName = cookieName;
        this.cookieValue = cookieValue;
        this.xsrfToken = xsrfToken;
        this.exactMatches = exactMatches;
        this.anyMatches = anyMatches;
        this.noMatches = noMatches;
    }

    public String getCookieName() {
        return cookieName;
    }

    public String getCookieValue() {
        return cookieValue;
    }

    public String getXsrfToken() {
        return xsrfToken;
    }

    public String[] getExactMatches() {
        return exactMatches;
    }

    public String[] getAnyMatches() {
        return anyMatches;
    }

    public String[] getNoMatches() {
        return noMatches;
    }

    public void addExactMatches(String[] exactMatches) {
        Set<String> titleSet = new HashSet<>(Set.of(this.exactMatches));
        titleSet.addAll(Set.of(exactMatches));
        this.exactMatches = titleSet.toArray(new String[0]);
    }
}
