package steamgiftsautomanager;

import java.time.Duration;
import java.time.Instant;

public class SteamgiftsAutoManager {

    public static void main(String[] args) {
        if (args.length == 0) {
            Instant startTime = Instant.now();

            RequestsFileContent requestsFileContent = RequestsFileReader.readRequestsFileContent();
            SteamgiftsHttpClient steamgiftsHttpClient = new SteamgiftsHttpClient(requestsFileContent);
            Giveaway[] giveaways = steamgiftsHttpClient.scrapeAvailableGiveaways();
            Giveaway[] filteredGiveaways = Utils.filterGiveaways(giveaways, requestsFileContent);

            steamgiftsHttpClient.enterGiveaways(filteredGiveaways);

            Utils.printTotalParsingTime(Duration.between(startTime, Instant.now()).toMillis());
        }

        if (args.length == 1 && args[0].strip().equals("update-titles")) {
            Instant startTime = Instant.now();

            RequestsFileContent requestsFileContent = RequestsFileReader.readRequestsFileContent();
            SteamgiftsHttpClient steamgiftsHttpClient = new SteamgiftsHttpClient(requestsFileContent);
            String[] allEnteredGiveaways = steamgiftsHttpClient.scrapeTitlesOfAllEnteredGiveaways();
            RequestsFileReader.updateRequestsFileContent(requestsFileContent, allEnteredGiveaways);

            Utils.printTotalParsingTime(Duration.between(startTime, Instant.now()).toMillis());
        }
    }
}
