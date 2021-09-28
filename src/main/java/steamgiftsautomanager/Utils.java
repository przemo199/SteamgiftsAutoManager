package steamgiftsautomanager;

import java.util.ArrayList;
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
        Giveaway[] filteredGiveaways = Arrays.stream(giveaways).filter(giveaway ->
                Utils.customFilter(giveaway.getTitle(), requestsFileContent)).toArray(Giveaway[]::new);

        System.out.println("Found " + filteredGiveaways.length + " giveaways matching requested game titles");

        return filteredGiveaways;
    }

}
