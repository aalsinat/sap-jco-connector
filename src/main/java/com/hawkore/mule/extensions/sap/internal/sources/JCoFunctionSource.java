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
package com.hawkore.mule.extensions.sap.internal.sources;

import java.io.InputStream;
import java.util.Map;

import com.hawkore.mule.extensions.sap.api.operations.SapJCoFunctionSourceAttributes;
import com.hawkore.mule.extensions.sap.internal.config.SapJCoConnection;
import com.hawkore.mule.extensions.sap.internal.datasense.AMetadataResolver;
import com.hawkore.mule.extensions.sap.internal.datasense.JCoFunctionMetadataResolver;
import com.hawkore.mule.extensions.sap.internal.factory.server.SapJCoServer;
import com.hawkore.mule.extensions.sap.internal.utils.U;
import com.hawkore.mule.extensions.sap.internal.utils.X;
import com.sap.conn.jco.JCoFunction;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.ConnectionProvider;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.execution.OnError;
import org.mule.runtime.extension.api.annotation.execution.OnSuccess;
import org.mule.runtime.extension.api.annotation.metadata.MetadataKeyId;
import org.mule.runtime.extension.api.annotation.metadata.MetadataScope;
import org.mule.runtime.extension.api.annotation.metadata.TypeResolver;
import org.mule.runtime.extension.api.annotation.param.ConfigOverride;
import org.mule.runtime.extension.api.annotation.param.Connection;
import org.mule.runtime.extension.api.annotation.param.Content;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.annotation.param.NullSafe;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Example;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.annotation.param.display.Summary;
import org.mule.runtime.extension.api.annotation.source.EmitsResponse;
import org.mule.runtime.extension.api.runtime.source.Source;
import org.mule.runtime.extension.api.runtime.source.SourceCallback;
import org.mule.runtime.extension.api.runtime.source.SourceCallbackContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registers a JCoServer to process incoming JCoFunctions calls from ABAP programs
 *
 * @author Alex Alsina de la Torre (alex.alsina@mango.com)
 */
@Alias("function-source")
@DisplayName("Source - receive Function")
@MetadataScope(keysResolver = JCoFunctionMetadataResolver.class, outputResolver = JCoFunctionMetadataResolver.class)
@MediaType(MediaType.APPLICATION_XML)
@EmitsResponse
public class JCoFunctionSource extends Source<InputStream, SapJCoFunctionSourceAttributes> {

    private static final Logger logger = LoggerFactory.getLogger(JCoFunctionSource.class);
    @Summary("The gateway host at which the JCoServer should be registered")
    @Parameter
    private String gatewayHost;
    @Summary(
        "The gateway service to be used for registering at the gateway, i.e. the symbolic service name or the port "
            + "number")
    @Parameter
    private String gatewayService;
    @Summary("The program ID for registering and identifying the JCoServer at the gateway")
    @Parameter
    private String programId;
    @Summary("The number of server connections to register at the gateway")
    @Parameter
    @Optional(defaultValue = "1")
    private Integer connectionCount;
    @Parameter
    @Optional
    @MetadataKeyId
    @Summary("Name of the unique Function to be handled. Received Function that do not match selected one will be "
                 + "silently discarded. If not provided, any Function can be received.")
    @DisplayName("Handle only this function")
    private String handleOnlyThisFunction;
    @ConfigOverride
    @Placement(tab = Placement.ADVANCED_TAB, order = 1)
    @Example("UTF-8")
    @Summary("Overrides default encoding defined in configuration")
    private String encoding;
    @ConfigOverride
    @Parameter
    @Placement(tab = Placement.ADVANCED_TAB, order = 2)
    @Summary("Overrides default disable functions cache flag in configuration")
    private boolean disableFunctionsCache;
    @Parameter
    @Optional
    @NullSafe
    @Placement(tab = Placement.ADVANCED_TAB, order = 3)
    @DisplayName("Additional Server JCo properties")
    @Summary("See sapjco3 javadoc for supported properties (com.sap.conn.jco.ext.ServerDataProvider)")
    private Map<String, Object> additionalJCOProperties;
    @Connection
    private ConnectionProvider<SapJCoConnection> connectionProvider;
    private SapJCoServer server;

    /**
     * On start.
     *
     * @param sourceCallback
     *     the source callback
     * @throws MuleException
     *     the mule exception
     */
    @Override
    public void onStart(SourceCallback<InputStream, SapJCoFunctionSourceAttributes> sourceCallback)
        throws MuleException {
        try {
            String selectedFunction =
                X.isEmpty(handleOnlyThisFunction) || handleOnlyThisFunction.equals(AMetadataResolver.NONE)
                    ? null
                    : handleOnlyThisFunction;
            this.server = connectionProvider.connect().getJCoFactory()
                              .createJCoFunctionServer(this.gatewayHost, this.gatewayService, this.connectionCount,
                                  this.programId, selectedFunction, this.encoding, this.additionalJCOProperties,
                                  sourceCallback);
            this.server.start();
        } catch (Exception e) {
            sourceCallback.onConnectionException(new ConnectionException(e));
        }
    }

