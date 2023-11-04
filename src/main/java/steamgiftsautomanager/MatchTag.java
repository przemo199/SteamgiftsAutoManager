package steamgiftsautomanager;

import static java.util.Arrays.stream;

enum MatchTag {
    EXACT_MATCH("[exact_match]"),
    ANY_MATCH("[any_match]"),
    NO_MATCH("[no_match]");

    private final String tagString;

    MatchTag(String tagString) {
        this.tagString = tagString;
    }

    @Override
    public String toString() {
        return tagString;
    }

    public static boolean contains(String tag) {
        return stream(MatchTag.values()).anyMatch(matchTag -> matchTag.toString().equals(tag));
    }
}
