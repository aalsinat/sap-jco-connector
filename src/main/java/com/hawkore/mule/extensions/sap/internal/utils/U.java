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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramSocket;
import java.net.URLClassLoader;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Properties;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLStreamReader;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * The type U.
 */
public class U {

    private static final Logger LOGGER = getLogger(U.class);

    private U() {}

    /**
     * Closes given resource logging possible checked exception.
     *
     * @param rsrc
     *     Resource to close. If it's {@code null} - it's no-op.
     */
    public static void close(@Nullable AutoCloseable rsrc) {
        if (rsrc != null) {
            try {
                rsrc.close();
            } catch (Exception e) {
                LOGGER.warn("Failed to close resource: " + e.getMessage());
            }
        }
    }

    /**
     * Closes given resource suppressing possible checked exception.
     *
     * @param rsrc
     *     Resource to close. If it's {@code null} - it's no-op.
     * @param e
     *     Suppressor exception
     */
    public static void closeWithSuppressingException(@Nullable AutoCloseable rsrc, @NotNull Exception e) {
        if (rsrc != null) {
            try {
                rsrc.close();
            } catch (Exception suppressed) {
                e.addSuppressed(suppressed);
            }
        }
    }

    /**
     * Quietly closes given resource ignoring possible checked exception.
     *
     * @param rsrc
     *     Resource to close. If it's {@code null} - it's no-op.
     */
    public static void closeQuiet(@Nullable AutoCloseable rsrc) {
        if (rsrc != null) {
            try {
                rsrc.close();
            } catch (Exception ignored) {
                // No-op.
            }
        }
    }

    /**
     * Close quiet.
     *
     * @param rsrc
     *     the rsrc
     */
    public static void closeQuiet(@Nullable XMLStreamReader rsrc) {
        if (rsrc != null) {
            try {
                rsrc.close();
            } catch (Exception ignored) {
                // No-op.
            }
        }
    }

    /**
     * Close quiet.
     *
     * @param rsrc
     *     the rsrc
     */
    public static void closeQuiet(@Nullable XMLEventWriter rsrc) {
        if (rsrc != null) {
            try {
                rsrc.close();
            } catch (Exception ignored) {
                // No-op.
            }
        }
    }

    /**
     * Closes given resource logging possible checked exceptions.
     *
     * @param rsrc
     *     Resource to close. If it's {@code null} - it's no-op.
     */
    public static void close(@Nullable SelectionKey rsrc) {
        if (rsrc != null)
        // This apply will automatically deregister the selection key as well.
        {
            close(rsrc.channel());
        }
    }

    /**
     * Quietly closes given resource ignoring possible checked exceptions.
     *
     * @param rsrc
     *     Resource to close. If it's {@code null} - it's no-op.
     */
    public static void closeQuiet(@Nullable SelectionKey rsrc) {
        if (rsrc != null)
        // This apply will automatically deregister the selection key as well.
        {
            closeQuiet(rsrc.channel());
        }
    }

    /**
     * Closes given resource.
     *
     * @param rsrc
     *     Resource to close. If it's {@code null} - it's no-op.
     */
    public static void close(@Nullable DatagramSocket rsrc) {
        if (rsrc != null) {
            rsrc.close();
        }
    }

    /**
     * Closes given resource logging possible checked exception.
     *
     * @param rsrc
     *     Resource to close. If it's {@code null} - it's no-op.
     */
    public static void close(@Nullable Selector rsrc) {
        if (rsrc != null) {
            try {
                if (rsrc.isOpen()) {
                    rsrc.close();
                }
            } catch (IOException e) {
                LOGGER.warn("Failed to close resource: " + e.getMessage());
            }
        }
    }

    /**
     * Quietly closes given resource ignoring possible checked exception.
     *
     * @param rsrc
     *     Resource to close. If it's {@code null} - it's no-op.
     */
    public static void closeQuiet(@Nullable Selector rsrc) {
        if (rsrc != null) {
            try {
                if (rsrc.isOpen()) {
                    rsrc.close();
                }
            } catch (IOException ignored) {
                // No-op.
            }
        }
    }

    /**
     * Closes given resource logging possible checked exception.
     *
     * @param rsrc
     *     Resource to close. If it's {@code null} - it's no-op.
     */
    public static void close(@Nullable Context rsrc) {
        if (rsrc != null) {
            try {
                rsrc.close();
            } catch (NamingException e) {
                LOGGER.warn("Failed to close resource: " + e.getMessage());
            }
        }
    }

