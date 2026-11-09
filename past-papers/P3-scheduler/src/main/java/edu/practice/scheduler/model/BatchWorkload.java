package edu.practice.scheduler.model;

/** An ordinary batch job: its urgency grows ("ages") the longer it waits after release. */
public class BatchWorkload extends Workload {
    private static final int BASE_URGENCY = 100;
    private static final int AGEING_PER_TICK = 2;

    public BatchWorkload(long id, int cpuCores, int memoryMb, int durationTicks, int releaseTick) {
        super(id, cpuCores, memoryMb, durationTicks, releaseTick);
    }

    /** 100 + 2 x max(0, nowTick - releaseTick), in long so it can't wrap; toIntExact rejects overflow. */
    @Override
    public int urgencyAt(int nowTick) {
        if (nowTick < 0) {
            throw new IllegalArgumentException("nowTick must not be negative");
        }
        long waited = Math.max(0L, (long) nowTick - getReleaseTick());
        return Math.toIntExact(BASE_URGENCY + AGEING_PER_TICK * waited);
    }

    /** No deadline: it may start at any tick. */
    @Override
    public int latestStartTick() {
        return Integer.MAX_VALUE;
    }
}
