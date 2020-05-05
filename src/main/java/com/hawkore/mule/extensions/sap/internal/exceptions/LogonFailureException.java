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

import org.jetbrains.annotations.Nullable;

/**
 * LogonFailureException
 *
 * @author Manuel Núñez  (alex.alsina@mango.com)
 */
public class LogonFailureException extends AbstractConnectorException {

    /**
     * Instantiates a new LogonFailureException exception.
     *
     * @param msg
     *     the msg
     */
    public LogonFailureException(final String msg) {
        super(msg);
    }

    /**
     * Instantiates a new LogonFailureException exception.
     *
     * @param cause
     *     the cause
     */
    public LogonFailureException(final Throwable cause) {
        this(cause.getMessage(), cause);
    }

    /**
     * Instantiates a new LogonFailureException exception.
     *
     * @param msg
     *     the msg
     * @param cause
     *     the cause
     */
    public LogonFailureException(final String msg, @Nullable final Throwable cause) {
        super(msg, cause);
    }

    /**
     * {@inheritDoc}
     *
     * @param <T>
     *     the type parameter
     * @param visitor
     *     the visitor
     * @return the t
     */
    @Override
    public <T> T accept(final IConnectorExceptionVisitor<T> visitor) {
        return visitor.accept(this);
    }

}
