package steamgiftsautomanager;

public final class Giveaway {
    private final String title;
    private final String relativeUrl;
    private final int pointCost;

    Giveaway(String title, String relativeUrl, int pointCost) {
        this.title = title;
        this.relativeUrl = relativeUrl;
        this.pointCost = pointCost;
    }

    public String getTitle() {
        return title;
    }

    public String getRelativeUrl() {
        return relativeUrl;
    }

    public int getPointCost() {
        return pointCost;
    }

    public String getGiveawayCode() {
        return this.getRelativeUrl().split("/")[2];
    }

    @Override
    public String toString() {
        return "Giveaway{" +
                "title='" + title + '\'' +
                ", relativeUrl='" + relativeUrl + '\'' +
                ", pointCost=" + pointCost +
                '}';
    }
}
