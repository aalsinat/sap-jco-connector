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
package com.hawkore.mule.extensions.sap.internal.operations;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import com.hawkore.mule.extensions.sap.internal.config.SapJCoConnection;
import com.hawkore.mule.extensions.sap.internal.datasense.AMetadataResolver;
import com.hawkore.mule.extensions.sap.internal.datasense.IDocDocumentMetadataResolver;
import com.hawkore.mule.extensions.sap.internal.datasense.JCoFunctionMetadataResolver;
import com.hawkore.mule.extensions.sap.internal.errors.ModuleExceptionMapper;
import com.hawkore.mule.extensions.sap.internal.errors.SapJCoErrorTypeProvider;
import com.hawkore.mule.extensions.sap.internal.exceptions.InvalidOperationParamException;
import com.hawkore.mule.extensions.sap.internal.factory.JCoFactory;
import com.hawkore.mule.extensions.sap.internal.utils.U;
import com.hawkore.mule.extensions.sap.internal.utils.X;
import com.sap.conn.jco.JCoFunction;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.error.Throws;
import org.mule.runtime.extension.api.annotation.metadata.MetadataKeyId;
import org.mule.runtime.extension.api.annotation.metadata.OutputResolver;
import org.mule.runtime.extension.api.annotation.metadata.TypeResolver;
import org.mule.runtime.extension.api.annotation.param.ConfigOverride;
import org.mule.runtime.extension.api.annotation.param.Connection;
import org.mule.runtime.extension.api.annotation.param.Content;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Example;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.annotation.param.display.Summary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JCoFunction operations.
 *
 * @author Alex Alsina de la Torre (alex.alsina@mango.com)
 */
public class JCoFunctionOperations {

    private static final Logger logger = LoggerFactory.getLogger(JCoFunctionOperations.class);

    /**
     * Generates a JCoFunction's XML schema using its metadata. Generated XML schema can be used by
     * other applications to prepare valid XML function calls or to define metadata types within mule applications.
     *
     * @param connection
     *     The connection
     * @param functionName
     *     The name of the Function to generate its XML Schema
     * @param encoding
     *     Overrides default encoding defined in configuration
     * @param disableFunctionsCache
     *     Overrides default disable jCoFunction cache flag in configuration
     * @return the functions's XML schema
     */
    @DisplayName("Function - generate XML Schema")
    @Throws({SapJCoErrorTypeProvider.class})
    @MediaType(MediaType.APPLICATION_XML)
    @Alias("function-schema")
    public InputStream createFunctionSchema(@Connection SapJCoConnection connection,
        @DisplayName("Function name") @Summary("The name of the Function to generate its XML Schema")
        @MetadataKeyId(IDocDocumentMetadataResolver.class) String functionName,
        @ConfigOverride @Placement(tab = Placement.ADVANCED_TAB, order = 1)
        @Summary("Overrides default encoding defined in configuration") @Example("UTF-8") final String encoding,
        @ConfigOverride @Placement(tab = Placement.ADVANCED_TAB, order = 2)
        @Summary("Overrides default disable jCoFunction cache flag in configuration") boolean disableFunctionsCache) {
        try {
            if (X.isEmpty(functionName) || functionName.equals(AMetadataResolver.NONE)) {
                throw new InvalidOperationParamException("JCoFunction name is required to generate XMl schema");
            }
            InputStream content = JCoFactory.genJCoFunctionXmlSchema(
                connection.getJCoFactory().getFunction(functionName, disableFunctionsCache), encoding);
            if (logger.isDebugEnabled()) {
                content = U.logRawContent("JCoFunction '" + functionName + "' generated XML Schema:\n", content, "\n");
            }
            return content;
        } catch (Throwable e) {
            throw ModuleExceptionMapper.map(e);
        }
    }

    /**
     * Gets a JCoFunction instance, rendered as XML.
     *
     * @param connection
     *     The connection
     * @param functionName
     *     The name of the Function to be retrieved
     * @param encoding
     *     Overrides default encoding defined in configuration
     * @param disableFunctionsCache
     *     Overrides default disable functions cache flag in configuration
     * @return JCoFunction rendered as XML
     */
    @DisplayName("Function - instance")
    @OutputResolver(output = JCoFunctionMetadataResolver.class)
    @Throws({SapJCoErrorTypeProvider.class})
    @MediaType(MediaType.APPLICATION_XML)
    @Alias("function-instance")
    public InputStream getFunctionAsXML(@Connection SapJCoConnection connection,
        @MetadataKeyId(JCoFunctionMetadataResolver.class) @DisplayName("Function name")
        @Summary("The name of the Function to be retrieved") String functionName,
        @ConfigOverride @Placement(tab = Placement.ADVANCED_TAB, order = 1)
        @Summary("Overrides default encoding defined in configuration") @Example("UTF-8") final String encoding,
        @ConfigOverride @Placement(tab = Placement.ADVANCED_TAB, order = 2)
        @Summary("Overrides default disable functions cache flag in configuration") boolean disableFunctionsCache) {
        try {
            if (X.isEmpty(functionName) || functionName.equals(AMetadataResolver.NONE)) {
                throw new InvalidOperationParamException("JCoFunction name is required to be retrieved");
            }
            InputStream content = JCoFactory.renderXML(
                connection.getJCoFactory().getFunction(functionName, disableFunctionsCache), encoding);
            if (logger.isDebugEnabled()) {
                content = U.logRawContent("Retrieved jCoFunction '" + functionName + "' as XML:\n", content, "\n");
            }
            return content;
        } catch (Throwable e) {
            throw ModuleExceptionMapper.map(e);
        }
    }