    /**
     * Quietly closes given resource ignoring possible checked exception.
     *
     * @param rsrc
     *     Resource to close. If it's {@code null} - it's no-op.
     */
    public static void closeQuiet(@Nullable Context rsrc) {
        if (rsrc != null) {
            try {
                rsrc.close();
            } catch (NamingException ignored) {
                // No-op.
            }
        }
    }

    /**
     * Closes class loader logging possible checked exception.
     *
     * @param clsLdr
     *     Class loader. If it's {@code null} - it's no-op.
     */
    public static void close(@Nullable URLClassLoader clsLdr) {
        if (clsLdr != null) {
            try {
                clsLdr.close();
            } catch (Exception e) {
                LOGGER.warn("Failed to close resource: " + e.getMessage());
            }
        }
    }

    /**
     * Calculate MD5 digits.
     *
     * @param in
     *     Input stream.
     * @return Calculated MD5 digest for given input stream.
     * @throws NoSuchAlgorithmException
     *     If MD5 algorithm was not found.
     * @throws IOException
     *     If an I/O exception occurs.
     */
    public static byte[] calculateMD5Digest(@NotNull InputStream in) throws NoSuchAlgorithmException, IOException {
        MessageDigest md = MessageDigest.getInstance("MD5");
        InputStream fis = new BufferedInputStream(in);
        byte[] dataBytes = new byte[1024];

        int nread;

        while ((nread = fis.read(dataBytes)) != -1) {
            md.update(dataBytes, 0, nread);
        }

        return md.digest();
    }

    /**
     * Calculate MD5 string.
     *
     * @param in
     *     Input stream.
     * @return Calculated MD5 string for given input stream.
     * @throws NoSuchAlgorithmException
     *     If MD5 algorithm was not found.
     * @throws IOException
     *     If an I/O exception occurs.
     */
    public static String calculateMD5(InputStream in) throws NoSuchAlgorithmException, IOException {
        byte[] md5Bytes = calculateMD5Digest(in);

        // Convert the byte to hex format.
        StringBuilder sb = new StringBuilder();

        for (byte md5Byte : md5Bytes) {
            sb.append(Integer.toString((md5Byte & 0xff) + 0x100, 16).substring(1));
        }

        return sb.toString();
    }

    /**
     * Print.
     *
     * @param content
     *     the content
     */
    public static void debugPrint(String content) {
        LOGGER.debug(content);
    }

    /**
     * Log raw content input stream.
     *
     * @param prefix
     *     the prefix
     * @param content
     *     the content
     * @param postfix
     *     the postfix
     * @return the input stream
     * @throws IOException
     *     the io exception
     */
    public static InputStream logRawContent(String prefix, InputStream content, String postfix) throws IOException {
        byte[] buffer = streamToArray(content);
        debugPrint(prefix + new String(buffer) + postfix);
        return new ByteArrayInputStream(buffer);
    }

    /**
     * Stream to array byte [ ].
     *
     * @param stream
     *     the stream
     * @return the byte [ ]
     * @throws IOException
     *     the io exception
     */
    public static byte[] streamToArray(InputStream stream) throws IOException {
        byte[] result = new byte[0];
        byte[] tmp = new byte[8192];
        int readCount = stream.read(tmp);
        while (readCount >= 0) {
            byte[] innerTmp = new byte[result.length + readCount];
            System.arraycopy(result, 0, innerTmp, 0, result.length);
            System.arraycopy(tmp, 0, innerTmp, result.length, readCount);
            result = innerTmp;
            readCount = stream.read(tmp);
        }
        stream.close();
        return result;
    }

    /**
     * Add property.
     *
     * @param target
     *     the target
     * @param key
     *     the key
     * @param value
     *     the value
     */
    public static void addProperty(Properties target, String key, Object value) {
        if (value != null) {
            target.put(key, value);
        }
    }

    /**
     * Add properties.
     *
     * @param target
     *     the target
     * @param keyVal
     *     the key val
     */
    public static void addProperties(Properties target, Map<String, Object> keyVal) {
        if (keyVal == null) {
            return;
        }
        keyVal.entrySet().forEach((e) -> addProperty(target, e.getKey(), e.getValue()));
    }

}
