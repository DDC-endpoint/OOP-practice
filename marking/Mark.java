import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.regex.*;
import java.util.stream.*;

/**
 * Marks a practice-paper attempt like the LBP mark sheet.
 *
 * Usage (from the repo root):  java marking/Mark.java <paper-id> <projectDir>
 *   e.g.  java marking/Mark.java p1 past-papers/P1-parking
 * Or use the wrappers:  marking/mark.sh p1 past-papers/P1-parking   |   marking\mark.cmd p1 past-papers\P1-parking
 *
 * It does four things:
 *   1. copies marking/<paper-id>/src/test/java/marking/** into <projectDir>/src/test/java/marking/
 *   2. runs the project's Maven wrapper: mvnw test
 *   3. reads target/surefire-reports
 *   4. prints marks per task (18/22/20/20 + Section B 20 = 100)
 *
 * Marks come from test method names ending in _m<N>  (e.g. t1_ceilingHours_m6).
 */
public class Mark {

    static final LinkedHashMap<String, Integer> ALLOCATION = new LinkedHashMap<>();
    static {
        ALLOCATION.put("Task1Test", 18);
        ALLOCATION.put("Task2Test", 22);
        ALLOCATION.put("Task3Test", 20);
        ALLOCATION.put("Task4Test", 20);
        ALLOCATION.put("SectionBTest", 20);
    }
    static final Pattern MARKED = Pattern.compile("void\\s+(\\w+_m(\\d+))\\s*\\(");
    static final Pattern NAME_MARKS = Pattern.compile("_m(\\d+)(\\(.*\\))?$");

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.err.println("Usage: java marking/Mark.java <paper-id> <projectDir>   (paper-id: m1 | p1 | p2 | p3)");
            System.exit(64);
        }
        String id = args[0].toLowerCase(Locale.ROOT);
        Path home = Paths.get(System.getProperty("marking.home", "marking")).toAbsolutePath().normalize();
        Path source = home.resolve(id).resolve("src/test/java/marking");
        Path project = Paths.get(args[1]).toAbsolutePath().normalize();

        if (!Files.isDirectory(source)) fatal("No marking tests for '" + id + "' at " + source);
        if (!Files.isRegularFile(project.resolve("pom.xml"))) fatal("Not a Maven project (no pom.xml): " + project);

        // 0. Check that the marking tests add up exactly to the paper's allocation.
        Map<String, Integer> declared = declaredMarks(source);
        boolean bad = false;
        for (var e : ALLOCATION.entrySet()) {
            int got = declared.getOrDefault(e.getKey(), 0);
            if (got != e.getValue()) {
                System.err.printf("MARKING ERROR: %s declares %d marks, expected %d%n", e.getKey(), got, e.getValue());
                bad = true;
            }
        }
        if (bad) System.exit(2);

        // 1. Copy marking tests in (replacing any previous copy).
        Path target = project.resolve("src/test/java/marking");
        deleteTree(target);
        copyTree(source, target);
        deleteTree(project.resolve("target/surefire-reports"));

        // 2. Run the tests through the project's own Maven wrapper.
        System.out.println("Marking " + id.toUpperCase(Locale.ROOT) + " in " + project);
        System.out.println("Running tests (the first run downloads Maven and dependencies, so it can take a few minutes)...");
        List<String> log = runMaven(project);

        // 3. Read the reports.
        Path reports = project.resolve("target/surefire-reports");
        Map<String, Boolean> results = readReports(reports);
        if (results.isEmpty()) {
            System.out.println();
            System.out.println("COMPILE FAILED (or no tests ran) -> 0 / 100");
            List<String> errors = log.stream().filter(l -> l.contains("[ERROR]") && l.contains(".java")).limit(15).toList();
            (errors.isEmpty() ? log.stream().filter(l -> l.contains("[ERROR]")).limit(15).toList() : errors)
                    .forEach(System.out::println);
            System.out.println("Tip: in the real exam a project that doesn't compile can't pass Section B. Keep it compiling.");
            System.exit(1);
        }

        // 4. Print the mark sheet.
        printSheet(declared, results, source);
    }

    static Map<String, Integer> declaredMarks(Path source) throws IOException {
        Map<String, Integer> totals = new HashMap<>();
        try (Stream<Path> files = Files.list(source)) {
            for (Path f : files.filter(p -> p.toString().endsWith("Test.java")).toList()) {
                String cls = f.getFileName().toString().replace(".java", "");
                Matcher m = MARKED.matcher(Files.readString(f));
                while (m.find()) totals.merge(cls, Integer.parseInt(m.group(2)), Integer::sum);
            }
        }
        return totals;
    }

    static List<String> runMaven(Path project) throws Exception {
        boolean windows = System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("win");
        Path wrapper = project.resolve(windows ? "mvnw.cmd" : "mvnw");
        if (!Files.exists(wrapper)) fatal("No Maven wrapper in " + project + " (expected " + wrapper.getFileName() + ")");
        List<String> cmd = new ArrayList<>();
        if (windows) { cmd.add("cmd.exe"); cmd.add("/c"); cmd.add(wrapper.toString()); }
        else { wrapper.toFile().setExecutable(true); cmd.add(wrapper.toString()); }
        cmd.addAll(List.of("-B", "test", "-Dmaven.test.failure.ignore=true", "-Dsurefire.printSummary=false"));
        Process p = new ProcessBuilder(cmd).directory(project.toFile()).redirectErrorStream(true).start();
        List<String> lines = new ArrayList<>();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = r.readLine()) != null) {
                lines.add(line);
                if (line.contains("Downloading from") || line.contains("Downloaded from")) continue;
                if (line.startsWith("[ERROR] COMPILATION") || line.contains("BUILD")) System.out.println("  " + line);
            }
        }
        p.waitFor();
        return lines;
    }

    /** name -> passed, for marking.* test classes only. */
    static Map<String, Boolean> readReports(Path reports) throws IOException {
        Map<String, Boolean> out = new LinkedHashMap<>();
        if (!Files.isDirectory(reports)) return out;
        Pattern testcase = Pattern.compile("<testcase\\s+([^>]*?)(/>|>(.*?)</testcase>)", Pattern.DOTALL);
        Pattern attr = Pattern.compile("(\\w+)=\"([^\"]*)\"");
        try (Stream<Path> files = Files.list(reports)) {
            for (Path f : files.filter(p -> p.getFileName().toString().matches("TEST-marking\\..*\\.xml")).sorted().toList()) {
                Matcher m = testcase.matcher(Files.readString(f, StandardCharsets.UTF_8));
                while (m.find()) {
                    Map<String, String> a = new HashMap<>();
                    Matcher am = attr.matcher(m.group(1));
                    while (am.find()) a.put(am.group(1), am.group(2));
                    String body = m.group(3) == null ? "" : m.group(3);
                    boolean passed = !(body.contains("<failure") || body.contains("<error") || body.contains("<skipped"));
                    String cls = a.getOrDefault("classname", "").replace("marking.", "");
                    out.put(cls + "#" + a.getOrDefault("name", "?"), passed);
                }
            }
        }
        return out;
    }

    static void printSheet(Map<String, Integer> declared, Map<String, Boolean> results, Path source) throws IOException {
        int total = 0;
        System.out.println();
        for (var e : ALLOCATION.entrySet()) {
            String cls = e.getKey();
            List<String> names = new ArrayList<>();
            Matcher m = MARKED.matcher(Files.readString(source.resolve(cls + ".java")));
            while (m.find()) names.add(m.group(1));
            int earned = 0;
            String title = cls.equals("SectionBTest") ? "Section B" : cls.replace("Test", "").replace("Task", "Task ");
            StringBuilder lines = new StringBuilder();
            for (String n : names) {
                Matcher nm = NAME_MARKS.matcher(n);
                int marks = nm.find() ? Integer.parseInt(nm.group(1)) : 0;
                Boolean ok = lookup(results, cls, n);
                boolean pass = Boolean.TRUE.equals(ok);
                if (pass) earned += marks;
                lines.append(String.format("   %s %2d  %s%s%n", pass ? "[x]" : "[ ]", marks, pretty(n), ok == null ? "   (did not run)" : ""));
            }
            total += earned;
            System.out.printf("%-10s %3d / %d%n", title, earned, e.getValue());
            System.out.print(lines);
        }
        System.out.println("------------------------");
        System.out.printf("TOTAL      %3d / 100%n", total);
        System.out.println("(An estimate from the paper's stated rules. Staff marking and the viva can differ.)");
    }

    static Boolean lookup(Map<String, Boolean> results, String cls, String method) {
        for (var e : results.entrySet()) {
            String key = e.getKey();
            if (key.startsWith(cls + "#") && key.substring(cls.length() + 1).replaceAll("\\(.*\\)$", "").equals(method)) return e.getValue();
        }
        return null;
    }

    static String pretty(String method) {
        return method.replaceAll("_m\\d+$", "").replaceFirst("^[a-z]\\d_", "").replace('_', ' ');
    }

    static void copyTree(Path from, Path to) throws IOException {
        try (Stream<Path> s = Files.walk(from)) {
            for (Path p : s.toList()) {
                Path dest = to.resolve(from.relativize(p).toString());
                if (Files.isDirectory(p)) Files.createDirectories(dest);
                else Files.copy(p, dest, StandardCopyOption.REPLACE_EXISTING);
            }
        }
    }

    static void deleteTree(Path p) throws IOException {
        if (!Files.exists(p)) return;
        try (Stream<Path> s = Files.walk(p)) {
            for (Path x : s.sorted(Comparator.reverseOrder()).toList()) Files.delete(x);
        }
    }

    static void fatal(String msg) {
        System.err.println(msg);
        System.exit(1);
    }
}
