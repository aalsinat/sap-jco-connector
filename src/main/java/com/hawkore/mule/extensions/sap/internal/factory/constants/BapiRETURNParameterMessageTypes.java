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
 * RETURN parameter message types.
 *
 * @author Manuel Núñez  (alex.alsina@mango.com)
 */
public enum BapiRETURNParameterMessageTypes {
    /**
     * S = normal message (NORMAL)
     */
    S("NORMAL"),
    /**
     * E = error message (ERROR)
     */
    E("ERROR"),
    /**
     * W = warning message (WARNING)
     */
    W("WARN"),
    /**
     * info message (INFO)
     */
    I("INFO"),
    /**
     * A = abort/termination message (FATAL)
     */
    A("FATAL");
    private final String status;

    BapiRETURNParameterMessageTypes(String status) {
        this.status = status;
    }

    /**
     * Gets status.
     *
     * @return the status
     */
    public String getStatus() {
        return status;
    }
}
