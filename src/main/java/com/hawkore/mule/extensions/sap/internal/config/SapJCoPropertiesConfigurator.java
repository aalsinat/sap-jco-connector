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
package com.hawkore.mule.extensions.sap.internal.config;

import java.util.Properties;

/**
 * SAP JCo properties configurator.
 *
 * @author Alex Alsina de la Torre (alex.alsina@mango.com)
 */
public interface SapJCoPropertiesConfigurator {

    /**
     * Append new properties to provided properties
     *
     * @param target
     *     the target
     */
    public void configure(Properties target);

}
