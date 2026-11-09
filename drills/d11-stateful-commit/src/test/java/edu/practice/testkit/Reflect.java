package edu.practice.testkit;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * Test helper for classes YOU have to create.
 *
 * Tests reach a class you haven't written yet through this helper rather than by name,
 * so everything still compiles before the class exists. A missing class only fails
 * the tests that need it, and the message tells you exactly what to create.
 */
public final class Reflect {

    private static final Map<Class<?>, Class<?>> BOXES = Map.of(
            int.class, Integer.class, long.class, Long.class, double.class, Double.class,
            boolean.class, Boolean.class, float.class, Float.class, short.class, Short.class,
            byte.class, Byte.class, char.class, Character.class);

    private Reflect() { }

    /** Loads a class, or fails with "Create class X in package Y". */
    public static Class<?> require(String fqcn) {
        try {
            return Class.forName(fqcn);
        } catch (ClassNotFoundException e) {
            int dot = fqcn.lastIndexOf('.');
            fail("Create class " + fqcn.substring(dot + 1) + " in package " + fqcn.substring(0, dot)
                    + " (file: src/main/java/" + fqcn.replace('.', '/') + ".java)");
            return null;
        }
    }

    /** Calls a public constructor that matches the arguments. */
    public static Object construct(String fqcn, Object... args) {
        Class<?> type = require(fqcn);
        for (Constructor<?> c : type.getConstructors()) {
            if (matches(c.getParameterTypes(), args)) {
                return unwrap(() -> c.newInstance(args));
            }
        }
        fail(type.getSimpleName() + " needs a public constructor taking " + describe(args));
        return null;
    }

    /** Calls a public method (including inherited ones) that matches the arguments. */
    public static Object call(Object target, String method, Object... args) {
        for (Method m : target.getClass().getMethods()) {
            if (m.getName().equals(method) && matches(m.getParameterTypes(), args)) {
                return unwrap(() -> m.invoke(target, args));
            }
        }
        fail(target.getClass().getSimpleName() + " needs a public method " + method + "(" + describe(args) + ")");
        return null;
    }

    /** True if `type` is a subclass of `parent` (e.g. it uses `extends`). */
    public static boolean extendsClass(Class<?> type, Class<?> parent) {
        return parent.isAssignableFrom(type) && type != parent;
    }

    private interface Call { Object run() throws Exception; }

    private static Object unwrap(Call call) {
        try {
            return call.run();
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException re) throw re;
            if (cause instanceof Error err) throw err;
            throw new RuntimeException(cause);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean matches(Class<?>[] params, Object[] args) {
        if (params.length != args.length) return false;
        for (int i = 0; i < params.length; i++) {
            Class<?> p = params[i].isPrimitive() ? BOXES.get(params[i]) : params[i];
            if (args[i] == null) {
                if (params[i].isPrimitive()) return false;
            } else if (!p.isInstance(args[i])) {
                return false;
            }
        }
        return true;
    }

    private static String describe(Object[] args) {
        StringBuilder sb = new StringBuilder();
        for (Object a : args) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(a == null ? "null" : a.getClass().getSimpleName());
        }
        return sb.toString();
    }
}
