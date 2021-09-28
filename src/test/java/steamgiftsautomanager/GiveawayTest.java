package steamgiftsautomanager;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class GiveawayTest {
    @Test
    void giveawayValuesTest() {
        final String title = "testTitle";
        final String relativeUrl = "testUrl";
        final int pointCost = 10;
        Giveaway giveaway = new Giveaway(title, relativeUrl, pointCost);
        assertNotNull(giveaway);
        assertEquals(title, giveaway.getTitle());
        assertEquals(relativeUrl, giveaway.getRelativeUrl());
        assertEquals(pointCost, giveaway.getPointCost());
    }

    @Test
    void giveawayGetGiveawayCodeTest() {
        final String title = "testTitle";
        final String testCode = "testCode";
        final String relativeUrl = "/giveaway/" + testCode + "/test";
        final int pointCost = 10;
        Giveaway giveaway = new Giveaway(title, relativeUrl, pointCost);
        assertNotNull(giveaway);
        assertEquals(testCode, giveaway.getGiveawayCode());
    }
}
