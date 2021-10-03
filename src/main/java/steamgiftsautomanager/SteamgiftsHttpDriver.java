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
    private static final String BASE_URL = "https://www.steamgifts.com/";
    private static final String SEARCH_URL = BASE_URL + "giveaways/search?page=";
    private static final String AJAX_URL = BASE_URL + "ajax.php";
    private static final String ENTERED_GIVEAWAYS_SEARCH_URL = BASE_URL + "/giveaways/entered/search?page=";

    private static final String INNER_GIVEAWAY_WRAP_CLASS = ".giveaway__row-inner-wrap";
    private static final String GIVEAWAY_HEADING_NAME_CLASS = ".giveaway__heading__name";
    private static final String GIVEAWAY_THUMBNAIL_CLASS = ".giveaway_image_thumbnail";
    private static final String GIVEAWAY_THUMBNAIL_MISSING_CLASS = ".giveaway_image_thumbnail_missing";
    private static final String GIVEAWAY_MISC_CLASS = ".giveaway__heading__thin";
    private static final String NAV_POINTS_CLASS = ".nav__points";

    private static final String NOT_NUMBER_REGEX = "[^0-9]";

    private static final String[] SUCCESS_KEYWORDS = new String[]{"success", "entry_count", "points"};

    private final RequestsFileContent requestsFileContent;

    public SteamgiftsHttpDriver(RequestsFileContent requestsFileContent) {
        this.requestsFileContent = requestsFileContent;
        if (!hasSession()) throw new RuntimeException("No session associated with the provided cookie found");
    }

    private boolean hasSession() {
        Document document = getDocumentFromUrl(BASE_URL);
        if (document == null) return false;
        return !document.toString().contains("Sign in through STEAM");
    }

    public Giveaway[] scrapeAvailableGiveaways() {
        HashMap<String, Giveaway> giveaways = new HashMap<>();
        int pageNumber = 1;

        Document document;
        do {
            document = getDocumentFromUrl(SEARCH_URL + pageNumber);
            if (document == null) break;
            Elements games = document.select(INNER_GIVEAWAY_WRAP_CLASS);

            for (Element element : games) {
                Giveaway giveaway = getGiveawayFromElement(element);
                if (giveaway != null) {
                    giveaways.put(giveaway.getRelativeUrl(), giveaway);
                }
            }

            System.out.print("\rScrapped " + pageNumber + " pages and found " + giveaways.size() + " giveaways");

            pageNumber++;
        } while (!document.toString().contains("No results were found."));

        System.out.println();

        return giveaways.values().toArray(new Giveaway[0]);
    }

    private Giveaway getGiveawayFromElement(Element element) {
        Element nameElement = element.select(GIVEAWAY_HEADING_NAME_CLASS).first();
        if (nameElement == null) return null;
        String title = nameElement.text();

        String relativeUrl;
        if (element.select(GIVEAWAY_THUMBNAIL_CLASS).hasAttr("href")) {
            relativeUrl = element.select(GIVEAWAY_THUMBNAIL_CLASS).attr("href");
        } else {
            relativeUrl = element.select(GIVEAWAY_THUMBNAIL_MISSING_CLASS).attr("href");
        }

        int pointCost = 0;
        Element pointElement = element.select(GIVEAWAY_MISC_CLASS).last();
        if (pointElement != null) {
            pointCost = Integer.parseInt(pointElement.text().replaceAll(NOT_NUMBER_REGEX, ""));
        }

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
        if (document == null) return 0;
        return Integer.parseInt(document.select(NAV_POINTS_CLASS).text());
    }

    private String[] getLinksToEnteredGiveaways() {
        List<String> links = new ArrayList<>();
        int pageNumber = 1;
        boolean hasMore = true;

        do {
            Document document = getDocumentFromUrl(ENTERED_GIVEAWAYS_SEARCH_URL + pageNumber);
            if (document != null) {
                Elements elements = document.select(".table__row-inner-wrap");

                for (Element element : elements) {
                    if (!element.select(".table__column__secondary-link").isEmpty()) {
                        links.add(element.select(".table_image_thumbnail").attr("href"));
                    } else {
                        if (element == elements.last()) {
                            hasMore = false;
                            break;
                        }
                    }
                }

                pageNumber++;
            } else {
                hasMore = false;
            }
        } while (hasMore);

        return links.toArray(new String[0]);
    }

    private boolean enterGiveaway(Giveaway giveaway) {
        try {
            Document document = Jsoup.connect(AJAX_URL).referrer(BASE_URL + giveaway.getRelativeUrl())
                    .cookie(requestsFileContent.getCookieName(), requestsFileContent.getCookieValue())
                    .requestBody("xsrf_token=" + requestsFileContent.getXsrfToken() + "&do=entry_insert&code=" +
                            giveaway.getGiveawayCode()).ignoreContentType(true).post();
            String response = document.text();

            for (String element : SUCCESS_KEYWORDS) {
                if (!response.contains(element)) {
                    return false;
                }
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public void enterGiveaways(Giveaway[] giveaways) {
        List<String> linksToEnteredGiveaways = Arrays.asList(getLinksToEnteredGiveaways());
        List<Giveaway> notEnteredGiveaways = new ArrayList<>();
        List<Giveaway> enteredGiveaways = new ArrayList<>();

        System.out.println("Found " + linksToEnteredGiveaways.size() + " already entered giveaways");

        for (Giveaway giveaway : giveaways) {
            if (!linksToEnteredGiveaways.contains(giveaway.getRelativeUrl())) {
                notEnteredGiveaways.add(giveaway);
            }
        }

        System.out.println("Found " + notEnteredGiveaways.size()
                + " giveaways that match requested titles and are not entered");

        for (Giveaway giveaway : notEnteredGiveaways) {
            if (enterGiveaway(giveaway)) {
                enteredGiveaways.add(giveaway);
                System.out.println("Entered giveaway for: " + giveaway.getTitle());
            } else {
                System.out.println("Failed to enter giveaway for: " + giveaway.getTitle());
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
