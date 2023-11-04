package steamgiftsautomanager;

import lombok.AllArgsConstructor;
import lombok.Value;

@Value
@AllArgsConstructor
public class Giveaway {
    String title;
    String relativeUrl;
    int pointCost;

    public String getGiveawayCode() {
        return this.getRelativeUrl().split("/")[2];
    }
}
