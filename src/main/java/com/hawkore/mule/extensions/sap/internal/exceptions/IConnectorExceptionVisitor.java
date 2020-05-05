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

/**
 * IConnectorExceptionVisitor
 *
 * @param <T>
 *     wrapped exception
 * @author Manuel Núñez  (alex.alsina@mango.com)
 */
public interface IConnectorExceptionVisitor<T> {

    /**
     * Visit BadConfigurationException
     *
     * @param exception
     *     the exception to visit
     * @return wrapped exception
     */
    T accept(InvalidConfigurationException exception);

    /**
     * Visit InvalidOperationParamException
     *
     * @param exception
     *     the exception to visit
     * @return wrapped exception
     */
    T accept(InvalidOperationParamException exception);

    /**
     * Visit ObjectDefinitionNotFoundException
     *
     * @param exception
     *     the exception to visit
     * @return wrapped exception
     */
    T accept(ResourceNotFoundException exception);

    /**
     * Visit exception
     *
     * @param exception
     *     the exception to visit
     * @return wrapped exception
     */
    T accept(OperationExecutionException exception);

    /**
     * Visit OperationTimeoutException
     *
     * @param exception
     *     the exception to visit
     * @return wrapped exception
     */
    T accept(OperationTimeoutException exception);

    /**
     * Visit LogonFailureException
     *
     * @param e
     *     the e
     * @return wrapped exception
     */
    T accept(LogonFailureException e);

    /**
     * Visit CommunicationException
     *
     * @param e
     *     the e
     * @return wrapped exception
     */
    T accept(CommunicationException e);

    /**
     * Visit CommunicationException
     *
     * @param e
     *     the e
     * @return wrapped exception
     */
    T accept(TransactionExecutionException e);

    /**
     * Visit SapJCoServerException
     *
     * @param e
     *     the e
     * @return wrapped exception
     */
    T accept(SapJCoServerException e);

    /**
     * Visit XmlParserException
     *
     * @param e
     *     the e
     * @return wrapped exception
     */
    T accept(XmlParserException e);

    /**
     * Visit SapJCoAbapException
     *
     * @param e
     *     the e
     * @return wrapped exception
     */
    T accept(SapJCoAbapException e);

}
