package steamgiftsautomanager;

import java.time.Duration;
import java.time.Instant;

public class SteamgiftsAutoManager {

    public static void main(String[] args) {
        try {
            Instant startTime = Instant.now();

            RequestsFileContent requestsFileContent = RequestsFileReader.readRequestsFileContent();
            SteamgiftsHttpClient steamgiftsHttpClient = new SteamgiftsHttpClient(requestsFileContent);
            Giveaway[] giveaways = steamgiftsHttpClient.scrapeAvailableGiveaways();
            Giveaway[] filteredGiveaways = Utils.filterGiveaways(giveaways, requestsFileContent);

            steamgiftsHttpClient.enterGiveaways(filteredGiveaways);

            System.out.println("Total execution time: " + Duration.between(startTime, Instant.now()).toMillis() + "ms");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
