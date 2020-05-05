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
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Example;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.annotation.param.display.Summary;

/**
 * SAP Message server remote connection configuration.
 *
 * @author Alex Alsina de la Torre (alex.alsina@mango.com)
 */
@DisplayName("SAP Message server remote connection configuration")
@TypeDsl(allowInlineDefinition = true, allowTopLevelDefinition = false)
@Alias("sap-message-server")
public class MessageServerConfig implements RemoteHostConfig {

    /**
     * SAP message server host
     */
    @Parameter
    @DisplayName("SAP message server host")
    @Placement(order = 1)
    @Summary("The host of the message server.")
    private String host;
    /**
     * SAP System ID (SID)
     */
    @Parameter
    @DisplayName("SAP System ID (SID)")
    @Placement(order = 2)
    @Summary("System ID of the SAP system, the so-called SID")
    private String systemId;
    /**
     * SAP message server service or port number (optional)
     */
    @Parameter
    @DisplayName("SAP Message server service")
    @Optional
    @Placement(order = 3)
    @Summary("SAP message server service or port number (optional)")
    private String service;
    /**
     * Logon group name of SAP application servers (Optional, default is PUBLIC)
     */
    @Parameter
    @DisplayName("SAP Message server group name")
    @Optional
    @Placement(order = 4)
    @Example("PUBLIC")
    @Summary("Logon group name of SAP application servers (Optional, default is PUBLIC)")
    private String group;
    /**
     * SAProuter string to use for networks being protected by a firewall
     */
    @Parameter
    @Optional
    @DisplayName("SAP router")
    @Placement(tab = "Advanced")
    @Summary("SAProuter string to use for networks being protected by a firewall")
    private String router;

    /**
     * Instantiates a new Message server config.
     */
    public MessageServerConfig() {
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
     * Gets system id.
     *
     * @return the system id
     */
    public String getSystemId() {
        return this.systemId;
    }

    /**
     * Sets system id.
     *
     * @param systemId
     *     the system id
     */
    public void setSystemId(String systemId) {
        this.systemId = systemId;
    }

    /**
     * Gets port.
     *
     * @return the port
     */
    public String getService() {
        return this.service;
    }

    /**
     * Sets port.
     *
     * @param service
     *     the port
     */
    public void setService(String service) {
        this.service = service;
    }

    /**
     * Gets group.
     *
     * @return the group
     */
    public String getGroup() {
        return this.group;
    }

    /**
     * Sets group.
     *
     * @param group
     *     the group
     */
    public void setGroup(String group) {
        this.group = group;
    }

    /**
     * Gets router.
     *
     * @return the router
     */
    public String getRouter() {
        return this.router;
    }

    /**
     * Sets router.
     *
     * @param router
     *     the router
     */
    public void setRouter(String router) {
        this.router = router;
    }

    /**
     * Configure.
     *
     * @param target
     *     the target
     */
    @Override
    public void configure(Properties target) {
        U.addProperty(target, DestinationDataProvider.JCO_MSHOST, getHost());
        U.addProperty(target, DestinationDataProvider.JCO_MSSERV, getService());
        U.addProperty(target, DestinationDataProvider.JCO_R3NAME, getSystemId());
        U.addProperty(target, DestinationDataProvider.JCO_GROUP, getGroup());
        U.addProperty(target, DestinationDataProvider.JCO_SAPROUTER, getRouter());
    }

}
