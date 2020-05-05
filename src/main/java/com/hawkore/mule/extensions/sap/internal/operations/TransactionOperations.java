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
package com.hawkore.mule.extensions.sap.internal.operations;

import com.hawkore.mule.extensions.sap.internal.config.SapJCoConnection;
import com.hawkore.mule.extensions.sap.internal.errors.ModuleExceptionMapper;
import com.hawkore.mule.extensions.sap.internal.errors.SapJCoErrorTypeProvider;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.error.Throws;
import org.mule.runtime.extension.api.annotation.param.Connection;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Transaction operations.
 *
 * @author Alex Alsina de la Torre (alex.alsina@mango.com)
 */
public class TransactionOperations {

    private static final Logger logger = LoggerFactory.getLogger(TransactionOperations.class);

    /**
     * Creates a transaction id.
     *
     * @param connection
     *     The connection
     * @return created transaction id
     */
    @MediaType(MediaType.TEXT_PLAIN)
    @DisplayName("Transaction - create")
    @Alias("transaction-create")
    @Throws({SapJCoErrorTypeProvider.class})
    public String createTransactionId(@Connection SapJCoConnection connection) {
        try {
            String transactionId = connection.getJCoFactory().createTransactionId();
            if (logger.isDebugEnabled()) {
                logger.debug("Create transaction id [{}].", transactionId);
            }
            return transactionId;
        } catch (Throwable e) {
            throw ModuleExceptionMapper.map(e);
        }
    }

    /**
     * Confirms a transaction.
     *
     * @param connection
     *     The connection
     * @param transactionId
     *     The transaction id to confirm
     */
    @DisplayName("Transaction - confirm")
    @Alias("transaction-confirm")
    @Throws({SapJCoErrorTypeProvider.class})
    public void confirmTransactionId(@Connection SapJCoConnection connection, String transactionId) {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Confirm transaction [{}].", transactionId);
            }
            connection.getJCoFactory().confirmTransaction(transactionId);
        } catch (Throwable e) {
            throw ModuleExceptionMapper.map(e);
        }
    }

}