    /**
     * Invokes a JCoFunction synchronously using the sRFC protocol.
     *
     * @param connection
     *     The connection
     * @param functionName
     *     Optional function's name to work with datasense metadata resolution
     * @param content
     *     Function to invoke as XML content
     * @param encoding
     *     Overrides default encoding defined in configuration
     * @param operationTimeout
     *     Overrides default timeout defined in configuration
     * @param operationTimeoutUnit
     *     Overrides default timeout unit defined in configuration
     * @param disableFunctionsCache
     *     Overrides default disable functions cache flag in configuration
     * @param processBapiReturnParameter
     *     Process BAPI RETURN parameter and throw exception if any error is found
     * @return the result of invocation as JCoFunction rendered as XML
     */
    @DisplayName("Function - invoke over sRFC")
    @OutputResolver(output = JCoFunctionMetadataResolver.class)
    @Throws({SapJCoErrorTypeProvider.class})
    @MediaType(MediaType.APPLICATION_XML)
    @Alias("function-invoke-srfc")
    public InputStream invokeOverSRFC(@Connection SapJCoConnection connection,
        @Optional(defaultValue = AMetadataResolver.NONE) @MetadataKeyId(JCoFunctionMetadataResolver.class)
        @DisplayName("Function name") @Summary("Optional function's name to work with datasense metadata resolution")
            String functionName,
        @DisplayName("Function XML content") @Content @TypeResolver(JCoFunctionMetadataResolver.class)
            InputStream content,
        @ConfigOverride @Placement(tab = Placement.ADVANCED_TAB, order = 1)
        @Summary("Overrides default encoding defined in configuration") @Example("UTF-8") final String encoding,
        @ConfigOverride @Placement(tab = Placement.ADVANCED_TAB, order = 2)
        @Summary("Overrides default timeout defined in configuration") Long operationTimeout,
        @ConfigOverride @Placement(tab = Placement.ADVANCED_TAB, order = 3)
        @Summary("Overrides default timeout unit defined in configuration") TimeUnit operationTimeoutUnit,
        @ConfigOverride @Placement(tab = Placement.ADVANCED_TAB, order = 4)
        @Summary("Overrides default disable functions cache flag in configuration") boolean disableFunctionsCache,
        @Placement(tab = Placement.ADVANCED_TAB, order = 5)
        @Summary("Process BAPI RETURN parameter and throw exception if any error is found")
        @DisplayName("Process BAPI RETURN Parameter") @Optional(defaultValue = "false")
            boolean processBapiReturnParameter) {
        try {
            InputStream jcoFunctionStream = content;
            if (logger.isDebugEnabled()) {
                jcoFunctionStream = U.logRawContent("Input XML JCoFunction to invoke over sRFC:\n", jcoFunctionStream,
                    "\n");
            }
            JCoFunction jCoFunction = connection.getJCoFactory()
                                          .parseJCoFunctionXML(jcoFunctionStream, encoding, disableFunctionsCache);
            // sync function execution
            connection.getJCoFactory().executeJCoFunction(jCoFunction, operationTimeout, operationTimeoutUnit);
            // process BAPI return parameter
            if (processBapiReturnParameter) {
                connection.getJCoFactory().processBapiReturnParameter(jCoFunction);
            }
            // returns executed jCoFunction
            InputStream response = JCoFactory.renderXML(jCoFunction, encoding);
            if (logger.isDebugEnabled()) {
                response = U.logRawContent("Execution response jCoFunction '" + functionName + "':\n", response, "\n");
            }
            return response;
        } catch (Throwable e) {
            throw ModuleExceptionMapper.map(e);
        }
    }

