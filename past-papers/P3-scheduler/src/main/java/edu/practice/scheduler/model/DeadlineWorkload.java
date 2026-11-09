package edu.practice.scheduler.model;

/** A job that must finish by deadlineTick: the less slack it has left, the more urgent it is. */
public class DeadlineWorkload extends Workload {
    private static final int MAX_URGENCY = 1000;
    private static final int PENALTY_PER_SLACK_TICK = 5;

    private final int deadlineTick;

    public DeadlineWorkload(long id, int cpuCores, int memoryMb, int durationTicks, int releaseTick, int deadlineTick) {
        super(id, cpuCores, memoryMb, durationTicks, releaseTick);
        this.deadlineTick = deadlineTick;
    }

    public int getDeadlineTick() {
        return deadlineTick;
    }

    /** 1000 - 5 x max(0, deadlineTick - nowTick - durationTicks), computed in long; toIntExact rejects overflow. */
    @Override
    public int urgencyAt(int nowTick) {
        if (nowTick < 0) {
            throw new IllegalArgumentException("nowTick must not be negative");
        }
        long slack = Math.max(0L, (long) deadlineTick - nowTick - getDurationTicks());
        return Math.toIntExact(MAX_URGENCY - PENALTY_PER_SLACK_TICK * slack);
    }

    /** The last tick it can start and still end by the deadline. */
    @Override
    public int latestStartTick() {
        return Math.toIntExact((long) deadlineTick - getDurationTicks());
    }
}
