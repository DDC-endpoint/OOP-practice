package marking;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * Reads a source file of YOUR project (path relative to the project root) so a test can
 * check a rule the paper states about the code itself, e.g. "Do not use instanceof".
 * Comments and import lines are ignored, so writing "no instanceof here" in a comment is fine.
 */
final class SourceScan {

    private SourceScan() { }

    /** The file's code with comments and import lines removed. */
    static String code(String relativePath) {
        Path p = Paths.get(relativePath);
        if (!Files.isRegularFile(p)) {
            fail("Expected source file " + relativePath + " (relative to the project root)");
        }
        try {
            String src = Files.readString(p);
            src = src.replaceAll("(?s)/\\*.*?\\*/", " ");      // block comments
            src = src.replaceAll("//[^\\n]*", " ");             // line comments
            src = src.replaceAll("(?m)^\\s*import\\s+[^;]+;", " ");
            return src;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Fails if the file contains a concrete-type check:
     *   - instanceof
     *   - getClass() used for anything except a display label: getClass().getSimpleName()
     *   - a cast to, a .class literal of, or a string literal naming one of the concrete classes
     *     (catches switch/equals on the class name).
     */
    static void assertNoTypeChecks(String relativePath, String... concreteClasses) {
        String code = code(relativePath);
        if (Pattern.compile("\\binstanceof\\b").matcher(code).find()) {
            fail(relativePath + " uses instanceof. The paper forbids concrete type checks: call the overridden method through the parent reference instead.");
        }
        Matcher gc = Pattern.compile("getClass\\s*\\(\\s*\\)").matcher(code);
        while (gc.find()) {
            String after = code.substring(gc.end());
            boolean labelOnly = after.matches("(?s)^\\s*\\.\\s*getSimpleName\\s*\\(\\s*\\)\\s*[^.\\s].*");
            if (!labelOnly) {
                fail(relativePath + " uses getClass() to inspect the concrete type. The paper forbids concrete type checks.");
            }
        }
        for (String type : concreteClasses) {
            if (Pattern.compile("\\(\\s*" + type + "\\s*\\)\\s*\\w").matcher(code).find()
                    || Pattern.compile("\\b" + type + "\\s*\\.\\s*class\\b").matcher(code).find()
                    || code.contains("\"" + type + "\"")) {
                fail(relativePath + " branches on the concrete type " + type
                        + " (cast, ." + "class or a \"" + type + "\" string). Use polymorphism instead.");
            }
        }
    }

    /** Fails if the file's code mentions any of the tokens (used for "the controller delegates to the service"). */
    static void assertNotMentioned(String relativePath, String why, String... tokens) {
        String code = code(relativePath);
        for (String token : tokens) {
            if (code.contains(token)) {
                fail(relativePath + " contains '" + token + "'. " + why);
            }
        }
    }
}
