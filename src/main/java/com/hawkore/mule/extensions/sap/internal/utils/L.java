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

import java.lang.invoke.CallSite;
import java.lang.invoke.LambdaConversionException;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * This class simply wraps the functionality of
 * {@link LambdaMetafactory#metafactory(java.lang.invoke.MethodHandles.Lookup,
 * String, MethodType, MethodType, MethodHandle, MethodType)}*****.
 * <p>
 * Defines global scope.
 * <p>
 * Contains often used utility functions allowing to cut down on code bloat. This
 * is somewhat analogous to {@code Predef} in Scala. Note that this should only be used
 * when this typedef <b>does not sacrifice</b> the code readability.
 */
public class L {

    private static Field lookupClassAllowedModesField;
    private static final int ALL_MODES = (MethodHandles.Lookup.PRIVATE | MethodHandles.Lookup.PROTECTED
                                              | MethodHandles.Lookup.PACKAGE | MethodHandles.Lookup.PUBLIC);

    private L() {}

    /**
     * Returns a dynamically generated implementation of the argument provided interface.
     * <p>
     * The provided <b>signatureName</b> must identify a method, whose arguments corresponds to the real <b>method</b>
     * to invoke.
     * <p>
     * If the <b>method</b> is a non-static method the interface method's first parameter must be an
     * Object and the subsequent parameters must match the <b>method</b> ones.
     *
     * <p>
     * Example (static method):
     * <pre>
     * // the real method to invoke
     * Method method = MyClass.class.getDeclaredMethod("myStaticMethod", int.class, int.class);
     * // make accessible if method is private
     * L.makeAccessible(method);
     * // dynamic create a implementation that invokes real "myStaticMethod" using dynamically generated
     * // implementation for MyInterface's "applyAsInt" method
     * MyInterface sam = L.create(method, MyInterface.class, "applyAsInt");
     * // simply call it
     * int result = sam.applyAsInt(3, 11);
     * </pre>
     *
     * <p>Example (NON static method):
     * <pre>
     * // the real method to invoke
     * Method method = MyClass.class.getDeclaredMethod("myNonStaticMethod", int.class, int.class);
     * // make accessible if method is private
     * L.makeAccessible(method);
     * // dynamic create a implementation that invokes real "myNonStaticMethod" using dynamically generated
     * // implementation for MyInterface's "applyAsInt" method
     * MyInterface sam = L.create(method, MyInterface.class, "applyAsInt");
     * // simply call it
     * MyClass myClassInstance = ....
     * int result = sam.applyAsInt(myClassInstance, 3, 11);
     * </pre>
     *
     * @param <T>
     *     the type parameter
     * @param method
     *     A Method object which defines what to invoke.
     * @param interfaceClass
     *     The interface, which the dynamically generated class shall implement.
     * @param signatatureName
     *     The name of an abstract method from the interface, which the dynamically create     class shall implement.
     * @return A dynamically generated implementation of the argument provided interface. The implementation offers
     *     invocation speed similar to that of a direct method invocation.
     * @throws Throwable
     *     the throwable
     */
    public static <T> T create(Method method, Class<T> interfaceClass, String signatatureName) throws Throwable {
        return create(method, interfaceClass, signatatureName, false);
    }

    /**
     * Same as {@link L#create(Method, Class, String)}, but this method returns a Lambda that will <em>not</em> be
     * subject to dynamic method dispatch.
     *
     * @param <T>
     *     the type parameter
     * @param method
     *     the method
     * @param interfaceClass
     *     the interface class
     * @param signatatureName
     *     the signatature name
     * @return the t
     * @throws Throwable
     *     the throwable
     */
    public static <T> T createSpecial(Method method, Class<T> interfaceClass, String signatatureName) throws Throwable {
        return create(method, interfaceClass, signatatureName, true);
    }

    /**
     * Same as {@link L#create(Method, Class, String)}, but with an additional parameter in the form of a Lookup
     * object.
     *
     * @param <T>
     *     the type parameter
     * @param method
     *     the method
     * @param lookup
     *     the lookup
     * @param interfaceClass
     *     the interface class
     * @param signatatureName
     *     the signatature name
     * @return t t
     * @throws Throwable
     *     the throwable
     */
    public static <T> T create(Method method,
        MethodHandles.Lookup lookup,
        Class<T> interfaceClass,
        String signatatureName) throws Throwable {
        return createLambda(method, lookup, interfaceClass, signatatureName, false);
    }

