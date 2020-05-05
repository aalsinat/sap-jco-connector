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
package com.hawkore.mule.extensions.sap.internal.exceptions;

import com.hawkore.mule.extensions.sap.internal.utils.X;
import org.jetbrains.annotations.Nullable;

/**
 * Abstract Connector Exception
 *
 * @author Manuel Núñez  (alex.alsina@mango.com)
 */
public abstract class AbstractConnectorException extends RuntimeException {

    /**
     * Instantiates a new Abstract connector exception.
     *
     * @param msg
     *     the message
     */
    public AbstractConnectorException(final String msg) {
        super(msg);
    }

    /**
     * Instantiates a new Abstract connector exception.
     *
     * @param cause
     *     wrapped Throwable
     */
    public AbstractConnectorException(final Throwable cause) {
        this(cause.getMessage(), cause);
    }

    /**
     * Instantiates a new Abstract connector exception.
     *
     * @param msg
     *     the message
     * @param cause
     *     wrapped Throwable
     */
    public AbstractConnectorException(final String msg, @Nullable final Throwable cause) {
        super(msg, cause);
    }

    /**
     * Has cause boolean.
     *
     * @param cls
     *     the exception class
     * @return whether has cause
     */
    public final boolean hasCause(@Nullable final Class<? extends Throwable>[] cls) {
        return X.hasCause(this, cls);
    }

    /**
     * Gets cause.
     *
     * @param <T>
     *     wrapped exception
     * @param cls
     *     the exception class
     * @return the cause
     */
    @Nullable
    public final <T extends Throwable> T getCause(@Nullable final Class<T> cls) {
        return X.cause(this, cls);
    }

    /**
     * To string string.
     *
     * @return the string
     */
    @Override
    public final String toString() {
        return super.getClass() + ": " + this.getMessage();
    }

    /**
     * Accept t.
     *
     * @param <T>
     *     wrapped exception
     * @param visitor
     *     the connector exception visitor
     * @return a wrapped exception
     */
    public abstract <T> T accept(IConnectorExceptionVisitor<T> visitor);

}