    /**
     * On success.
     *
     * @param response
     *     the JCoFunction as result of processing incoming JCoFunction
     * @param context
     *     the source callback context
     */
    @OnSuccess
    public void onSuccess(
        @Optional @TypeResolver(JCoFunctionMetadataResolver.class) @DisplayName("Function XML response content")
        @Summary("The Function as result of processing incoming Function. If provided, it MUST be the same "
                     + "Function type as incoming one. If not provided, incoming Function will not be altered.")
        @Content InputStream response, SourceCallbackContext context) {
        // nofify transaction success to JCoServer
        if (context.getVariable(SapJCoServer.JCO_SERVER_TRANSACTION_ID).isPresent()) {
            this.server.onSuccess((String)context.getVariable(SapJCoServer.JCO_SERVER_TRANSACTION_ID).get());
        }
        try {
            if (response != null) {
                InputStream emmitedResponse = response;
                if (logger.isDebugEnabled()) {
                    emmitedResponse = U.logRawContent("Function's Source emmits this JCoFunction response:\n",
                        emmitedResponse, "\n");
                }
                // parse response as JCoFunction
                JCoFunction responseFunction = server.getJCoFactory()
                                                   .parseJCoFunctionXML(emmitedResponse, this.encoding,
                                                       this.disableFunctionsCache);
                context.addVariable(SapJCoServer.EMMITED_JCO_FUNC_RESPONSE, responseFunction);
            }
        } catch (Exception e) {
            // silent - not a valid JCoFunction
            if (logger.isDebugEnabled()) {
                logger.debug("Unable to parse emmited JCoFunction response", e);
            }
        }
        // cleanup cache
        if (this.disableFunctionsCache) {
            this.server.getJCoServer().getRepository().clear();
        }
        // notify success to context to process response
        this.server.doNotifySuccess(context);
    }

    /**
     * On exception.
     *
     * @param context
     *     the context
     */
    @OnError
    public void onException(SourceCallbackContext context) {
        // nofify transaction error to JCoServer
        if (context.getVariable(SapJCoServer.JCO_SERVER_TRANSACTION_ID).isPresent()) {
            this.server.onError((String)context.getVariable(SapJCoServer.JCO_SERVER_TRANSACTION_ID).get());
        }
        // notify error to context
        this.server.doNotifyException(context);
    }

    /**
     * On stop.
     */
    @Override
    public void onStop() {
        this.server.stop();
    }

    /**
     * Gets gateway host.
     *
     * @return the gateway host
     */
    public String getGatewayHost() {
        return gatewayHost;
    }

    /**
     * Sets gateway host.
     *
     * @param gatewayHost
     *     the gateway host
     * @return this for chaining
     */
    public JCoFunctionSource setGatewayHost(String gatewayHost) {
        this.gatewayHost = gatewayHost;
        return this;
    }

    /**
     * Gets gateway service.
     *
     * @return the gateway service
     */
    public String getGatewayService() {
        return gatewayService;
    }

    /**
     * Sets gateway service.
     *
     * @param gatewayService
     *     the gateway service
     * @return this for chaining
     */
    public JCoFunctionSource setGatewayService(String gatewayService) {
        this.gatewayService = gatewayService;
        return this;
    }

    /**
     * Gets program id.
     *
     * @return the program id
     */
    public String getProgramId() {
        return programId;
    }

    /**
     * Sets program id.
     *
     * @param programId
     *     the program id
     * @return this for chaining
     */
    public JCoFunctionSource setProgramId(String programId) {
        this.programId = programId;
        return this;
    }

    /**
     * Gets connection count.
     *
     * @return the connection count
     */
    public Integer getConnectionCount() {
        return connectionCount;
    }

    /**
     * Sets connection count.
     *
     * @param connectionCount
     *     the connection count
     * @return this for chaining
     */
    public JCoFunctionSource setConnectionCount(Integer connectionCount) {
        this.connectionCount = connectionCount;
        return this;
    }

    /**
     * Gets encoding.
     *
     * @return the encoding
     */
    public String getEncoding() {
        return encoding;
    }

    /**
     * Sets encoding.
     *
     * @param encoding
     *     the encoding
     * @return this for chaining
     */
    public JCoFunctionSource setEncoding(String encoding) {
        this.encoding = encoding;
        return this;
    }

    /**
     * Gets handle only this function.
     *
     * @return the handle only this function
     */
    public String getHandleOnlyThisFunction() {
        return handleOnlyThisFunction;
    }

    /**
     * Sets handle only this function.
     *
     * @param handleOnlyThisFunction
     *     the handle only this function
     * @return this for chaining
     */
    public JCoFunctionSource setHandleOnlyThisFunction(String handleOnlyThisFunction) {
        this.handleOnlyThisFunction = handleOnlyThisFunction;
        return this;
    }

    /**
     * Gets connection provider.
     *
     * @return the connection provider
     */
    public ConnectionProvider<SapJCoConnection> getConnectionProvider() {
        return connectionProvider;
    }

    /**
     * Sets connection provider.
     *
     * @param connectionProvider
     *     the connection provider
     * @return this for chaining
     */
    public JCoFunctionSource setConnectionProvider(ConnectionProvider<SapJCoConnection> connectionProvider) {
        this.connectionProvider = connectionProvider;
        return this;
    }

    /**
     * Is disable functions cache boolean.
     *
     * @return the boolean
     */
    public boolean isDisableFunctionsCache() {
        return disableFunctionsCache;
    }

    /**
     * Sets disable functions cache.
     *
     * @param disableFunctionsCache
     *     the disable functions cache
     * @return this for chaining
     */
    public JCoFunctionSource setDisableFunctionsCache(boolean disableFunctionsCache) {
        this.disableFunctionsCache = disableFunctionsCache;
        return this;
    }

    /**
     * Gets additional jco properties.
     *
     * @return the additional jco properties
     */
    public Map<String, Object> getAdditionalJCOProperties() {
        return additionalJCOProperties;
    }

    /**
     * Sets additional jco properties.
     *
     * @param additionalJCOProperties
     *     the additional jco properties
     * @return this for chaining
     */
    public JCoFunctionSource setAdditionalJCOProperties(Map<String, Object> additionalJCOProperties) {
        this.additionalJCOProperties = additionalJCOProperties;
        return this;
    }

}
