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
package com.hawkore.mule.extensions.sap.api.config.auth;

import java.util.Properties;

import com.hawkore.mule.extensions.sap.internal.utils.U;
import com.sap.conn.jco.ext.DestinationDataProvider;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.dsl.xml.TypeDsl;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Password;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.annotation.param.display.Summary;

/**
 * Username Password authentication.
 *
 * @author Alex Alsina de la Torre (alex.alsina@mango.com)
 */
@DisplayName("Username Password authentication")
@TypeDsl(allowInlineDefinition = true, allowTopLevelDefinition = false)
@Alias("username-password")
public class UsernamePasswordAuth implements Authentication {

    /**
     * User name for logging into the SAP system
     */
    @Parameter
    @DisplayName("SAP username")
    @Placement(order = 1)
    @Summary("User name for logging into the SAP system")
    private String username;
    /**
     * Password for logging into the SAP system
     */
    @Parameter
    @DisplayName("SAP password")
    @Password
    @Placement(order = 2)
    @Summary("Password for logging into the SAP system")
    private String password;

    /**
     * Instantiates a new Username password auth.
     */
    public UsernamePasswordAuth() {
    }

    /**
     * Gets username.
     *
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Gets password.
     *
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Configure.
     *
     * @param properties
     *     the properties
     */
    @Override
    public void configure(Properties properties) {
        // user logon properties
        U.addProperty(properties, DestinationDataProvider.JCO_USER, this.username);
        U.addProperty(properties, DestinationDataProvider.JCO_PASSWD, this.password);
    }

}
