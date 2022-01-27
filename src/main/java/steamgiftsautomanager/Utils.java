package steamgiftsautomanager;

import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

public class Utils {

    private Utils() {
    }

    private static boolean customFilter(String giveawayTitle, RequestsFileContent requestsFileContent) {
        String lowercaseTitle = giveawayTitle.toLowerCase();

        if (lowercaseTitle.endsWith("...")) {
            String shortTitle = lowercaseTitle.substring(0, lowercaseTitle.length() - 3);

            for (String title : requestsFileContent.getNoMatches()) {
                if (title.contains(shortTitle)) {
                    return false;
                }
            }

            for (String title : requestsFileContent.getExactMatches()) {
                if (title.contains(shortTitle)) {
                    return true;
                }
            }

            for (String title : requestsFileContent.getAnyMatches()) {
                if (shortTitle.contains(title.toLowerCase())) {
                    return true;
                }
            }
        } else {
            List<String> noMatchesList = Arrays.asList(requestsFileContent.getNoMatches());
            if (noMatchesList.contains(lowercaseTitle)) {
                return false;
            }

            List<String> exactMatchesList = Arrays.asList(requestsFileContent.getExactMatches());
            if (exactMatchesList.contains(lowercaseTitle)) {
                return true;
            }

            for (String title : requestsFileContent.getAnyMatches()) {
                if (lowercaseTitle.contains(title)) {
                    return true;
                }
            }
        }

        return false;
    }

    public static Giveaway[] filterGiveaways(Giveaway[] giveaways, RequestsFileContent requestsFileContent) {
        return Arrays.stream(giveaways).filter(giveaway ->
                Utils.customFilter(giveaway.getTitle(), requestsFileContent)).toArray(Giveaway[]::new);
    }

    public static void printFoundGiveawayCandidates(int giveawayCount) {
        boolean isOne = giveawayCount == 1;
        System.out.println("Found " + giveawayCount + " candidate " + (isOne ? "giveaway" : "giveaways") +
                " to enter");
    }

    public static void printFoundEnteredGiveaways(int giveawayCount) {
        System.out.println("Found " + giveawayCount + " already entered " +
                (giveawayCount == 1 ? "giveaway" : "giveaways"));
    }

    public static void printEnteredGiveaway(String giveawayTitle) {
        System.out.println("Entered giveaway for: " + giveawayTitle);
    }

    public static void printFailedToEnterGiveaway(String giveawayTitle) {
        System.out.println("Failed to enter giveaway for: " + giveawayTitle);
    }

    public static void printScrapedGiveaways(int pageNumber, int giveawayCount, long time) {
        System.out.print("\rScraped " + pageNumber + (pageNumber == 1 ? " page" : " pages") + " and found " +
                giveawayCount + (giveawayCount == 1 ? " giveaway" : " giveaways") + " in " + time + "ms");
    }

    public static void printFinalSummary(int giveawayCount, int pointsSpent, int remainingPoints) {
        System.out.println("Entered " + giveawayCount + (giveawayCount == 1 ? " giveaway" : " giveaways") + ", spent " +
                pointsSpent + (pointsSpent == 1 ? " point" : " points") + ", " +
                remainingPoints + (remainingPoints == 1 ? " point" : " points") + " remaining");
    }

    public static void printFoundRequestedTitles(int titleCount, String tagName) {
        System.out.println("Found " + titleCount + " requested " + (titleCount == 1 ? " title" : "titles") +
                " tagged as " + tagName);
    }

    public static void printRequestsFileParsingTime(long duration) {
        System.out.println("Requests file parsed and sorted in: " + duration + "ms");
    }
}
