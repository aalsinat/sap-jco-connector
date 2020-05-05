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
import com.hawkore.mule.extensions.sap.internal.errors.ModuleExceptionMapper;
import com.hawkore.mule.extensions.sap.internal.errors.SapJCoErrorTypeProvider;
import com.hawkore.mule.extensions.sap.internal.exceptions.InvalidOperationParamException;
import com.hawkore.mule.extensions.sap.internal.factory.JCoFactory;
import com.hawkore.mule.extensions.sap.internal.utils.U;
import com.hawkore.mule.extensions.sap.internal.utils.X;
import com.sap.conn.idoc.IDocDocumentList;
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
 * IDoc operations
 *
 * @author Alex Alsina de la Torre (alex.alsina@mango.com)
 */
public class IDocDocumentOperations {

    private static final Logger logger = LoggerFactory.getLogger(IDocDocumentOperations.class);

    /**
     * Generate a XML schema for an IDoc using its metadata. Generated XML schema can be used by
     * other applications to prepare valid XML IDoc or to define metadata types within mule applications.
     *
     * @param connection
     *     The connection
     * @param idocType
     *     The IDoc's type to generate XML Schema
     * @param encoding
     *     Overrides default encoding defined in configuration
     * @return generate XML Schema
     */
    @DisplayName("IDoc - generate XML Schema")
    @Throws({SapJCoErrorTypeProvider.class})
    @MediaType(MediaType.APPLICATION_XML)
    @Alias("idoc-schema")
    public InputStream createIdocSchema(@Connection SapJCoConnection connection,
        @DisplayName("IDoc type") @Summary("The IDoc's type to generate XML Schema")
        @MetadataKeyId(IDocDocumentMetadataResolver.class) String idocType,
        @ConfigOverride @Placement(tab = Placement.ADVANCED_TAB, order = 1) @Example("UTF-8") final String encoding) {
        try {
            if (X.isEmpty(idocType) || idocType.equals(AMetadataResolver.NONE)) {
                throw new InvalidOperationParamException("IDoc name is required to generate XML schema");
            }
            InputStream content = JCoFactory.getIDocDocumentXmlSchema(connection.getJCoFactory().createIDoc(idocType),
                encoding);
            if (logger.isDebugEnabled()) {
                content = U.logRawContent("IDoc '" + idocType + "' XML schema:\n", content, "\n");
            }
            return content;
        } catch (Throwable e) {
            throw ModuleExceptionMapper.map(e);
        }
    }

    /**
     * Creates an empty IDoc with EDI_DC segment, rendered as XML.
     *
     * @param connection
     *     The connection
     * @param idocType
     *     The IDoc's type to create an instance
     * @param encoding
     *     Overrides default encoding defined in configuration
     * @return IDoc rendered as XML
     */
    @DisplayName("IDoc - instance")
    @OutputResolver(output = IDocDocumentMetadataResolver.class)
    @Throws({SapJCoErrorTypeProvider.class})
    @MediaType(MediaType.APPLICATION_XML)
    @Alias("idoc-instance")
    public InputStream createIDocAsXML(@Connection SapJCoConnection connection,
        @DisplayName("IDoc type") @Summary("The IDoc's type to create an instance")
        @MetadataKeyId(IDocDocumentMetadataResolver.class) String idocType,
        @Summary("Overrides default encoding defined in configuration") @ConfigOverride
        @Placement(tab = Placement.ADVANCED_TAB, order = 1) @Example("UTF-8") final String encoding) {
        try {
            if (X.isEmpty(idocType) || idocType.equals(AMetadataResolver.NONE)) {
                throw new InvalidOperationParamException("IDoc name is required to create it");
            }
            InputStream content = JCoFactory.renderXML(connection.getJCoFactory().createIDoc(idocType), encoding);
            if (logger.isDebugEnabled()) {
                content = U.logRawContent("Rendered IDoc '" + idocType + "' as XML:\n", content, "\n");
            }
            return content;
        } catch (Throwable e) {
            throw ModuleExceptionMapper.map(e);
        }
    }

    /**
     * Sends an IDoc package.
     *
     * @param connection
     *     The connection
     * @param idocType
     *     Optional IDoc's type to work with datasense metadata resolution
     * @param content
     *     The IDoc as XML content to send
     * @param transactionId
     *     The transaction id
     * @param queueName
     *     An optional parameter used in case when the IDoc package shall be sent via queued RFC (qRFC)
     * @param version
     *     Overrides the version of the IDoc defined in configuration
     * @param operationTimeout
     *     Overrides default timeout defined in configuration
     * @param operationTimeoutUnit
     *     Overrides default timeout unit defined in configuration
     */
    @DisplayName("IDoc - send")
    @Throws({SapJCoErrorTypeProvider.class})
    @Alias("idoc-send")
    public void sendIDoc(@Connection SapJCoConnection connection,
        @Optional(defaultValue = AMetadataResolver.NONE) @MetadataKeyId(IDocDocumentMetadataResolver.class)
        @DisplayName("IDoc type") @Summary("Optional IDoc's type to work with datasense metadata resolution")
            String idocType,
        @DisplayName("IDoc XML content") @Summary("The IDoc as XML content to send") @Content
        @TypeResolver(IDocDocumentMetadataResolver.class) InputStream content,
        @Optional @Summary("The Transaction ID") String transactionId,
        @Optional @Summary(
            "An optional parameter used in case when the IDoc package shall be sent via queued RFC (qRFC). This name "
                + "is also used as the queue name at IDoc application layer queueing") String queueName,
        @ConfigOverride @Placement(tab = Placement.ADVANCED_TAB, order = 1) @Summary(
            "Overrides the version of the IDoc defined in configuration. See com.sap.conn.idoc.IDocFactory "
                + "documentation for supported values") @DisplayName("IDoc version") Character version,
        @ConfigOverride @Placement(tab = Placement.ADVANCED_TAB, order = 2)
        @Summary("Overrides default timeout defined in configuration") Long operationTimeout,
        @ConfigOverride @Placement(tab = Placement.ADVANCED_TAB, order = 3)
        @Summary("Overrides default timeout unit defined in configuration") TimeUnit operationTimeoutUnit) {
        try {
            JCoFactory jCoFactory = connection.getJCoFactory();
            InputStream idocStream = content;
            if (logger.isDebugEnabled()) {
                idocStream = U.logRawContent("Input XML IDoc to send:\n", idocStream, "\n");
            }
            IDocDocumentList iDocuments = jCoFactory.parseIDocDocumentListXML(idocStream);
            if (!X.isEmpty(queueName)) {
                // Sends an IDoc package to the destination by using the given transactionId and queueName.
                jCoFactory
                    .sendIDoc(iDocuments, version, transactionId, queueName, operationTimeout, operationTimeoutUnit);
            } else {
                // Sends an IDocDocumentList to the destination by using the given transactionId.
                jCoFactory.sendIDoc(iDocuments, version, transactionId, operationTimeout, operationTimeoutUnit);
            }
        } catch (Throwable e) {
            throw ModuleExceptionMapper.map(e);
        }
    }

}
