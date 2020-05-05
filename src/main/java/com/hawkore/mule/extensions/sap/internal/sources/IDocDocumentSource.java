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

import com.hawkore.mule.extensions.sap.api.operations.SapJCoIDocSourceAttributes;
import com.hawkore.mule.extensions.sap.internal.config.SapJCoConnection;
import com.hawkore.mule.extensions.sap.internal.datasense.AMetadataResolver;
import com.hawkore.mule.extensions.sap.internal.datasense.IDocDocumentMetadataResolver;
import com.hawkore.mule.extensions.sap.internal.factory.server.SapJCoServer;
import com.hawkore.mule.extensions.sap.internal.utils.X;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.ConnectionProvider;
import org.mule.runtime.api.exception.MuleException;
import org.mule.runtime.extension.api.annotation.Alias;
import org.mule.runtime.extension.api.annotation.metadata.MetadataKeyId;
import org.mule.runtime.extension.api.annotation.metadata.MetadataScope;
import org.mule.runtime.extension.api.annotation.param.ConfigOverride;
import org.mule.runtime.extension.api.annotation.param.Connection;
import org.mule.runtime.extension.api.annotation.param.MediaType;
import org.mule.runtime.extension.api.annotation.param.NullSafe;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Example;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.annotation.param.display.Summary;
import org.mule.runtime.extension.api.runtime.source.Source;
import org.mule.runtime.extension.api.runtime.source.SourceCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Registers a JCoIDocServer to receive IDocs from ABAP programs
 *
 * @author Alex Alsina de la Torre (alex.alsina@mango.com)
 */
@Alias("idoc-source")
@DisplayName("Source - receive IDoc")
@MetadataScope(keysResolver = IDocDocumentMetadataResolver.class, outputResolver = IDocDocumentMetadataResolver.class)
@MediaType(MediaType.APPLICATION_XML)
public class IDocDocumentSource extends Source<InputStream, SapJCoIDocSourceAttributes> {

    private static final Logger logger = LoggerFactory.getLogger(IDocDocumentSource.class);
    @Summary("The gateway host at which the JCoIDocServer should be registered")
    @Parameter
    private String gatewayHost;
    @Summary(
        "The gateway service to be used for registering at the gateway, i.e. the symbolic service name or the port "
            + "number")
    @Parameter
    private String gatewayService;
    @Summary("The program ID for registering and identifying the JCoIDocServer at the gateway")
    @Parameter
    private String programId;
    @Summary("The number of server connections to register at the gateway")
    @Parameter
    @Optional(defaultValue = "1")
    private Integer connectionCount;
    @Parameter
    @Optional
    @MetadataKeyId
    @DisplayName("Handle only this IDoc")
    @Summary("Unique IDoc type to be handled. Received IDocs that do not match selected one will be silently "
                 + "discarded. If not provided, any IDoc can be received.")
    private String handleOnlyThisIDoc;
    //@ConfigOverride
    @Parameter
    @Placement(tab = Placement.ADVANCED_TAB, order = 1)
    @Example("UTF-8")
    @Summary("Overrides default encoding defined in configuration")
    private String encoding;
    @Parameter
    @Optional
    @NullSafe
    @Placement(tab = Placement.ADVANCED_TAB, order = 2)
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
    public void onStart(SourceCallback<InputStream, SapJCoIDocSourceAttributes> sourceCallback) throws MuleException {
        try {
            String selectedIdoc = X.isEmpty(handleOnlyThisIDoc) || handleOnlyThisIDoc.equals(AMetadataResolver.NONE)
                                      ? null
                                      : handleOnlyThisIDoc;

            this.server = connectionProvider.connect().getJCoFactory()
                              .createIDocServer(this.gatewayHost, this.gatewayService, this.connectionCount,
                                  this.programId, selectedIdoc, this.encoding, this.additionalJCOProperties, sourceCallback);
            this.server.start();
        } catch (Exception e) {
            sourceCallback.onConnectionException(new ConnectionException(e));
        }
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
    public IDocDocumentSource setGatewayHost(String gatewayHost) {
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
    public IDocDocumentSource setGatewayService(String gatewayService) {
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
    public IDocDocumentSource setProgramId(String programId) {
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
    public IDocDocumentSource setConnectionCount(Integer connectionCount) {
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
    public IDocDocumentSource setEncoding(String encoding) {
        this.encoding = encoding;
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
    public IDocDocumentSource setConnectionProvider(ConnectionProvider<SapJCoConnection> connectionProvider) {
        this.connectionProvider = connectionProvider;
        return this;
    }

    /**
     * Gets handle only this IDoc.
     *
     * @return the handle only this IDoc
     */
    public String getHandleOnlyThisIDoc() {
        return handleOnlyThisIDoc;
    }

    /**
     * Sets handle only this IDoc.
     *
     * @param handleOnlyThisIDoc
     *     the handle only this IDoc
     * @return this for chaining
     */
    public IDocDocumentSource setHandleOnlyThisIDoc(String handleOnlyThisIDoc) {
        this.handleOnlyThisIDoc = handleOnlyThisIDoc;
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
    public IDocDocumentSource setAdditionalJCOProperties(Map<String, Object> additionalJCOProperties) {
        this.additionalJCOProperties = additionalJCOProperties;
        return this;
    }

}
