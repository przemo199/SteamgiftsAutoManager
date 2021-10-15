package steamgiftsautomanager;

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

    public static void printFoundGiveaways(int giveawaysCount) {
        System.out.println("Found " + giveawaysCount + " giveaway(s) that match requested titles and are not entered");
    }

    public static void printFoundEnteredGiveaways(int giveawaysCount) {
        System.out.println("Found " + giveawaysCount + " already entered giveaway(s)");
    }

    public static void printEnteredGiveaway(String giveawayTitle) {
        System.out.println("Entered giveaway for: " + giveawayTitle);
    }

    public static void printFailedToEnterGiveaway(String giveawayTitle) {
        System.out.println("Failed to enter giveaway for: " + giveawayTitle);
    }

    public static void printScrappedGiveaways(int pageNumber, int giveawaysCount) {
        System.out.print("\rScrapped " + pageNumber + " pages and found " + giveawaysCount + " giveaways");
    }

    public static void printFinalSummary(int giveawaysCount, int pointsSpent, int remainingPoints) {
        System.out.println("Entered " + giveawaysCount + " giveaway(s), spent " + pointsSpent +
                " point(s), " + remainingPoints + " points remaining");
    }

}
