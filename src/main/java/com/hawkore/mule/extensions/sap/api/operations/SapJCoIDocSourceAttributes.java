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
 * SapJCo IDoc Source Attributes
 *
 * @author Alex Alsina de la Torre (alex.alsina@mango.com)
 */
public class SapJCoIDocSourceAttributes implements SapJCoAttributes {

    /**
     * The incoming IDoc type. Could be composed IDocType[-IDocExtension]
     */
    private final String incomingIDocType;
    /**
     * The incoming IDoc name. If IDocExtension not exists, returns IDocType otherwise IDocExtension
     */
    private final String incomingIDocName;
    /**
     * The transaction id
     */
    private final String transactionId;

    /**
     * Instantiates a new Sap JCo attributes.
     *
     * @param incomingIDocType
     *     the incoming IDoc identifier, could be composed IDocType[-IDocExtension]
     * @param incomingIDocName
     *     the incoming IDoc name (XML root name), if extension not exists, IDocType otherwise IDocExtension
     * @param transactionId
     *     the transaction id
     */
    public SapJCoIDocSourceAttributes(String incomingIDocType, String incomingIDocName, String transactionId) {
        this.incomingIDocType = incomingIDocType;
        this.incomingIDocName = incomingIDocName;
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
     * Gets incoming IDoc name.
     *
     * @return the incoming IDoc name
     */
    public String getIncomingIDocName() {
        return incomingIDocName;
    }

    /**
     * Gets incoming IDoc identifier.
     *
     * @return the incoming IDoc identifier
     */
    public String getIncomingIDocType() {
        return incomingIDocType;
    }

}