    /**
     * Same as {@link L#createSpecial(Method, Class, String)}, but with an additional parameter in the form of a Lookup
     * object.
     *
     * @param <T>
     *     the type parameter
     * @param method
     *     the method
     * @param lookup
     *     the lookup
     * @param interfaceClass
     *     the interface class
     * @param signatatureName
     *     the signatature name
     * @return the t
     * @throws Throwable
     *     the throwable
     */
    public static <T> T createSpecial(Method method,
        MethodHandles.Lookup lookup,
        Class<T> interfaceClass,
        String signatatureName) throws Throwable {
        return createLambda(method, lookup, interfaceClass, signatatureName, true);
    }

    /**
     * Make private class method accessible
     *
     * @param m
     *     the m
     * @throws Exception
     *     the exception
     */
    public static void makeAccessible(final Method m) throws Exception {
        if (m == null) {
            return;
        }
        PrivilegedAction<Object> p = () -> {
            try {
                m.setAccessible(true);
                return null;
            } catch (Exception e) {
                return e;
            }
        };
        Object o = AccessController.doPrivileged(p);
        if (o != null) {
            throw (Exception)o;
        }
    }

    private static <T> T createLambda(Method method,
        MethodHandles.Lookup lookup,
        Class<T> interfaceClass,
        String signatatureName,
        boolean createSpecial) throws Throwable {
        if (method.isAccessible()) {
            lookup = lookup.in(method.getDeclaringClass());
            setAccessible(lookup);
        }
        return privateCreateLambda(method, lookup, interfaceClass, signatatureName, createSpecial);
    }

    private static <T> T create(Method method, Class<T> interfaceClass, String signatureName, boolean invokeSpecial)
        throws Throwable {
        MethodHandles.Lookup lookup = MethodHandles.lookup().in(method.getDeclaringClass());
        setAccessible(lookup);
        return createLambda(method, lookup, interfaceClass, signatureName, invokeSpecial);
    }

    private static <T> T privateCreateLambda(Method method,
        MethodHandles.Lookup lookup,
        Class<T> interfaceClass,
        String signatureName,
        boolean createSpecial) throws Throwable {
        MethodHandle methodHandle = createSpecial
                                        ? lookup.unreflectSpecial(method, method.getDeclaringClass())
                                        : lookup.unreflect(method);
        MethodType instantiatedMethodType = methodHandle.type();
        MethodType signature = createLambdaMethodType(method, instantiatedMethodType);

        CallSite site = createCallSite(signatureName, lookup, methodHandle, instantiatedMethodType, signature,
            interfaceClass);
        MethodHandle factory = site.getTarget();
        return (T)factory.invoke();
    }

    private static MethodType createLambdaMethodType(Method method, MethodType instantiatedMethodType) {
        boolean isStatic = Modifier.isStatic(method.getModifiers());
        MethodType signature = isStatic
                                   ? instantiatedMethodType
                                   : instantiatedMethodType.changeParameterType(0, Object.class);

        Class<?>[] params = method.getParameterTypes();
        for (int i = 0; i < params.length; i++) {
            if (Object.class.isAssignableFrom(params[i])) {
                signature = signature.changeParameterType(isStatic ? i : i + 1, Object.class);
            }
        }
        if (Object.class.isAssignableFrom(signature.returnType())) {
            signature = signature.changeReturnType(Object.class);
        }

        return signature;
    }

    private static CallSite createCallSite(String signatureName,
        MethodHandles.Lookup lookup,
        MethodHandle methodHandle,
        MethodType instantiatedMethodType,
        MethodType signature,
        Class<?> interfaceClass) throws LambdaConversionException {
        return LambdaMetafactory
                   .metafactory(lookup, signatureName, MethodType.methodType(interfaceClass), signature, methodHandle,
                       instantiatedMethodType);
    }

    private static void setAccessible(MethodHandles.Lookup lookup) throws NoSuchFieldException, IllegalAccessException {
        getLookupsModifiersField().set(lookup, ALL_MODES);
    }

    private static Field getLookupsModifiersField() throws NoSuchFieldException, IllegalAccessException {
        if (lookupClassAllowedModesField == null || !lookupClassAllowedModesField.isAccessible()) {
            Field modifiersField = Field.class.getDeclaredField("modifiers");
            modifiersField.setAccessible(true);
            Field allowedModes = MethodHandles.Lookup.class.getDeclaredField("allowedModes");
            allowedModes.setAccessible(true);
            int modifiers = allowedModes.getModifiers();
            modifiersField.setInt(allowedModes, modifiers & ~Modifier.FINAL);
            lookupClassAllowedModesField = allowedModes;
        }
        return lookupClassAllowedModesField;
    }

}
