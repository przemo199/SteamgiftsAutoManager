package steamgiftsautomanager;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class SteamgiftsHttpClient {
    private static final String BASE_URL = "https://www.steamgifts.com";
    private static final String GIVEAWAY_SEARCH_URL = BASE_URL + "/giveaways/search?page=";
    private static final String AJAX_REQUEST_URL = BASE_URL + "/ajax.php";
    private static final String ENTERED_GIVEAWAYS_URL = BASE_URL + "/giveaways/entered";
    private static final String ENTERED_GIVEAWAYS_SEARCH_URL = ENTERED_GIVEAWAYS_URL + "/search?page=";
    private static final String INNER_GIVEAWAY_WRAP_CLASS = ".giveaway__row-inner-wrap";
    private static final String GIVEAWAY_HEADING_NAME_CLASS = ".giveaway__heading__name";
    private static final String GIVEAWAY_THUMBNAIL_CLASS = ".giveaway_image_thumbnail";
    private static final String GIVEAWAY_THUMBNAIL_MISSING_CLASS = ".giveaway_image_thumbnail_missing";
    private static final String GIVEAWAY_MISC_CLASS = ".giveaway__heading__thin";
    private static final String NAV_POINTS_CLASS = ".nav__points";
    private static final String TABLE_ROW_INNER_WRAP_CLASS = ".table__row-inner-wrap";
    private static final String TABLE_COLUMN_SECONDARY_LINK_CLASS = ".table__column__secondary-link";
    private static final String TABLE_COLUMN_HEADING_CLASS = ".table__column__heading";
    private static final String NOT_NUMBER_REGEX = "[^0-9]";
    private static final String[] SUCCESS_KEYWORDS = {"success", "entry_count", "points"};
    private final RequestsFileContent requestsFileContent;

    private boolean hasNoSession() {
        var document = getDocumentFromUrl(BASE_URL);
        if (document == null) return false;
        return document.toString().contains("Sign in through STEAM");
    }

    public SteamgiftsHttpClient(RequestsFileContent requestsFileContent) {
        this.requestsFileContent = requestsFileContent;
        if (hasNoSession()) throw new RuntimeException("No session associated with the provided cookie found");
    }

    public Giveaway[] scrapeAvailableGiveaways() {
        Map<String, Giveaway> giveaways = new HashMap<>();
        int pageNumber = 1;
        AtomicInteger scrappedPages = new AtomicInteger(0);
        Stream<Boolean> hasMorePages;
        int requestBatchSize = 10;
        Instant startTime = Instant.now();

        try (var threadPool = Executors.newVirtualThreadPerTaskExecutor()) {
            List<CompletableFuture<Giveaway[]>> futures = new ArrayList<>();
            do {
                hasMorePages = IntStream.range(pageNumber, pageNumber + requestBatchSize).mapToObj(index -> threadPool.submit(() -> {
                    var document = getDocumentFromUrl(GIVEAWAY_SEARCH_URL + index);

                    if (document == null || document.toString().contains("No results were found.")) {
                        return null;
                    }

                    var gameElements = document.select(INNER_GIVEAWAY_WRAP_CLASS);

                    scrappedPages.getAndIncrement();

                    return gameElements.stream().map(this::getGiveawayFromElement).filter(Objects::nonNull).toList().toArray(Giveaway[]::new);
                })).map(giveawayFuture -> {
                    try {
                        Giveaway[] giveawayList = giveawayFuture.get();
                        if (giveawayList == null) {
                            return false;
                        } else {
                            for (Giveaway giveaway : giveawayList) {
                                giveaways.put(giveaway.getRelativeUrl(), giveaway);
                            }
                        }
                    } catch (ExecutionException | InterruptedException e) {
                        e.printStackTrace();
                        Thread.currentThread().interrupt();
                    }
                    return true;
                });
                pageNumber += requestBatchSize;

                Utils.printScrapedGiveaways(scrappedPages.get(), giveaways.size(), Duration.between(startTime, Instant.now()).toMillis());
            } while (hasMorePages.allMatch(x -> x));

            System.out.println();

            return giveaways.values().toArray(Giveaway[]::new);
        }
    }

    private Giveaway getGiveawayFromElement(Element element) {
        Element nameElement = element.select(GIVEAWAY_HEADING_NAME_CLASS).first();
        if (nameElement == null) return null;
        String title = nameElement.text();

        String relativeUrl;
        Elements elements = element.select(GIVEAWAY_THUMBNAIL_CLASS);
        if (elements.hasAttr("href")) {
            relativeUrl = elements.attr("href");
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
        } catch (IOException exception) {
            exception.printStackTrace();
        }

        return document;
    }

    private int getRemainingPoints() {
        var document = getDocumentFromUrl(BASE_URL);
        if (document == null) return 0;
        return Integer.parseInt(document.select(NAV_POINTS_CLASS).text());
    }

    private String[] scrapeLinksToEnteredGiveaways() {
        List<String> links = new ArrayList<>();
        int pageNumber = 1;
        boolean hasMore = true;

        do {
            var document = getDocumentFromUrl(ENTERED_GIVEAWAYS_SEARCH_URL + pageNumber);
            if (document != null) {
                var elements = document.select(TABLE_ROW_INNER_WRAP_CLASS);

                for (var element : elements) {
                    if (element.select(TABLE_COLUMN_SECONDARY_LINK_CLASS).isEmpty()) {
                        if (element == elements.last()) {
                            hasMore = false;
                            break;
                        }
                    } else {
                        links.add(element.select(TABLE_COLUMN_HEADING_CLASS).attr("href"));
                    }
                }

                pageNumber++;
            } else {
                hasMore = false;
            }
        } while (hasMore);

        return links.toArray(String[]::new);
    }

    private boolean enterGiveaway(Giveaway giveaway) {
        try {
            String body = "xsrf_token=" + requestsFileContent.getXsrfToken() + "&do=entry_insert&code=" +
                    giveaway.getGiveawayCode();

            Document document = Jsoup.connect(AJAX_REQUEST_URL).referrer(BASE_URL + giveaway.getRelativeUrl())
                    .cookie(requestsFileContent.getCookieName(), requestsFileContent.getCookieValue())
                    .requestBody(body).ignoreContentType(true).post();

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

    public void enterGiveaways(final Giveaway[] giveaways) {
        List<String> linksToEnteredGiveaways = Arrays.asList(scrapeLinksToEnteredGiveaways());
        List<Giveaway> notEnteredGiveaways = new ArrayList<>();

        Utils.printFoundEnteredGiveaways(linksToEnteredGiveaways.size());

        for (Giveaway giveaway : giveaways) {
            if (!linksToEnteredGiveaways.contains(giveaway.getRelativeUrl())) {
                notEnteredGiveaways.add(giveaway);
            }
        }

        Utils.printFoundGiveawayCandidates(notEnteredGiveaways.size());

        try (var threadPool = Executors.newVirtualThreadPerTaskExecutor()) {
            List<CompletableFuture<Giveaway>> futures = new ArrayList<>();
            var giveawayList = notEnteredGiveaways.stream().map(giveaway -> threadPool.submit(() -> {
                if (enterGiveaway(giveaway)) {
                    Utils.printEnteredGiveaway(giveaway.getTitle());
                    return giveaway;
                } else {
                    Utils.printFailedToEnterGiveaway(giveaway.getTitle());
                    return null;
                }
            })).map(future -> {
                try {
                    return future.get();
                }  catch (ExecutionException | InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }).filter(Objects::nonNull).toList();
            int enteredGiveaways = giveawayList.size();
            int pointsSpent = giveawayList.stream().map(Giveaway::getPointCost).reduce(0, Integer::sum);

            Utils.printFinalSummary(enteredGiveaways, pointsSpent, getRemainingPoints());
        }
    }

    public String[] scrapeTitlesOfAllEnteredGiveaways() {
        var document = getDocumentFromUrl(ENTERED_GIVEAWAYS_URL);

        if (document == null) return new String[]{};

        var lastDataPageNumberElement = document.select("[data-page-number]").last();

        if (lastDataPageNumberElement == null) return new String[]{};

        int pageCount = Integer.parseInt(lastDataPageNumberElement.attr("data-page-number"));
        try (var threadPool = Executors.newVirtualThreadPerTaskExecutor()) {
            return IntStream.range(1, pageCount).mapToObj(index -> threadPool.submit(() -> {
                Document searchDocument = getDocumentFromUrl(ENTERED_GIVEAWAYS_SEARCH_URL + index);
                if (searchDocument == null) return new String[]{};
                var elements = searchDocument.select(TABLE_COLUMN_HEADING_CLASS);
                List<String> giveawayTitles = new ArrayList<>();
                for (var element : elements) {
                    giveawayTitles.add(element.text());
                }
                return giveawayTitles.toArray(String[]::new);
            })).map(future -> {
                try {
                    var titlesOnPage = future.get();
                    return Arrays.stream(titlesOnPage)
                            .map(titles -> titles.replaceAll(" \\(\\d+ Copies\\)", ""))
                            .collect(Collectors.toSet());
                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                    Thread.currentThread().interrupt();
                }
                return new HashSet<String>();
            }).flatMap(Collection::stream).distinct().toArray(String[]::new);
        }
    }
}
