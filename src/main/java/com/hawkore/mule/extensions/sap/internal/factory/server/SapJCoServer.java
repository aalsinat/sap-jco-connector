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
package com.hawkore.mule.extensions.sap.internal.factory.server;

import com.hawkore.mule.extensions.sap.internal.factory.JCoFactory;
import com.sap.conn.jco.server.JCoServer;
import org.mule.runtime.extension.api.runtime.source.SourceCallbackContext;

/**
 * SapJCoServer interface.
 *
 * @author Alex Alsina de la Torre (alex.alsina@mango.com)
 */
public interface SapJCoServer {

    /**
     * The constant EMMITED_JCO_FUNC_RESPONSE variable name
     */
    String EMMITED_JCO_FUNC_RESPONSE = "EMMITED_JCO_FUNC_RESPONSE";
    /**
     * The constant JCO_SERVER_TRANSACTION_ID variable name
     */
    String JCO_SERVER_TRANSACTION_ID = "JCO-SERVER-TID";

    /**
     * Start.
     */
    void start();

    /**
     * Stop.
     */
    void stop();

    /**
     * On success.
     *
     * @param transactionId
     *     the transaction id
     */
    void onSuccess(String transactionId);

    /**
     * On error.
     *
     * @param transactionId
     *     the transaction id
     */
    void onError(String transactionId);

    /**
     * Do notify success.
     *
     * @param context
     *     the context
     */
    void doNotifySuccess(SourceCallbackContext context);

    /**
     * Do notify exception.
     *
     * @param context
     *     the context
     */
    void doNotifyException(SourceCallbackContext context);

    /**
     * Gets jco server.
     *
     * @return the jco server
     */
    JCoServer getJCoServer();

    /**
     * Gets j co factory.
     *
     * @return the j co factory
     */
    JCoFactory getJCoFactory();

}
