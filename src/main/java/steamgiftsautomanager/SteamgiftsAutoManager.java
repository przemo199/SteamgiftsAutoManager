package steamgiftsautomanager;

import java.time.Duration;
import java.time.Instant;

public class SteamgiftsAutoManager {

    public static void main(String[] args) {
        try {
            Instant start = Instant.now();

            RequestsFileContent requestsFileContent = RequestsReader.readRequestsFileContent();
            SteamgiftsHttpDriver steamgiftsHttpDriver = new SteamgiftsHttpDriver(requestsFileContent);
            Giveaway[] giveaways = steamgiftsHttpDriver.scrapeAvailableGiveaways();
            Giveaway[] filteredGiveaways = Utils.filterGiveaways(giveaways, requestsFileContent);

            steamgiftsHttpDriver.enterGiveaways(filteredGiveaways);

            System.out.println("Total execution time: " + Duration.between(start, Instant.now()).toMillis() + "ms");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
