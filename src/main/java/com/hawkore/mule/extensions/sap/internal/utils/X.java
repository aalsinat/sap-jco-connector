/*
 * HAWKORE CONFIDENTIAL
 * ____________________
 *
 * 2019 (c) HAWKORE, S.L. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains
 * the property of HAWKORE, S.L and its suppliers,
 * if any. The intellectual and technical concepts contained
 * herein are proprietary to HAWKORE, S.L. and its suppliers
 * and may be covered by OEPM or EPO, and are protected
 * by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from HAWKORE, S.L.
 */
package com.hawkore.mule.extensions.sap.internal.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.hawkore.mule.extensions.sap.internal.exceptions.InvalidOperationParamException;
import com.hawkore.mule.extensions.sap.internal.exceptions.OperationExecutionException;
import com.hawkore.mule.extensions.sap.internal.exceptions.ResourceNotFoundException;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

/**
 * Defines global scope.
 * <p>
 * Contains often used utility functions allowing to cut down on code bloat. This
 * is somewhat analogous to {@code Predef} in Scala. Note that this should only be used
 * when this typedef <b>does not sacrifice</b> the code readability.
 *
 * @author Alex Alsina de la Torre (alex.alsina@mango.com)
 */
public final class X {

    /** An empty immutable {@code Object} array. */
    public static final Object[] EMPTY_OBJECT_ARRAY = new Object[0];
    /** The names of methods commonly used to access a wrapped exception. */
    private static final String[] CAUSE_MTD_NAMES = new String[] {"getCause", "getNextException", "getTargetException",
        "getException", "getSourceException", "getRootCause", "getCausedByException", "getNested", "getLinkedException",
        "getNestedException", "getLinkedCause", "getThrowable"};
    /** The Method object for Java 1.4 getCause. */
    private static final Method THROWABLE_CAUSE_METHOD;
    /**
     * The Err parameter must be gt 0.
     */
    static final String ERR_PARAMETER_MUST_BE_GT0 = "Parameter must be > 0";

    static {
        Method causeMtd;

        try {
            causeMtd = Throwable.class.getMethod("getCause", null);
        } catch (Exception ignored) {
            causeMtd = null;
        }

        THROWABLE_CAUSE_METHOD = causeMtd;
    }

    /**
     * Ensures singleton.
     */
    private X() {
        // No-op.
    }

    /**
     * Idx value t.
     *
     * @param <T>
     *     the type parameter
     * @param c
     *     the c
     * @param index
     *     the index
     * @return the t
     */
    public static <T> T idxValue(@Nullable T[] c, int index) {
        if (c == null || index > c.length - 1) {
            return null;
        }
        return c[index];
    }

    /**
     * Tests if given string is {@code null} or empty.
     *
     * @param s
     *     String to test.
     * @return Whether or not the given string is {@code null} or empty.
     */
    public static boolean isEmpty(@Nullable String s) {
        return s == null || s.isEmpty();
    }

    /**
     * Tests if the given array is either {@code null} or empty.
     *
     * @param <T>
     *     the type parameter
     * @param c
     *     Array to test.
     * @return Whether or not the given array is {@code null} or empty.
     */
    public static <T> boolean isEmpty(@Nullable T[] c) {
        return c == null || c.length == 0;
    }

    /**
     * Tests if the given array is {@code null}, empty or contains only {@code null} values.
     *
     * @param <T>
     *     the type parameter
     * @param c
     *     Array to test.
     * @return Whether or not the given array is {@code null}, empty or contains only {@code null} values.
     */
    public static <T> boolean isEmptyOrNulls(@Nullable T[] c) {
        if (isEmpty(c)) {
            return true;
        }

        for (T element : c) {
            if (element != null) {
                return false;
            }
        }

        return true;
    }

    /**
     * Tests if the given array is either {@code null} or empty.
     *
     * @param c
     *     Array to test.
     * @return Whether or not the given array is {@code null} or empty.
     */
    public static boolean isEmpty(@Nullable int[] c) {
        return c == null || c.length == 0;
    }

    /**
     * Tests if the given array is either {@code null} or empty.
     *
     * @param c
     *     Array to test.
     * @return Whether or not the given array is {@code null} or empty.
     */
    public static boolean isEmpty(@Nullable byte[] c) {
        return c == null || c.length == 0;
    }

