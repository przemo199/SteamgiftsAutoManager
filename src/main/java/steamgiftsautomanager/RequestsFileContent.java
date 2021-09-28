package steamgiftsautomanager;

public final class RequestsFileContent {
    private final String cookieName;
    private final String cookieValue;
    private final String xsrfToken;
    private final String[] exactMatches;
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
}
