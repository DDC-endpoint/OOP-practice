package edu.practice.d03;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static edu.practice.d03.StorageBilling.*;
import static org.junit.jupiter.api.Assertions.*;

class StorageBillingTest {

    @ParameterizedTest(name = "{0} GB -> {1} block(s)")
    @CsvSource({"1,1", "99,1", "100,1", "101,2", "200,2", "201,3", "1050,11", "2147483647,21474837"})
    @DisplayName("1. blocksFor rounds UP to whole 100 GB blocks (and survives Integer.MAX_VALUE)")
    void blocks(int gb, int expected) {
        assertEquals(expected, blocksFor(gb));
    }

    @Test
    @DisplayName("2. blocksFor rejects 0 and negatives")
    void blocksRejectsNonPositive() {
        assertThrows(IllegalArgumentException.class, () -> blocksFor(0));
        assertThrows(IllegalArgumentException.class, () -> blocksFor(-250));
    }

    @ParameterizedTest(name = "half of {0} -> {1}")
    @CsvSource({"1000,500", "999,500", "1,1", "2,1", "7,4"})
    @DisplayName("3. halfRoundedUp")
    void half(int cents, int expected) {
        assertEquals(expected, halfRoundedUp(cents));
    }

    @Test
    @DisplayName("4. halfRoundedUp rejects 0 and negatives")
    void halfRejects() {
        assertThrows(IllegalArgumentException.class, () -> halfRoundedUp(0));
        assertThrows(IllegalArgumentException.class, () -> halfRoundedUp(-4));
    }

    @ParameterizedTest(name = "{0} GB at {1}c/block -> {2}c")
    @CsvSource({
            "1,999,999",          // 1 block
            "100,999,999",        // still 1 block
            "101,999,1998",       // 2 blocks
            "1000,250,2500",      // exactly 10 blocks, all full price
            "1001,250,2625",      // 11 blocks: 10 x 250 + 1 x 125
            "1500,999,12490",     // 15 blocks: 10 x 999 + 5 x 500
            "4321,333,9008"       // 44 blocks: 10 x 333 + 34 x 167
    })
    @DisplayName("5. priceCents: first 10 blocks full price, the rest half price (rounded up)")
    void price(int gb, int rate, int expected) {
        assertEquals(expected, priceCents(gb, rate));
    }

    @Test
    @DisplayName("6. priceCents rejects bad gb and bad rate")
    void priceRejects() {
        assertThrows(IllegalArgumentException.class, () -> priceCents(0, 100));
        assertThrows(IllegalArgumentException.class, () -> priceCents(500, 0));
        assertThrows(IllegalArgumentException.class, () -> priceCents(500, -1));
    }

    @Test
    @DisplayName("7. priceCents never returns a wrapped-around number: overflow -> IllegalArgumentException")
    void overflowIsRejected() {
        // 21,474,837 blocks: the half-price part alone is about 10.7 billion cents, too big for an int.
        assertThrows(IllegalArgumentException.class, () -> priceCents(Integer.MAX_VALUE, 1_000));
        // the full-price part overflows on its own: 10 x 300,000,000.
        assertThrows(IllegalArgumentException.class, () -> priceCents(1_000, 300_000_000));
        // just fits: 10 x 100,000,000 = 1,000,000,000
        assertEquals(1_000_000_000, priceCents(1_000, 100_000_000));
    }
}
