package edu.practice.d02.model;

/** Given: a finished subclass. Use it as your model for Annual. */
public class PayAsYouGo extends Membership {

    private final int perVisitCents;

    public PayAsYouGo(long id, String member, int perVisitCents) {
        super(id, member);
        if (perVisitCents <= 0) throw new IllegalArgumentException("perVisitCents must be positive");
        this.perVisitCents = perVisitCents;
    }

    public int getPerVisitCents() { return perVisitCents; }

    @Override
    public int monthlyFeeCents(int visits) {
        if (visits < 0) throw new IllegalArgumentException("visits cannot be negative");
        return Math.multiplyExact(visits, perVisitCents);
    }
}
