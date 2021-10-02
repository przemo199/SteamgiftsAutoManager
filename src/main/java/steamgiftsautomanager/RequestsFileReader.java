package steamgiftsautomanager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

enum Tag {
    EXACT_MATCH("[exact_match]"),
    ANY_MATCH("[any_match]"),
    NO_MATCH("[no_match]");

    private final String tagString;

    Tag(String tagString) {
        this.tagString = tagString;
    }

    @Override
    public String toString() {
        return tagString;
    }

    public static boolean contains(String string) {
        for (Tag tag : Tag.values()) {
            if (tag.toString().equals(string)) {
                return true;
            }
        }

        return false;
    }
}

public class RequestsFileReader {
    private static final String REQUESTS_FILE = "./requests.txt";

    private RequestsFileReader() {
    }

    private static String[] readRequestsFile() {
        try {
            return Files.readAllLines(Paths.get(REQUESTS_FILE)).toArray(new String[0]);
        } catch (IOException e) {
            throw new RuntimeException("Requests file not found");
        }
    }

    private static void sortRequestedTitles(RequestsFileContent requestsFileContent) {
        Arrays.sort(requestsFileContent.getExactMatches());
        Arrays.sort(requestsFileContent.getAnyMatches());
        Arrays.sort(requestsFileContent.getNoMatches());
    }

    private static void writeRequestsFileContent(RequestsFileContent requestsFileContent) {
        String content = requestsFileContent.getCookieName() + "=" + requestsFileContent.getCookieValue() + "\n" +
                (requestsFileContent.getXsrfToken() + "\n" +
                        Tag.EXACT_MATCH + "\n" +
                        String.join("\n", requestsFileContent.getExactMatches()) + "\n" +
                        Tag.ANY_MATCH + "\n" +
                        String.join("\n", requestsFileContent.getAnyMatches()) + "\n" +
                        Tag.NO_MATCH + "\n" +
                        String.join("\n", requestsFileContent.getNoMatches()) + "\n").toLowerCase();

        try {
            Files.writeString(Paths.get(REQUESTS_FILE), content);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static boolean isValidCookie(String cookie) {
        if (!cookie.contains("=")) return false;
        String[] elements = cookie.split("=");
        return elements.length == 2 && elements[0].length() != 0 && elements[1].length() == 48;
    }

    private static boolean isValidXsrfToken(String token) {
        return token.length() == 32;
    }

    private static String[] getTitlesByTag(Tag tag, List<String> lines) {
        List<String> tagMatches = new ArrayList<>();
        int tagMatchIndex = lines.indexOf(tag.toString());

        if (tagMatchIndex == -1) return new String[0];

        for (int i = tagMatchIndex + 1; i < lines.size(); i++) {
            String line = lines.get(i);
            if (!Tag.contains(line) && !line.equals("")) {
                tagMatches.add(line);
            } else {
                break;
            }
        }

        return tagMatches.toArray(new String[0]);
    }

    private static RequestsFileContent getRequestsFileContent() {
        List<String> lines = Arrays.asList(readRequestsFile());

        if (!isValidCookie(lines.get(0))) throw new RuntimeException("Invalid cookie format");
        if (!isValidXsrfToken(lines.get(1))) throw new RuntimeException("Invalid token format");

        String[] cookieElements = lines.get(0).split("=");

        return new RequestsFileContent(cookieElements[0], cookieElements[1], lines.get(1),
                getTitlesByTag(Tag.EXACT_MATCH, lines),
                getTitlesByTag(Tag.ANY_MATCH, lines),
                getTitlesByTag(Tag.NO_MATCH, lines)
        );
    }

    public static RequestsFileContent readRequestsFileContent() {
        Instant start = Instant.now();
        RequestsFileContent requestsFileContent = getRequestsFileContent();
        sortRequestedTitles(requestsFileContent);
        writeRequestsFileContent(requestsFileContent);
        System.out.println("Requests file sorted in: " + Duration.between(start, Instant.now()).toMillis() + "ms");
        return requestsFileContent;
    }
}
