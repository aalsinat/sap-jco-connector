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
package com.hawkore.mule.extensions.sap.api.config.host;

import java.util.Properties;

import com.hawkore.mule.extensions.sap.internal.utils.U;
import com.sap.conn.jco.ext.DestinationDataProvider;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.dsl.xml.TypeDsl;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.annotation.param.display.Summary;

/**
 * SAP Application server remote connection configuration.
 *
 * @author Alex Alsina de la Torre (alex.alsina@mango.com)
 */
@DisplayName("SAP Application server remote connection configuration")
@TypeDsl(allowInlineDefinition = true, allowTopLevelDefinition = false)
@Alias("sap-application-server")
public class ApplicationServerConfig implements RemoteHostConfig {

    /**
     * The host of the SAP application server.
     */
    @Parameter
    @DisplayName("SAP application server host")
    @Placement(order = 1)
    @Summary("The host of the application server.")
    private String host;

    /**
     * Instantiates a new Application server config.
     */
    public ApplicationServerConfig() {
    }

    /**
     * Gets host.
     *
     * @return the host
     */
    public String getHost() {
        return this.host;
    }

    /**
     * Sets host.
     *
     * @param host
     *     the host
     */
    public void setHost(String host) {
        this.host = host;
    }

    /**
     * Configure.
     *
     * @param target
     *     the target
     */
    @Override
    public void configure(Properties target) {
        U.addProperty(target, DestinationDataProvider.JCO_ASHOST, getHost());
    }

}
