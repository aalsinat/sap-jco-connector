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
package com.hawkore.mule.extensions.sap.api.operations;

/**
 * SapJCo Function Source Attributes
 *
 * @author Alex Alsina de la Torre (alex.alsina@mango.com)
 */
public class SapJCoFunctionSourceAttributes implements SapJCoAttributes {

    /**
     * The incoming Function name
     */
    private final String incomingFunctionName;
    /**
     * The transaction id
     */
    private final String transactionId;

    /**
     * Instantiates a new Sap JCo attributes.
     *
     * @param incomingFunctionName
     *     the incoming Function name
     * @param transactionId
     *     the transaction id
     */
    public SapJCoFunctionSourceAttributes(String incomingFunctionName, String transactionId) {
        this.incomingFunctionName = incomingFunctionName;
        this.transactionId = transactionId;
    }

    /**
     * Gets transaction id.
     *
     * @return the transaction id
     */
    public String getTransactionId() {
        return this.transactionId;
    }

    /**
     * Gets incoming function name.
     *
     * @return the incoming function name
     */
    public String getIncomingFunctionName() {
        return incomingFunctionName;
    }

}
