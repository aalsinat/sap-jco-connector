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

import java.nio.charset.Charset;
import java.util.concurrent.TimeUnit;

import com.hawkore.mule.extensions.sap.internal.factory.constants.XmlSchemaConstants;
import com.hawkore.mule.extensions.sap.internal.operations.IDocDocumentOperations;
import com.hawkore.mule.extensions.sap.internal.operations.JCoFunctionOperations;
import com.hawkore.mule.extensions.sap.internal.operations.TransactionOperations;
import com.hawkore.mule.extensions.sap.internal.sources.IDocDocumentSource;
import com.hawkore.mule.extensions.sap.internal.sources.JCoFunctionSource;
import org.mule.runtime.api.meta.ExpressionSupport;
import org.mule.runtime.api.meta.NamedObject;
import org.mule.runtime.extension.api.annotation.Configuration;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.Operations;
import org.mule.runtime.extension.api.annotation.Sources;
import org.mule.runtime.extension.api.annotation.connectivity.ConnectionProviders;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.RefName;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Example;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.annotation.param.display.Summary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SAP JCo connection's configuration.
 * <p>
 * With this connector you can integrate your mule applications with SAP ERP Central Component (ECC).
 *
 * @author Alex Alsina de la Torre (alex.alsina@mango.com)
 */
@Configuration(name = "config")
@ConnectionProviders(SapJCoConnectionProvider.class)
@Operations({IDocDocumentOperations.class, JCoFunctionOperations.class, TransactionOperations.class})
@Sources({IDocDocumentSource.class, JCoFunctionSource.class})
public class SapJCoConfiguration implements SapJCoConfigurationDataProviderLifeCycle, NamedObject {

    private static final Logger logger = LoggerFactory.getLogger(SapJCoConfiguration.class);
    @RefName
    private String name;
    @Parameter
    @Optional(defaultValue = XmlSchemaConstants.UTF_8)
    @Expression(ExpressionSupport.NOT_SUPPORTED)
    @Placement(tab = Placement.ADVANCED_TAB, order = 1)
    @Example("UTF-8")
    @Summary("Default encoding. By default UTF-8")
    private String encoding;
    @Parameter
    @Summary(
        "The version of the IDoc. Default 0. See com.sap.conn.idoc.IDocFactory documentation for supported versions.")
    @Optional(defaultValue = "0")
    @Placement(tab = Placement.ADVANCED_TAB, order = 2)
    @DisplayName("IDoc version")
    private Character version;
    @Parameter
    @Placement(tab = Placement.ADVANCED_TAB, order = 3)
    @Summary("Disables JCo function templates cache")
    private boolean disableFunctionsCache;
    @Parameter
    @Placement(tab = Placement.ADVANCED_TAB, order = 4)
    @Optional(defaultValue = "0")
    @Summary("Operation timeout. Default 0 disabled")
    @DisplayName("Global timeout for operations.")
    private Long operationTimeout;
    @Parameter
    @Placement(tab = Placement.ADVANCED_TAB, order = 5)
    @Optional(defaultValue = "MILLISECONDS")
    @Summary("Operation timeout unit. Default MILLISECONDS")
    @DisplayName("The timeout unit")
    private TimeUnit operationTimeoutUnit = TimeUnit.MILLISECONDS;

    /**
     * Gets name.
     *
     * @return the name
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * Gets encoding.
     *
     * @return the encoding
     */
    public String getEncoding() {
        try {
            Charset.forName(this.encoding);
            return this.encoding;
        } catch (Exception e) {
            logger.error("Invalid encoding '{}'. Error was: {}. Using default encoding '{}'", this.encoding,
                e.getMessage(), XmlSchemaConstants.UTF_8);
            return XmlSchemaConstants.UTF_8;
        }
    }

    /**
     * Gets version.
     *
     * @return the version
     */
    public Character getVersion() {
        return version;
    }

    /**
     * Is disable function cache boolean.
     *
     * @return the boolean
     */
    public boolean isDisableFunctionsCache() {
        return disableFunctionsCache;
    }

    /**
     * Gets operation timeout.
     *
     * @return the operation timeout
     */
    public Long getOperationTimeout() {
        return operationTimeout;
    }

    /**
     * Gets operation timeout unit.
     *
     * @return the operation timeout unit
     */
    public TimeUnit getOperationTimeoutUnit() {
        return operationTimeoutUnit;
    }

}
