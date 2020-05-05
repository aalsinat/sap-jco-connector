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
package com.hawkore.mule.extensions.sap.api.config.trace;

/**
 * JCo CPIC Trace Levels
 *
 * @author Alex Alsina de la Torre (alex.alsina@mango.com)
 */
public enum JCoCPICTraceLevel {
    /**
     * take over environment value
     */
    DEFAULT(-1),
    /**
     * disabled tracing
     */
    DISABLED(0),
    /**
     * trace level 1
     */
    LEVEL_1(1),
    /**
     * trace level 2
     */
    LEVEL_2(2),
    /**
     * trace level 3
     */
    LEVEL_3(3);
    private final Integer level;

    private JCoCPICTraceLevel(Integer level) {
        this.level = level;
    }

    /**
     * Gets level.
     *
     * @return the level
     */
    public Integer getLevel() {
        return this.level;
    }
}