    /**
     * Tests if the given array is either {@code null} or empty.
     *
     * @param c
     *     Array to test.
     * @return Whether or not the given array is {@code null} or empty.
     */
    public static boolean isEmpty(@Nullable long[] c) {
        return c == null || c.length == 0;
    }

    /**
     * Tests if the given collection is either {@code null} or empty.
     *
     * @param c
     *     Collection to test.
     * @return Whether or not the given collection is {@code null} or empty.
     */
    public static boolean isEmpty(@Nullable Iterable<?> c) {
        return c == null || (c instanceof Collection<?> ? ((Collection<?>)c).isEmpty() : !c.iterator().hasNext());
    }

    /**
     * Tests if the given collection is either {@code null} or empty.
     *
     * @param c
     *     Collection to test.
     * @return Whether or not the given collection is {@code null} or empty.
     */
    public static boolean isEmpty(@Nullable Collection<?> c) {
        return c == null || c.isEmpty();
    }

    /**
     * Tests if the given map is either {@code null} or empty.
     *
     * @param m
     *     Map to test.
     * @return Whether or not the given collection is {@code null} or empty.
     */
    public static boolean isEmpty(@Nullable Map<?, ?> m) {
        return m == null || m.isEmpty();
    }

    /**
     * Checks if passed in {@code 'Throwable'} has given class in {@code 'cause'} hierarchy
     * <b>including</b> that throwable itself.
     * <p>
     * Note that this method follows includes {@link Throwable#getSuppressed()}
     * into check.
     *
     * @param t
     *     Throwable to check (if {@code null}, {@code false} is returned).
     * @param cls
     *     Cause classes to check (if {@code null} or empty, {@code false} is returned).
     * @return {@code True} if one of the causing exception is an instance of passed in classes,     {@code false}
     *     otherwise.
     */
    @SafeVarargs
    public static boolean hasCause(@Nullable Throwable t, @Nullable Class<?>... cls) {
        if (t == null || isEmpty(cls)) {
            return false;
        }

        assert cls != null;

        for (Throwable th = t; th != null; th = th.getCause()) {
            for (Class<?> c : cls) {
                if (c.isAssignableFrom(th.getClass())) {
                    return true;
                }
            }

            for (Throwable n : th.getSuppressed()) {
                if (hasCause(n, cls)) {
                    return true;
                }
            }

            if (th.getCause() == th) {
                break;
            }
        }

        return false;
    }

