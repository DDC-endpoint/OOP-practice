package edu.practice.d03;

/**
 * Cloud storage is sold in BLOCKS of 100 GB. Any started block is charged in full.
 *
 *   1-100 GB  -> 1 block,   101-200 GB -> 2 blocks,   ...
 *
 * Price: the first 10 blocks cost `centsPerBlock` each.
 *        Every block AFTER the 10th costs half that, rounded UP to a whole cent.
 *
 * Everything is in whole cents (int). Bad input or a result too big for an int
 * must throw IllegalArgumentException, never return a wrong (wrapped-around) number.
 */
public final class StorageBilling {

    public static final int GB_PER_BLOCK = 100;
    public static final int FULL_PRICE_BLOCKS = 10;

    private StorageBilling() { }

    /** How many 100 GB blocks are needed to hold `gb` gigabytes. */
    public static int blocksFor(int gb) {
        if (gb <= 0) throw new IllegalArgumentException("gb must be positive");
        return (gb - 1) / GB_PER_BLOCK + 1;
    }

    /** Half of `cents`, rounded up: 999 -> 500, 1000 -> 500, 1 -> 1. */
    public static int halfRoundedUp(int cents) {
        if (cents <= 0) throw new IllegalArgumentException("cents must be positive");
        return (cents - 1) / 2 + 1;
    }

    /** Monthly price in cents for storing `gb` gigabytes. */
    public static int priceCents(int gb, int centsPerBlock) {
        if (centsPerBlock <= 0) throw new IllegalArgumentException("centsPerBlock must be positive");
        int blocks = blocksFor(gb);
        int full = Math.min(blocks, FULL_PRICE_BLOCKS);
        int rest = blocks - full;
        try {
            return Math.addExact(Math.multiplyExact(full, centsPerBlock),
                                 Math.multiplyExact(rest, halfRoundedUp(centsPerBlock)));
        } catch (ArithmeticException e) {
            throw new IllegalArgumentException("price too large", e);
        }
    }
}
