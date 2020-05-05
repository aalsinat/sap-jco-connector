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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.charset.Charset;
import java.util.Properties;

import com.hawkore.mule.extensions.sap.internal.exceptions.InvalidConfigurationException;
import com.hawkore.mule.extensions.sap.internal.utils.U;
import com.sap.conn.jco.ext.DestinationDataProvider;
import com.sap.conn.jco.util.Codecs.Base64;
import org.mule.runtime.api.meta.model.display.PathModel.Type;
import org.mule.runtime.core.api.util.IOUtils;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.dsl.xml.TypeDsl;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Path;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.annotation.param.display.Summary;

/**
 * X.509 cerificate authentication.
 *
 * @author Alex Alsina de la Torre (alex.alsina@mango.com)
 */
@DisplayName("X.509 cerificate authentication")
@TypeDsl(allowInlineDefinition = true, allowTopLevelDefinition = false)
@Alias("x509-certificate")
public class X509CerificateAuth implements Authentication {

    /**
     * Path to an X.509 certificate to be used as logon ticket.
     */
    @Parameter
    @Optional
    @Path(type = Type.FILE)
    @Placement(order = 1)
    @DisplayName("X.509 Certificate")
    @Summary("Path to an X.509 certificate to be used as logon ticket.")
    private String path;

    /**
     * Instantiates a new X 509 cerificate auth.
     */
    public X509CerificateAuth() {
    }

    /**
     * Gets path.
     *
     * @return the path
     */
    public String getPath() {
        return path;
    }

    /**
     * Configure.
     *
     * @param properties
     *     the properties
     * @throws InvalidConfigurationException
     *     the invalid configuration exception
     */
    @Override
    public void configure(Properties properties) throws InvalidConfigurationException {
        try {
            // user logon property
            U.addProperty(properties, DestinationDataProvider.JCO_X509CERT, Base64.encode(
                IOUtils.toString(new FileInputStream(new File(this.path))).getBytes(Charset.forName("UTF-8"))));
        } catch (FileNotFoundException e) {
            throw new InvalidConfigurationException(
                "Invalid path '" + this.path + "' used to retrieve the X509 certificate.", e);
        }
    }

}
