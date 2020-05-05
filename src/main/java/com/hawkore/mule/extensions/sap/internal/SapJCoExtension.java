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
package com.hawkore.mule.extensions.sap.internal;

import com.hawkore.mule.extensions.sap.api.config.auth.Authentication;
import com.hawkore.mule.extensions.sap.api.config.auth.UsernamePasswordAuth;
import com.hawkore.mule.extensions.sap.api.config.auth.X509CerificateAuth;
import com.hawkore.mule.extensions.sap.api.config.host.ApplicationServerConfig;
import com.hawkore.mule.extensions.sap.api.config.host.MessageServerConfig;
import com.hawkore.mule.extensions.sap.api.config.host.RemoteHostConfig;
import com.hawkore.mule.extensions.sap.api.exceptions.SapJCoModuleErrorType;
import com.hawkore.mule.extensions.sap.internal.config.SapJCoConfiguration;
import org.mule.runtime.extension.api.annotation.Configurations;
import org.mule.runtime.extension.api.annotation.Extension;
import org.mule.runtime.extension.api.annotation.SubTypeMapping;
import org.mule.runtime.extension.api.annotation.dsl.xml.Xml;
import org.mule.runtime.extension.api.annotation.error.ErrorTypes;

/**
 * Connector to integrate your mule applications with SAP ERP Central Component (ECC) using SAP JCo libraries.
 *
 * @author Alex Alsina de la Torre (alex.alsina@mango.com)
 */
@Xml(prefix = SapJCoExtension.PREFIX)
@Extension(name = SapJCoExtension.NAME, vendor = SapJCoExtension.VENDOR)
@Configurations(SapJCoConfiguration.class)
@ErrorTypes(SapJCoModuleErrorType.class)
@SubTypeMapping(baseType = Authentication.class, subTypes = {UsernamePasswordAuth.class, X509CerificateAuth.class})
@SubTypeMapping(baseType = RemoteHostConfig.class,
    subTypes = {ApplicationServerConfig.class, MessageServerConfig.class})

public class SapJCoExtension {

    /**
     * connector creator url
     */
    public static final String VENDOR_URL = "https://www.hawkore.com";
    /**
     * connector creator
     */
    public static final String VENDOR = "HAWKORE, S.L.";
    /**
     * connector friendly name
     */
    public static final String NAME = "SAP JCo";
    /**
     * connector xml prefix
     */
    public static final String PREFIX = "sap-jco";

}
