package steamgiftsautomanager;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class RequestsFileIO {
    private static final int VALID_COOKIE_LENGTH = 48;
    private static final int VALID_XSRF_TOKEN_LENGTH = 32;

    private static final String REQUESTS_FILE_NAME = "requests.txt";

    private RequestsFileIO() {
    }

    private static String[] readRequestsFile() {
        if (Files.exists(Paths.get(REQUESTS_FILE_NAME))) {
            try {
                return Files.readAllLines(Paths.get(REQUESTS_FILE_NAME)).toArray(String[]::new);
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException("Error when reading requests file");
            }
        } else {
            throw new RuntimeException("Requests file not found");
        }
    }

     private static void writeRequestsFileContent(RequestsFileContent requestsFileContent) {
        var newLine = System.lineSeparator();
        var content = requestsFileContent.getCookieName() + "=" + requestsFileContent.getCookieValue() + newLine +
                (requestsFileContent.getXsrfToken() + newLine +
                        MatchTag.EXACT_MATCH + newLine +
                        String.join(newLine, requestsFileContent.getExactMatches()) + newLine +
                        MatchTag.ANY_MATCH + newLine +
                        String.join(newLine, requestsFileContent.getAnyMatches()) + newLine +
                        MatchTag.NO_MATCH + newLine +
                        String.join(newLine, requestsFileContent.getNoMatches()) + newLine).toLowerCase();

        if (Files.exists(Paths.get(REQUESTS_FILE_NAME))) {
            try {
                Files.writeString(Paths.get(REQUESTS_FILE_NAME), content);
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("Requests file not found");
            }
        }
    }

    private static boolean isValidCookie(String cookie) {
        if (!cookie.contains("=")) return false;
        var elements = cookie.split("=");
        return elements.length == 2 && !elements[0].isEmpty() && elements[1].length() == VALID_COOKIE_LENGTH;
    }

    private static boolean isValidXsrfToken(String token) {
        return token.length() == VALID_XSRF_TOKEN_LENGTH;
    }

    private static String[] getSortedAndUniqueTitlesByTag(MatchTag matchTag, List<String> lines) {
        final Set<String> tagMatches = new HashSet<>();
        int tagMatchIndex = lines.indexOf(matchTag.toString());

        if (tagMatchIndex == -1) return new String[0];

        for (int i = tagMatchIndex + 1; i < lines.size(); i++) {
            var line = lines.get(i);
            if (!MatchTag.contains(line) && !line.isEmpty()) {
                tagMatches.add(line);
            } else {
                break;
            }
        }

        var result = tagMatches.toArray(String[]::new);
        Arrays.sort(result);
        return result;
    }

    private static RequestsFileContent getRequestsFileContent() {
        var lines = Arrays.asList(readRequestsFile());

        if (!isValidCookie(lines.get(0))) throw new RuntimeException("Invalid cookie format");
        if (!isValidXsrfToken(lines.get(1))) throw new RuntimeException("Invalid token format");

        var cookieElements = lines.get(0).split("=");

        var exactMatch = getSortedAndUniqueTitlesByTag(MatchTag.EXACT_MATCH, lines);
        var anyMatch = getSortedAndUniqueTitlesByTag(MatchTag.ANY_MATCH, lines);
        var noMatch = getSortedAndUniqueTitlesByTag(MatchTag.NO_MATCH, lines);

        Utils.printFoundRequestedTitles(exactMatch.length, MatchTag.EXACT_MATCH.toString());
        Utils.printFoundRequestedTitles(anyMatch.length, MatchTag.ANY_MATCH.toString());
        Utils.printFoundRequestedTitles(noMatch.length, MatchTag.NO_MATCH.toString());

        return new RequestsFileContent(cookieElements[0], cookieElements[1], lines.get(1), exactMatch, anyMatch, noMatch);
    }

    public static RequestsFileContent readRequestsFileContent() {
        var start = Instant.now();
        var requestsFileContent = getRequestsFileContent();
        writeRequestsFileContent(requestsFileContent);
        Utils.printRequestsFileParsingTime(Duration.between(start, Instant.now()).toMillis());
        return requestsFileContent;
    }

    public static void updateRequestsFileContent(final RequestsFileContent requestsFileContent, final String[] newTitles) {
        var start = Instant.now();
        writeRequestsFileContent(updateRequestsFile(requestsFileContent, newTitles));
        System.out.println("Updated requested titles in " + Duration.between(start, Instant.now()).toMillis() / 1000.0 + "s");
    }

    private static RequestsFileContent updateRequestsFile(final RequestsFileContent requestsFileContent, final String[] newTitles) {
        var uniqueTitles = new HashSet<>(Arrays.asList(requestsFileContent.getExactMatches()));
        uniqueTitles.addAll(Set.of(newTitles));
        return requestsFileContent.withExactMatches(uniqueTitles.toArray(String[]::new));
    }
}
