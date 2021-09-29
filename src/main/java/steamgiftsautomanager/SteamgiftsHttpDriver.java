package steamgiftsautomanager;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class SteamgiftsHttpDriver {
    static final String BASE_URL = "https://www.steamgifts.com/";
    static final String SEARCH_URL = BASE_URL + "giveaways/search?page=";
    static final String AJAX_URL = BASE_URL + "ajax.php";
    static final String ENTERED_GIVEAWAYS_SEARCH_URL = BASE_URL + "/giveaways/entered/search?page=";

    static final String INNER_GIVEAWAY_WRAP_CLASS = ".giveaway__row-inner-wrap";
    static final String GIVEAWAY_HEADING_NAME_CLASS = ".giveaway__heading__name";
    static final String GIVEAWAY_THUMBNAIL_CLASS = ".giveaway_image_thumbnail";
    static final String GIVEAWAY_THUMBNAIL_MISSING_CLASS = ".giveaway_image_thumbnail_missing";
    static final String GIVEAWAY_MISC_CLASS = ".giveaway__heading__thin";
    static final String NAV_POINTS_CLASS = ".nav__points";

    private final RequestsFileContent requestsFileContent;

    public SteamgiftsHttpDriver(RequestsFileContent requestsFileContent) {
        this.requestsFileContent = requestsFileContent;
        if (!hasSession()) throw new RuntimeException("No session associated with the provided cookie found");
    }

    private boolean hasSession() {
        Document document = getDocumentFromUrl(BASE_URL);
        return !document.toString().contains("Sign in through STEAM");
    }

    public Giveaway[] scrapeAvailableGiveaways() {
        HashMap<String, Giveaway> giveaways = new HashMap<>();
        int pageNumber = 1;

        Document document;
        do {
            document = getDocumentFromUrl(SEARCH_URL + pageNumber);
            Elements games = document.select(INNER_GIVEAWAY_WRAP_CLASS);

            for (Element element : games) {
                Giveaway giveaway = getGiveawayFromElement(element);
                giveaways.put(giveaway.getRelativeUrl(), giveaway);
            }

            System.out.print("\rScrapped " + pageNumber + " pages and found " + giveaways.size() + " giveaways");

            pageNumber++;
        } while (!document.toString().contains("No results were found."));

        System.out.println();

        return giveaways.values().toArray(new Giveaway[0]);
    }

    private Giveaway getGiveawayFromElement(Element element) {
        String title = element.select(GIVEAWAY_HEADING_NAME_CLASS).first().text();

        String relativeUrl;
        if (element.select(GIVEAWAY_THUMBNAIL_CLASS).hasAttr("href")) {
            relativeUrl = element.select(GIVEAWAY_THUMBNAIL_CLASS).attr("href");
        } else {
            relativeUrl = element.select(GIVEAWAY_THUMBNAIL_MISSING_CLASS).attr("href");
        }

        int pointCost = Integer.parseInt(element.select(GIVEAWAY_MISC_CLASS).last().text().replaceAll("[^0-9]", ""));

        return new Giveaway(title, relativeUrl, pointCost);
    }

    private Document getDocumentFromUrl(String url) {
        Document document = null;
        try {
            document = Jsoup.connect(url).cookie(requestsFileContent.getCookieName(),
                    requestsFileContent.getCookieValue()).get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return document;
    }

    private int getRemainingPoints() {
        Document document = getDocumentFromUrl(BASE_URL);
        return Integer.parseInt(document.select(NAV_POINTS_CLASS).text());
    }

    private String[] getLinksToEnteredGiveaways() {
        List<String> links = new ArrayList<>();
        int pageNumber = 1;
        boolean hasMore = true;

        do {
            Document document = getDocumentFromUrl(ENTERED_GIVEAWAYS_SEARCH_URL + pageNumber);
            Elements elements = document.select(".table__row-inner-wrap");

            for (Element element : elements) {
                if (!element.select(".table__column__secondary-link").isEmpty()) {
                    links.add(element.select(".table_image_thumbnail").attr("href"));
                } else {
                    hasMore = false;
                    break;
                }
            }

            pageNumber++;
        } while (hasMore);

        return links.toArray(new String[0]);
    }

    private void enterGiveaway(Giveaway giveaway) {
        try {
            Document document = Jsoup.connect(AJAX_URL).referrer(BASE_URL + giveaway.getRelativeUrl())
                    .cookie(requestsFileContent.getCookieName(), requestsFileContent.getCookieValue())
                    .requestBody("xsrf_token=" + requestsFileContent.getXsrfToken() + "&do=entry_insert&code=" +
                            giveaway.getGiveawayCode()).ignoreContentType(true).post();

            if (document.text().contains("success")) {
                System.out.println("Entered giveaway for: " + giveaway.getTitle());
            } else {
                System.out.println("Failed to enter giveaway for: " + giveaway.getTitle());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void enterGiveaways(Giveaway[] giveaways) {
        List<String> linksToEnteredGiveaways = Arrays.asList(getLinksToEnteredGiveaways());
        List<Giveaway> enteredGiveaways = new ArrayList<>();

        for (Giveaway giveaway : giveaways) {
            if (!linksToEnteredGiveaways.contains(giveaway.getRelativeUrl())) {
                enterGiveaway(giveaway);
                enteredGiveaways.add(giveaway);
            }
        }

        int pointsSpent = 0;
        for (Giveaway giveaway : enteredGiveaways) {
            pointsSpent += giveaway.getPointCost();
        }

        System.out.println("Entered " + enteredGiveaways.size() + " giveaways, spent " + pointsSpent +
                " points, " + getRemainingPoints() + " points remaining");
    }

}
