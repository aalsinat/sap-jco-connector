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
package com.hawkore.mule.extensions.sap.internal.factory.constants;

/**
 * IDoc constants.
 *
 * @author Alex Alsina de la Torre (alex.alsina@mango.com)
 */
public final class IDocConstants {

    /**
     * The constant EDI_DC_40_TAG.
     */
    public static final String EDI_DC_40_TAG = "EDI_DC40";
    /**
     * The constant IDOC_TAG.
     */
    public static final String IDOC_TAG = "IDOC";
    /**
     * The constant SEGMENT_ATTR.
     */
    public static final String SEGMENT_ATTR = "SEGMENT";
    /**
     * The constant BEGIN_ATTR.
     */
    public static final String BEGIN_ATTR = "BEGIN";
    /**
     * The constant EDI_DC_DESC.
     */
    public static final String EDI_DC_DESC =
        "The control record IDoc. Contains important information on the transmitting and receiving partners. "
            + "It can occur exactly one time per IDoc";
    /**
     * The constant SAP_IDOC_SCHEMA_DESC.
     */
    public static final String SAP_IDOC_SCHEMA_DESC = "SAP IDoc schema definition for ";

}
