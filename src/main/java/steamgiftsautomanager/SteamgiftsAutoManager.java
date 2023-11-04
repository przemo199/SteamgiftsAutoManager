package steamgiftsautomanager;

import java.time.Duration;
import java.time.Instant;

public class SteamgiftsAutoManager {

    public static void main(String[] args) {
        if (args.length == 0) {
            var startTime = Instant.now();

            var requestsFileContent = RequestsFileIO.readRequestsFileContent();
            var steamgiftsHttpClient = new SteamgiftsHttpClient(requestsFileContent);
            var giveaways = steamgiftsHttpClient.scrapeAvailableGiveaways();
            var filteredGiveaways = Utils.filterGiveaways(giveaways, requestsFileContent);

            steamgiftsHttpClient.enterGiveaways(filteredGiveaways);

            Utils.printTotalParsingTime(Duration.between(startTime, Instant.now()).toMillis());
        }

        if (args.length == 1 && args[0].strip().equals("update-titles")) {
            var startTime = Instant.now();

            var requestsFileContent = RequestsFileIO.readRequestsFileContent();
            var steamgiftsHttpClient = new SteamgiftsHttpClient(requestsFileContent);
            var allEnteredGiveaways = steamgiftsHttpClient.scrapeTitlesOfAllEnteredGiveaways();
            RequestsFileIO.updateRequestsFileContent(requestsFileContent, allEnteredGiveaways);

            Utils.printTotalParsingTime(Duration.between(startTime, Instant.now()).toMillis());
        }
    }
}
