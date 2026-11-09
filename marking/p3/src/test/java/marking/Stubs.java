package marking;

import edu.practice.scheduler.model.Workload;
import edu.practice.scheduler.repo.WorkloadRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Test doubles so Task 2-3 service marks don't depend on your Task 1-2 urgency formulas.
 * W has a FIXED urgency and latest start, so a test controls the ranking exactly.
 */
final class Stubs {

    private Stubs() { }

    static final class W extends Workload {
        private final int urgency;
        private final int latestStart;

        W(long id, int cpuCores, int memoryMb, int durationTicks, int releaseTick, int urgency, int latestStart) {
            super(id, cpuCores, memoryMb, durationTicks, releaseTick);
            this.urgency = urgency;
            this.latestStart = latestStart;
        }

        @Override public int urgencyAt(int nowTick) { return urgency; }
        @Override public int latestStartTick() { return latestStart; }
    }

    /** id, cpu, memory, duration, release, urgency; no deadline. */
    static W w(long id, int cpu, int mem, int duration, int release, int urgency) {
        return new W(id, cpu, mem, duration, release, urgency, Integer.MAX_VALUE);
    }

    /** Same, with a latest legal start tick (a deadline). */
    static W wd(long id, int cpu, int mem, int duration, int release, int urgency, int latestStart) {
        return new W(id, cpu, mem, duration, release, urgency, latestStart);
    }

    /** A repository holding exactly the workloads you give it (no seed). Contents can be replaced. */
    static final class StubRepo extends WorkloadRepository {
        private final List<Workload> list = new ArrayList<>();

        StubRepo(Workload... workloads) { set(workloads); }

        void set(Workload... workloads) {
            list.clear();
            list.addAll(List.of(workloads));
        }

        @Override public Collection<Workload> all() { return List.copyOf(list); }
        @Override public Workload find(long id) {
            return list.stream().filter(x -> x.getId() == id).findFirst().orElse(null);
        }
    }
}
