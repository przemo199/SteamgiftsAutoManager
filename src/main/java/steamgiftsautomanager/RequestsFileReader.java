package steamgiftsautomanager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

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
    private static final int VALID_COOKIE_LENGTH = 48;
    private static final int VALID_XSRF_TOKEN_LENGTH = 32;

    private static final String REQUESTS_FILE_NAME = "requests.txt";

    private RequestsFileReader() {
    }

    private static String[] readRequestsFile() {
        if (Files.exists(Paths.get(REQUESTS_FILE_NAME))) {
            try {
                return Files.readAllLines(Paths.get(REQUESTS_FILE_NAME)).toArray(new String[0]);
            } catch (IOException e) {
                throw new RuntimeException("Error when reading requests file");
            }
        } else if (Files.exists(Paths.get("./requests/" + REQUESTS_FILE_NAME))) {
            try {
                return Files.readAllLines(Paths.get("./requests/" + REQUESTS_FILE_NAME)).toArray(new String[0]);
            } catch (IOException e) {
                throw new RuntimeException("Error when reading requests file");
            }
        } else {
            throw new RuntimeException("Requests file not found");
        }
    }

    private static void writeRequestsFileContent(RequestsFileContent requestsFileContent) {
        String newLine = System.lineSeparator();
        String content = requestsFileContent.getCookieName() + "=" + requestsFileContent.getCookieValue() + newLine +
                (requestsFileContent.getXsrfToken() + newLine +
                        Tag.EXACT_MATCH + newLine +
                        String.join(newLine, requestsFileContent.getExactMatches()) + newLine +
                        Tag.ANY_MATCH + newLine +
                        String.join(newLine, requestsFileContent.getAnyMatches()) + newLine +
                        Tag.NO_MATCH + newLine +
                        String.join(newLine, requestsFileContent.getNoMatches()) + newLine).toLowerCase();

        if (Files.exists(Paths.get(REQUESTS_FILE_NAME))) {
            try {
                Files.writeString(Paths.get(REQUESTS_FILE_NAME), content);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (Files.exists(Paths.get("./requests/" + REQUESTS_FILE_NAME))) {
            try {
                Files.writeString(Paths.get("./requests/" + REQUESTS_FILE_NAME), content);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static boolean isValidCookie(String cookie) {
        if (!cookie.contains("=")) return false;
        String[] elements = cookie.split("=");
        return elements.length == 2 && elements[0].length() != 0 && elements[1].length() == VALID_COOKIE_LENGTH;
    }

    private static boolean isValidXsrfToken(String token) {
        return token.length() == VALID_XSRF_TOKEN_LENGTH;
    }

    private static String[] getSortedAndUniqueTitlesByTag(Tag tag, List<String> lines) {
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

        String[] result = (new TreeSet<>(tagMatches)).toArray(new String[0]);
        Arrays.sort(result);
        return result;
    }

    private static RequestsFileContent getRequestsFileContent() {
        List<String> lines = Arrays.asList(readRequestsFile());

        if (!isValidCookie(lines.get(0))) throw new RuntimeException("Invalid cookie format");
        if (!isValidXsrfToken(lines.get(1))) throw new RuntimeException("Invalid token format");

        String[] cookieElements = lines.get(0).split("=");

        String[] exactMatch = getSortedAndUniqueTitlesByTag(Tag.EXACT_MATCH, lines);
        String[] anyMatch = getSortedAndUniqueTitlesByTag(Tag.ANY_MATCH, lines);
        String[] noMatch = getSortedAndUniqueTitlesByTag(Tag.NO_MATCH, lines);

        Utils.printFoundRequestedTitles(exactMatch.length, Tag.EXACT_MATCH.toString());
        Utils.printFoundRequestedTitles(anyMatch.length, Tag.ANY_MATCH.toString());
        Utils.printFoundRequestedTitles(noMatch.length, Tag.NO_MATCH.toString());

        return new RequestsFileContent(cookieElements[0], cookieElements[1], lines.get(1), exactMatch, anyMatch, noMatch);
    }

    public static RequestsFileContent readRequestsFileContent() {
        Instant start = Instant.now();
        RequestsFileContent requestsFileContent = getRequestsFileContent();
        writeRequestsFileContent(requestsFileContent);
        Utils.printRequestsFileParsingTime(Duration.between(start, Instant.now()).toMillis());
        return requestsFileContent;
    }
}