    /**
     * Checks if passed throwable has given class in one of the suppressed exceptions.
     *
     * @param t
     *     Throwable to check (if {@code null}, {@code false} is returned).
     * @param cls
     *     Class to check.
     * @return {@code True} if one of the suppressed exceptions is an instance of passed class,     {@code false}
     *     otherwise.
     */
    public static boolean hasSuppressed(@Nullable Throwable t, @Nullable Class<? extends Throwable> cls) {
        if (t == null || cls == null) {
            return false;
        }

        for (Throwable th : t.getSuppressed()) {
            if (cls.isAssignableFrom(th.getClass())) {
                return true;
            }

            if (hasSuppressed(th, cls)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Gets first cause if passed in {@code 'Throwable'} has given class in {@code 'cause'} hierarchy.
     * <p>
     * Note that this method follows includes {@link Throwable#getSuppressed()}
     * into check.
     *
     * @param <T>
     *     the type parameter
     * @param t
     *     Throwable to check (if {@code null}, {@code null} is returned).
     * @param cls
     *     Cause class to get cause (if {@code null}, {@code null} is returned).
     * @return First causing exception of passed in class, {@code null} otherwise.
     */
    @SuppressWarnings({"unchecked"})
    @Nullable
    public static <T extends Throwable> T cause(@Nullable Throwable t, @Nullable Class<T> cls) {
        if (t == null || cls == null) {
            return null;
        }

        for (Throwable th = t; th != null; th = th.getCause()) {
            if (cls.isAssignableFrom(th.getClass())) {
                return (T)th;
            }

            for (Throwable n : th.getSuppressed()) {
                T found = cause(n, cls);

                if (found != null) {
                    return found;
                }
            }

            if (th.getCause() == th) {
                break;
            }
        }

        return null;
    }

    /**
     * @param throwable
     *     The exception to examine.
     * @return The wrapped exception, or {@code null} if not found.
     */
    private static Throwable getCauseUsingWellKnownTypes(Throwable throwable) {
        if (throwable instanceof SQLException) {
            return ((SQLException)throwable).getNextException();
        }

        if (throwable instanceof InvocationTargetException) {
            return ((InvocationTargetException)throwable).getTargetException();
        }

        return null;
    }

    /**
     * Finds a {@code Throwable} by method name.
     *
     * @param throwable
     *     The exception to examine.
     * @param mtdName
     *     The name of the method to find and invoke.
     * @return The wrapped exception, or {@code null} if not found.
     */
    private static Throwable getCauseUsingMethodName(Throwable throwable, String mtdName) {
        Method mtd = null;

        try {
            mtd = throwable.getClass().getMethod(mtdName, null);
        } catch (NoSuchMethodException | SecurityException ignored) {
            // exception ignored
        }

        if (mtd != null && Throwable.class.isAssignableFrom(mtd.getReturnType())) {
            try {
                return (Throwable)mtd.invoke(throwable, EMPTY_OBJECT_ARRAY);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException ignored) {
                // exception ignored
            }
        }

        return null;
    }

    /**
     * Finds a {@code Throwable} by field name.
     *
     * @param throwable
     *     The exception to examine.
     * @param fieldName
     *     The name of the attribute to examine.
     * @return The wrapped exception, or {@code null} if not found.
     */
    private static Throwable getCauseUsingFieldName(Throwable throwable, String fieldName) {
        Field field = null;

        try {
            field = throwable.getClass().getField(fieldName);
        } catch (NoSuchFieldException | SecurityException ignored) {
            // exception ignored
        }

        if (field != null && Throwable.class.isAssignableFrom(field.getType())) {
            try {
                return (Throwable)field.get(throwable);
            } catch (IllegalAccessException | IllegalArgumentException ignored) {
                // exception ignored
            }
        }

        return null;
    }

    /**
     * Checks if the Throwable class has a {@code getCause} method.
     * <p>
     * This is true for JDK 1.4 and above.
     *
     * @return True if Throwable is nestable.
     */
    public static boolean isThrowableNested() {
        return THROWABLE_CAUSE_METHOD != null;
    }

    /**
     * Checks whether this {@code Throwable} class can store a cause.
     * <p>
     * This method does not check whether it actually does store a cause.
     *
     * @param throwable
     *     The {@code Throwable} to examine, may be null.
     * @return Boolean {@code true} if nested otherwise {@code false}.
     */
    public static boolean isNestedThrowable(Throwable throwable) {
        if (throwable == null) {
            return false;
        }

        if (throwable instanceof SQLException || throwable instanceof InvocationTargetException) {
            return true;
        }

        if (isThrowableNested()) {
            return true;
        }

        Class<?> cls = throwable.getClass();
        for (String CAUSE_MTD_NAME : CAUSE_MTD_NAMES) {
            try {
                Method mtd = cls.getMethod(CAUSE_MTD_NAME, null);

                if (mtd != null && Throwable.class.isAssignableFrom(mtd.getReturnType())) {
                    return true;
                }
            } catch (NoSuchMethodException | SecurityException ignored) {
                // exception ignored
            }
        }

        try {
            Field field = cls.getField("detail");

            if (field != null) {
                return true;
            }
        } catch (NoSuchFieldException | SecurityException ignored) {
            // exception ignored
        }

        return false;
    }

    /**
     * Introspects the {@code Throwable} to obtain the cause.
     * <p>
     * The method searches for methods with specific names that return a {@code Throwable} object.
     * This will pick up most wrapping exceptions, including those from JDK 1.4.
     * <p>
     * The default list searched for are:</p> <ul> <li>{@code getCause()}</li>
     * <li>{@code getNextException()}</li> <li>{@code getTargetException()}</li>
     * <li>{@code getException()}</li> <li>{@code getSourceException()}</li>
     * <li>{@code getRootCause()}</li> <li>{@code getCausedByException()}</li>
     * <li>{@code getNested()}</li> </ul>
     *
     * <p>In the absence of any such method, the object is inspected for a {@code detail}
     * field assignable to a {@code Throwable}.</p>
     * <p>
     * If none of the above is found, returns {@code null}.
     *
     * @param throwable
     *     The throwable to introspect for a cause, may be null.
     * @return The cause of the {@code Throwable},     {@code null} if none found or null throwable input.
     */
    public static Throwable getCause(Throwable throwable) {
        return getCause(throwable, CAUSE_MTD_NAMES);
    }

    /**
     * Introspects the {@code Throwable} to obtain the cause.
     *
     * <ol> <li>Try known exception types.</li>
     * <li>Try the supplied array of method names.</li>
     * <li>Try the field 'detail'.</li> </ol>
     *
     * <p>A {@code null} set of method names means use the default set.
     * A {@code null} in the set of method names will be ignored.</p>
     *
     * @param throwable
     *     The throwable to introspect for a cause, may be null.
     * @param mtdNames
     *     The method names, null treated as default set.
     * @return The cause of the {@code Throwable}, {@code null} if none found or null throwable input.
     */
    public static Throwable getCause(Throwable throwable, String[] mtdNames) {
        if (throwable == null) {
            return null;
        }

        Throwable cause = getCauseUsingWellKnownTypes(throwable);

        if (cause == null) {
            if (mtdNames == null) {
                mtdNames = CAUSE_MTD_NAMES;
            }

            for (String mtdName : mtdNames) {
                if (mtdName != null) {
                    cause = getCauseUsingMethodName(throwable, mtdName);

                    if (cause != null) {
                        break;
                    }
                }
            }

            if (cause == null) {
                cause = getCauseUsingFieldName(throwable, "detail");
            }
        }

        return cause;
    }

    /**
     * Returns the list of {@code Throwable} objects in the exception chain.
     * <p>
     * A throwable without cause will return a list containing one element - the input throwable.
     * A throwable with one cause will return a list containing two elements - the input throwable
     * and the cause throwable. A {@code null} throwable will return a list of size zero.
     * <p>
     * This method handles recursive cause structures that might otherwise cause infinite loops.
     * The cause chain is processed until the end is reached, or until the next item in the chain
     * is already in the result set.</p>
     *
     * @param throwable
     *     The throwable to inspect, may be null.
     * @return The list of throwables, never null.
     */
    public static List<Throwable> getThrowableList(Throwable throwable) {
        List<Throwable> list = new ArrayList<>();

        while (throwable != null && !list.contains(throwable)) {
            list.add(throwable);
            throwable = getCause(throwable);
        }

        return list;
    }

    /**
     * Returns the list of {@code Throwable} objects in the exception chain.
     * <p>
     * A throwable without cause will return an array containing one element - the input throwable.
     * A throwable with one cause will return an array containing two elements - the input throwable
     * and the cause throwable. A {@code null} throwable will return an array of size zero.
     *
     * @param throwable
     *     The throwable to inspect, may be null.
     * @return The array of throwables, never null.
     */
    public static Throwable[] getThrowables(Throwable throwable) {
        List<Throwable> list = getThrowableList(throwable);

        return list.toArray(new Throwable[list.size()]);
    }

    /**
     * Collects suppressed exceptions from throwable and all it causes.
     *
     * @param t
     *     Throwable.
     * @return List of suppressed throwables.
     */
    public static List<Throwable> getSuppressedList(@Nullable Throwable t) {
        List<Throwable> result = new ArrayList<>();

        if (t == null) {
            return result;
        }

        do {
            for (Throwable suppressed : t.getSuppressed()) {
                result.add(suppressed);

                result.addAll(getSuppressedList(suppressed));
            }
        } while ((t = t.getCause()) != null);

        return result;
    }

    /**
     * A way to get the entire nested stack-trace of an throwable.
     * <p>
     * The result of this method is highly dependent on the JDK version
     * and whether the exceptions override printStackTrace or not.
     *
     * @param throwable
     *     The {@code Throwable} to be examined.
     * @return The nested stack trace, with the root cause first.
     */
    public static String getFullStackTrace(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        Throwable[] ts = getThrowables(throwable);

        for (Throwable t : ts) {
            t.printStackTrace(pw);

            if (isNestedThrowable(t)) {
                break;
            }
        }

        return sw.getBuffer().toString();
    }

    /**
     * Copies input byte stream to output byte stream.
     *
     * @param in
     *     Input byte stream.
     * @param out
     *     Output byte stream.
     * @param bufSize
     *     Intermediate buffer size.
     * @return Number of the copied bytes.
     * @throws IOException
     *     Thrown if an I/O error occurs.
     */
    public static int copy(InputStream in, OutputStream out, int bufSize) throws IOException {
        byte[] buf = new byte[bufSize];

        int cnt = 0;

        for (int n; (n = in.read(buf)) > 0; ) {
            out.write(buf, 0, n);

            cnt += n;
        }

        return cnt;
    }

    /**
     * Parses double from possibly {@code null} or invalid string.
     *
     * @param s
     *     String to parse double from. If string is null or invalid, a default value is used.
     * @param dflt
     *     Default value for double, if parsing failed.
     * @return Resulting double.
     */
    public static double parseDouble(@Nullable String s, double dflt) {
        try {
            return s != null ? Double.parseDouble(s) : dflt;
        } catch (NumberFormatException ignored) {
            return dflt;
        }
    }

    /**
     * Verify gt 0 param.
     *
     * @param l
     *     the l
     * @param paramName
     *     the param name
     * @param methodName
     *     the method name
     * @param logger
     *     the logger
     */
    static void verifyGt0Param(final long l, final String paramName, final String methodName, final Logger logger) {
        if (l < 1) {
            logger.error("[{}] Argument {} invalid: {}", methodName, paramName, ERR_PARAMETER_MUST_BE_GT0);
            throw new InvalidOperationParamException(
                "Invalid argument '" + paramName + "' at '" + methodName + "': " + ERR_PARAMETER_MUST_BE_GT0);
        }
    }

    /**
     * Verify not null param.
     *
     * @param value
     *     the value
     * @param paramName
     *     the param name
     * @param methodName
     *     the method name
     * @param logger
     *     the logger
     */
    static void verifyNotNullParam(final Object value,
        final String paramName,
        final String methodName,
        final Logger logger) {
        if (value == null) {
            logger.error("[{}] Argument {} can not be null", methodName, paramName);
            throw new InvalidOperationParamException("null " + paramName + " at " + methodName);
        }
    }

    /**
     * Verify not empty param.
     *
     * @param value
     *     the value
     * @param paramName
     *     the param name
     * @param methodName
     *     the method name
     * @param logger
     *     the logger
     */
    static void verifyNotEmptyParam(final Map<?, ?> value,
        final String paramName,
        final String methodName,
        final Logger logger) {
        if (value == null || value.isEmpty()) {
            logger.error("[{}] Argument {} can not be null or empty", methodName, paramName);
            throw new InvalidOperationParamException("null or empty " + paramName + " at " + methodName);
        }
    }

    /**
     * Invalid param t.
     *
     * @param <T>
     *     the type parameter
     * @param methodName
     *     the method name
     * @param description
     *     the description
     * @param e
     *     the e
     * @param logger
     *     the logger
     * @return the t
     */
    static <T> T invalidParam(final String methodName,
        final String description,
        final Exception e,
        final Logger logger) {
        logger.error("[{}] {}", methodName, description);
        throw new InvalidOperationParamException(description, e);
    }

    /**
     * Checks is object is null
     *
     * @param <T>
     *     the type parameter
     * @param obj
     *     the object
     * @param methodName
     *     the invoker method name
     * @param entity
     *     the entity name
     * @param id
     *     the object id
     * @param e
     *     the main exception
     * @param logger
     *     the logger
     * @return the obj parameter
     */
    public static <T> T verifyEntityNotFound(final T obj,
        final String methodName,
        final String entity,
        final String id,
        final Exception e,
        final Logger logger) {
        if (obj == null) {
            final String msg = entity + " not found with id '" + id + "' at " + methodName;
            logger.error("[{}] {}", methodName, msg);
            throw new ResourceNotFoundException(msg, e);
        } else {
            return obj;
        }
    }

    /**
     * Throw an exception with information about operation failure
     *
     * @param <T>
     *     the type parameter
     * @param operation
     *     the operation
     * @param description
     *     opcional, if not present then e.getMessage() will be used
     * @param e
     *     the e
     * @param logger
     *     the logger
     * @return t t
     */
    static <T> T operationFailure(final String operation,
        final String description,
        final Exception e,
        final Logger logger) {
        String desc;
        if (description == null && e != null) {
            desc = e.getMessage();
        } else {
            desc = description;
        }
        desc = "Operation '" + operation + "' failed: " + desc;
        logger.error("[{}] {}", operation, desc);
        throw new OperationExecutionException(desc, e);
    }

}