    /**
     * Invokes a JCoFunction in transactional mode using the tRFC protocol.
     *
     * @param connection
     *     The connection
     * @param functionName
     *     Optional function's name to work with datasense metadata resolution
     * @param content
     *     Function to invoke as XML content
     * @param transactionId
     *     The transaction id
     * @param encoding
     *     Overrides default encoding defined in configuration
     * @param operationTimeout
     *     Overrides default timeout defined in configuration
     * @param operationTimeoutUnit
     *     Overrides default timeout unit defined in configuration
     * @param disableFunctionsCache
     *     Overrides default disable functions cache flag in configuration
     */
    @DisplayName("Function - invoke over tRFC")
    @OutputResolver(output = JCoFunctionMetadataResolver.class)
    @Throws({SapJCoErrorTypeProvider.class})
    @Alias("function-invoke-trfc")
    public void invokeOverTRFC(@Connection SapJCoConnection connection,
        @Optional(defaultValue = AMetadataResolver.NONE) @MetadataKeyId(JCoFunctionMetadataResolver.class)
        @DisplayName("Function name") @Summary("Optional function's name to work with datasense metadata resolution")
            String functionName,
        @DisplayName("Function XML content") @Content @TypeResolver(JCoFunctionMetadataResolver.class)
            InputStream content,
        @Optional @Summary("The transaction ID to use") String transactionId,
        @ConfigOverride @Placement(tab = Placement.ADVANCED_TAB, order = 1)
        @Summary("Overrides default encoding defined in configuration") @Example("UTF-8") final String encoding,
        @ConfigOverride @Placement(tab = Placement.ADVANCED_TAB, order = 2)
        @Summary("Overrides default timeout defined in configuration") Long operationTimeout,
        @ConfigOverride @Placement(tab = Placement.ADVANCED_TAB, order = 3)
        @Summary("Overrides default timeout unit defined in configuration") TimeUnit operationTimeoutUnit,
        @ConfigOverride @Placement(tab = Placement.ADVANCED_TAB, order = 4)
        @Summary("Overrides default disable functions cache flag in configuration") boolean disableFunctionsCache) {
        try {
            InputStream jcoFunctionStream = content;
            if (logger.isDebugEnabled()) {
                jcoFunctionStream = U.logRawContent("Input XML JCoFunction to invoke over tRFC:\n", jcoFunctionStream,
                    "\n");
            }
            JCoFunction jCoFunction = connection.getJCoFactory()
                                          .parseJCoFunctionXML(jcoFunctionStream, encoding, disableFunctionsCache);
            connection.getJCoFactory()
                .executeJCoFunction(jCoFunction, transactionId, operationTimeout, operationTimeoutUnit);
        } catch (Throwable e) {
            throw ModuleExceptionMapper.map(e);
        }
    }

    /**
     * Invokes a JCoFunction in queued transactional mode using the qRFC protocol.
     *
     * @param connection
     *     The connection
     * @param functionName
     *     Optional function's name to work with datasense metadata resolution
     * @param content
     *     Function to invoke as XML content
     * @param transactionId
     *     The transaction id
     * @param queueName
     *     The inbound queue name to use
     * @param encoding
     *     Overrides default encoding defined in configuration
     * @param operationTimeout
     *     Overrides default timeout defined in configuration
     * @param operationTimeoutUnit
     *     Overrides default timeout unit defined in configuration
     * @param disableFunctionsCache
     *     Overrides default disable functions cache flag in configuration
     */
    @DisplayName("Function - invoke over qRFC")
    @OutputResolver(output = JCoFunctionMetadataResolver.class)
    @Throws({SapJCoErrorTypeProvider.class})
    @Alias("function-invoke-qrfc")
    public void invokeOverQRFC(@Connection SapJCoConnection connection,
        @Optional(defaultValue = AMetadataResolver.NONE) @MetadataKeyId(JCoFunctionMetadataResolver.class)
        @DisplayName("Function name") @Summary("Optional function's name to work with datasense metadata resolution")
            String functionName,
        @DisplayName("Function XML content") @Content @TypeResolver(JCoFunctionMetadataResolver.class)
            InputStream content,
        @Optional @Summary("the transaction ID to use") String transactionId,
        @Summary("The inbound queue name to use") String queueName,
        @ConfigOverride @Placement(tab = Placement.ADVANCED_TAB, order = 1)
        @Summary("Overrides default encoding defined in configuration") @Example("UTF-8") final String encoding,
        @ConfigOverride @Placement(tab = Placement.ADVANCED_TAB, order = 2)
        @Summary("Overrides default timeout defined in configuration") Long operationTimeout,
        @ConfigOverride @Placement(tab = Placement.ADVANCED_TAB, order = 3)
        @Summary("Overrides default timeout unit defined in configuration") TimeUnit operationTimeoutUnit,
        @ConfigOverride @Placement(tab = Placement.ADVANCED_TAB, order = 4)
        @Summary("Overrides default disable functions cache flag in configuration") boolean disableFunctionsCache) {
        try {
            InputStream jcoFunctionStream = content;
            if (logger.isDebugEnabled()) {
                jcoFunctionStream = U.logRawContent("Input XML JCoFunction to invoke over qRFC:\n", jcoFunctionStream,
                    "\n");
            }
            JCoFunction jCoFunction = connection.getJCoFactory()
                                          .parseJCoFunctionXML(jcoFunctionStream, encoding, disableFunctionsCache);
            connection.getJCoFactory()
                .executeJCoFunction(jCoFunction, transactionId, queueName, operationTimeout, operationTimeoutUnit);
        } catch (Throwable e) {
            throw ModuleExceptionMapper.map(e);
        }
    }

}
