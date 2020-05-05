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
package com.hawkore.mule.extensions.sap.internal.factory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.hawkore.mule.extensions.sap.api.operations.SapJCoFunctionSourceAttributes;
import com.hawkore.mule.extensions.sap.api.operations.SapJCoIDocSourceAttributes;
import com.hawkore.mule.extensions.sap.internal.config.SapJCoConfiguration;
import com.hawkore.mule.extensions.sap.internal.exceptions.AbstractConnectorException;
import com.hawkore.mule.extensions.sap.internal.exceptions.OperationExecutionException;
import com.hawkore.mule.extensions.sap.internal.exceptions.OperationTimeoutException;
import com.hawkore.mule.extensions.sap.internal.exceptions.ResourceNotFoundException;
import com.hawkore.mule.extensions.sap.internal.exceptions.SapJCoServerException;
import com.hawkore.mule.extensions.sap.internal.exceptions.TransactionExecutionException;
import com.hawkore.mule.extensions.sap.internal.factory.constants.BapiRETURNParameterMessageTypes;
import com.hawkore.mule.extensions.sap.internal.factory.dataprovider.SapJCoDataProvider;
import com.hawkore.mule.extensions.sap.internal.factory.server.SapJCoServer;
import com.hawkore.mule.extensions.sap.internal.factory.server.SapJCoServerImpl;
import com.hawkore.mule.extensions.sap.internal.factory.xml.function.JCoFunctionXMLParser;
import com.hawkore.mule.extensions.sap.internal.factory.xml.function.JCoFunctionXMLRenderer;
import com.hawkore.mule.extensions.sap.internal.factory.xml.function.JCoFunctionXMLSchemaRenderer;
import com.hawkore.mule.extensions.sap.internal.factory.xml.idoc.IDocXMLSchemaRenderer;
import com.hawkore.mule.extensions.sap.internal.utils.U;
import com.hawkore.mule.extensions.sap.internal.utils.X;
import com.sap.conn.idoc.IDocDocument;
import com.sap.conn.idoc.IDocDocumentList;
import com.sap.conn.idoc.IDocMetaDataUnavailableException;
import com.sap.conn.idoc.IDocParseException;
import com.sap.conn.idoc.IDocRepository;
import com.sap.conn.idoc.IDocXMLProcessor;
import com.sap.conn.idoc.jco.JCoIDoc;
import com.sap.conn.idoc.jco.JCoIDocServer;
import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoDestinationManager;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoMetaData;
import com.sap.conn.jco.JCoParameterList;
import com.sap.conn.jco.JCoRecord;
import com.sap.conn.jco.JCoRepository;
import com.sap.conn.jco.JCoStructure;
import com.sap.conn.jco.JCoTable;
import com.sap.conn.jco.ext.ServerDataProvider;
import com.sap.conn.jco.server.DefaultServerHandlerFactory.FunctionHandlerFactory;
import com.sap.conn.jco.server.JCoServer;
import com.sap.conn.jco.server.JCoServerFactory;
import javax.xml.namespace.QName;
import org.jetbrains.annotations.Nullable;
import org.mule.metadata.xml.api.utils.XmlConstants;
import org.mule.runtime.extension.api.runtime.source.SourceCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JCo factory.
 * <p>
 * Provides relevant methods to SAP JCo connector for Mule4.
 *
 * @author Alex Alsina de la Torre (alex.alsina@mango.com)
 */
public class JCoFactory {

    private static final Logger logger = LoggerFactory.getLogger(JCoFactory.class);
    private JCoDestination destination;
    private JCoRepository repository;
    private IDocRepository iDocRepository;
    private static final String SLASH = "/";
    // SAP escape slash
    private static final String ESCAPED_SLASH = "_-";
    // SAP escape starting digit
    private static final String ESCAPED_STARTING_DIGIT = "_--3";
    private static final ExecutorService ASYNC_EXECUTOR_SERVICE = Executors.newCachedThreadPool();
    private final SapJCoConfiguration config;

    /**
     * Instantiates a new JCoFactory.
     *
     * @param config
     *     the config
     * @param destinationName
     *     the destination name
     */
    public JCoFactory(SapJCoConfiguration config, String destinationName) {
        try {
            this.config = config;
            this.destination = JCoDestinationManager.getDestination(destinationName);
            this.repository = this.destination.getRepository();
            this.iDocRepository = JCoIDoc.getIDocRepository(destination);
        } catch (JCoException e) {
            throw new ResourceNotFoundException(e);
        }
    }

    /**
     * Instantiates a new JCoFactory.
     *
     * @param config
     *     the config
     * @param destination
     *     the destination
     */
    public JCoFactory(SapJCoConfiguration config, JCoDestination destination) {
        try {
            this.config = config;
            this.destination = destination;
            this.repository = this.destination.getRepository();
            this.iDocRepository = JCoIDoc.getIDocRepository(destination);
        } catch (JCoException e) {
            throw new ResourceNotFoundException(e);
        }
    }

    /**
     * Render IDocDocumentList to XML.
     *
     * @param documentList
     *     the document list
     * @param encoding
     *     the encoding
     * @return the input stream
     */
    public static InputStream renderXML(IDocDocumentList documentList, String encoding) {
        return new ByteArrayInputStream(JCoIDoc.getIDocFactory().getIDocXMLProcessor().render(documentList, encoding,
            IDocXMLProcessor.RENDER_WITH_TABS_AND_CRLF | IDocXMLProcessor.RENDER_EMPTY_TAGS)
                                            .getBytes(Charset.forName(encoding)));
    }

    /**
     * Render IDocDocument to XML.
     *
     * @param document
     *     the document
     * @param encoding
     *     the encoding
     * @return the input stream
     */
    public static InputStream renderXML(IDocDocument document, String encoding) {
        return new ByteArrayInputStream(JCoIDoc.getIDocFactory().getIDocXMLProcessor().render(document, encoding,
            IDocXMLProcessor.RENDER_WITH_TABS_AND_CRLF | IDocXMLProcessor.RENDER_EMPTY_TAGS)
                                            .getBytes(Charset.forName(encoding)));
    }

    /**
     * Render JCoFunction to XML.
     *
     * @param function
     *     the function
     * @param encoding
     *     the encoding
     * @return the input stream
     */
    public static InputStream renderXML(JCoFunction function, String encoding) {
        return new JCoFunctionXMLRenderer().renderXML(function, encoding);
    }

    /**
     * Parse XML to IDoc document list.
     *
     * @param content
     *     the content
     * @return the doc document list
     * @throws IOException
     *     the io exception
     * @throws IDocParseException
     *     the doc parse exception
     */
    public IDocDocumentList parseIDocDocumentListXML(InputStream content) throws IOException, IDocParseException {
        return JCoIDoc.getIDocFactory().getIDocXMLProcessor().parse(this.getIDocRepository(), content);
    }

    /**
     * Parse XML to JCoFunction.
     *
     * @param content
     *     the content
     * @param encoding
     *     the encoding
     * @param disableCache
     *     the disable cache
     * @return the JCo function
     * @throws IOException
     *     the io exception
     * @throws IDocParseException
     *     the doc parse exception
     */
    public JCoFunction parseJCoFunctionXML(InputStream content, String encoding, boolean disableCache)
        throws IOException, IDocParseException {
        return new JCoFunctionXMLParser(this, disableCache).parseXML(content, encoding);
    }

    /**
     * Generates XML schema from JCoFunction metadata.
     *
     * @param jCoFunction
     *     the JCo function
     * @param encoding
     *     the encoding
     * @return the JCo function xml schema
     */
    public static InputStream genJCoFunctionXmlSchema(JCoFunction jCoFunction, String encoding) {
        return JCoFunctionXMLSchemaRenderer.renderXMLSchema(jCoFunction, encoding);
    }

    /**
     * Generates XML schema from IDoc metadata.
     *
     * @param document
     *     the document
     * @param encoding
     *     the encoding
     * @return the IDoc document xml schema
     */
    public static InputStream getIDocDocumentXmlSchema(IDocDocument document, String encoding) {
        return IDocXMLSchemaRenderer.renderXMLSchema(document, encoding);
    }

    /**
     * Escape SAP name to tag name.
     *
     * @param name
     *     the name
     * @return the string
     */
    public static String escapeSapName(String name) {
        try {
            String escaped = name.replaceAll(SLASH, ESCAPED_SLASH);
            if (Character.isDigit(name.charAt(0))) {
                escaped = ESCAPED_STARTING_DIGIT + escaped;
            }
            return escaped;
        } catch (Exception e) {
            return name;
        }
    }

    /**
     * Unescape tag name to SAP name.
     *
     * @param name
     *     the name
     * @return the string
     */
    public static String unEscapeSapName(String name) {
        return name.replaceAll(ESCAPED_SLASH, SLASH).replaceAll(ESCAPED_STARTING_DIGIT, "");
    }

    /**
     * Process BAPI RETURN parameter and throw exception if error found.
     * <p>
     * The return parameter RETURN contains success or error messages for the BAPI, and depending on the SAP version
     * the (common) relevant fields of these structures are:
     * <p>
     * TYPE (Message type: S(uccess), E(rror), W(arning), I(nformation), A(bort))
     * <p>
     * ID (message class)
     * <p>
     * NUMBER (message number)
     * <p>
     * MESSAGE (message text)
     * <p>
     * MESSAGE_V1 , MESSAGE_V2 , MESSAGE_V3 , MESSAGE_V4 (message variables)
     * <p>
     * If the transmission is successful, RETURN is either completely empty (all the fields have their initial fields
     * for their types), or only the TYPE field has the value 'S'. Refer to the documentation to find out which
     * applies to the BAPI you are using.
     *
     * @param function
     *     the function to process
     */
    public static void processBapiReturnParameter(final JCoFunction function) {
        if (function == null) {
            return;
        }
        JCoParameterList parameterList = function.getExportParameterList();
        try {
            if (parameterList != null && parameterList.getListMetaData().hasField("RETURN")) {
                JCoStructure bapiReturn = parameterList.getStructure("RETURN");
                processAndThrowBAPIError(function.getName(), bapiReturn);
            }
        } catch (AbstractConnectorException e) {
            throw e;
        } catch (Exception e) {
            // Silent - unable to process RETURN export parameter (not found)
        }
        parameterList = function.getTableParameterList();
        try {
            if (parameterList != null && parameterList.getListMetaData().hasField("RETURN")) {
                JCoTable bapiReturn = parameterList.getTable("RETURN");
                for (int i = 0; i < bapiReturn.getNumRows(); i++) {
                    bapiReturn.setRow(i);
                    processAndThrowBAPIError(function.getName(), bapiReturn);
                }
            }
        } catch (AbstractConnectorException e) {
            throw e;
        } catch (Exception e) {
            // Silent - unable to process RETURN table (not found)
        }
    }

    private static void processAndThrowBAPIError(String functionName, JCoRecord record) {
        if (record == null) {
            return;
        }
        String errorLevel = "N/A";
        String errorMessage = "N/A";
        String type = record.getString("TYPE");
        String errorType = type;
        try {
            errorLevel = BapiRETURNParameterMessageTypes.valueOf(type.toUpperCase()).getStatus();
        } catch (Exception e) {
            // unable to decode error type
        }
        try {
            errorMessage = record.getString("MESSAGE");
        } catch (Exception e) {
            // silent - message not found
        }
        processAndThrowBAPIError(functionName, errorType, errorLevel, errorMessage);
    }

    private static void processAndThrowBAPIError(String functionName,
        String errorType,
        String errorLevel,
        String errorMessage) {
        if (!X.isEmpty(errorType)) {
            switch (errorType.toUpperCase()) {
                case "E":
                case "A":
                    logger.error("Error found on BAPI [{}] response (RETURN parameter). TYPE={}, LEVEL={}, MESSAGE={}",
                        functionName, errorType, errorLevel, errorMessage);
                    throw new OperationExecutionException(String.format(
                        "Error found on BAPI [%s] response (RETURN parameter). TYPE=%s, LEVEL=%s, MESSAGE=%s",
                        functionName, errorType, errorLevel, errorMessage));
                case "I":
                    logger.info("BAPI [{}] response (RETURN parameter). TYPE={}, LEVEL={}, MESSAGE={}", functionName,
                        errorType, errorLevel, errorMessage);
                    break;
                case "W":
                    logger.warn("BAPI [{}] response (RETURN parameter). TYPE={}, LEVEL={}, MESSAGE={}", functionName,
                        errorType, errorLevel, errorMessage);
                    break;
                default:
                    logger.debug("BAPI [{}] response (RETURN parameter). TYPE={}, LEVEL={}, MESSAGE={}", functionName,
                        errorType, errorLevel, errorMessage);
            }
        }
    }

    /**
     * Gets repository.
     *
     * @return the repository
     */
    public JCoRepository getRepository() {
        return repository;
    }

    /**
     * Gets destination.
     *
     * @return the destination
     */
    public JCoDestination getDestination() {
        return destination;
    }

    /**
     * Gets IDoc repository.
     *
     * @return the IDoc repository
     */
    public IDocRepository getIDocRepository() {
        return iDocRepository;
    }

    /**
     * Gets JCoFunction.
     *
     * @param functionName
     *     the function name
     * @param disableCache
     *     the disable cache
     * @return the function
     */
    public JCoFunction getFunction(String functionName, boolean disableCache) {
        try {
            JCoFunction jCoFunction = null;
            if (!X.isEmpty(functionName)) {
                if (disableCache) {
                    this.destination.getRepository().clear();
                }
                jCoFunction = this.destination.getRepository().getFunction(functionName);
            }
            if (jCoFunction == null) {
                throw new ResourceNotFoundException(String.format("Missing function [%s]", functionName));
            } else {
                return jCoFunction;
            }
        } catch (JCoException e) {
            throw new OperationExecutionException(
                String.format("An error occurred while retrieving the function [%s]", functionName), e);
        }
    }

    /**
     * Create IDoc.
     *
     * @param idocType
     *     the idoc type
     * @return the doc document
     * @throws JCoException
     *     the JCo exception
     * @throws IDocMetaDataUnavailableException
     *     the doc meta data unavailable exception
     */
    public IDocDocument createIDoc(String idocType) throws JCoException, IDocMetaDataUnavailableException {
        String[] type = idocType.split("-");
        return createIDoc(X.idxValue(type, 0), X.idxValue(type, 1), X.idxValue(type, 2), X.idxValue(type, 3));
    }

    /**
     * Create IDoc.
     *
     * @param iDocType
     *     the doc type
     * @param iDocTypeExtension
     *     the doc type extension
     * @param systemRelease
     *     the system release
     * @param applicationRelease
     *     the application release
     * @return the doc document
     */
    public IDocDocument createIDoc(String iDocType,
        String iDocTypeExtension,
        String systemRelease,
        String applicationRelease) {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug(
                    "Create iDoc [iDocType={}, iDocTypeExtension={}, systemRelease={}, " + "applicationRelease={}]",
                    iDocType, iDocTypeExtension, systemRelease, applicationRelease);
            }
            return JCoIDoc.getIDocFactory()
                       .createIDocDocument(JCoIDoc.getIDocRepository(this.destination), iDocType, iDocTypeExtension,
                           systemRelease, applicationRelease);
        } catch (IDocMetaDataUnavailableException e) {
            throw new ResourceNotFoundException(e);
        } catch (JCoException e) {
            throw new OperationExecutionException(e);
        }
    }

    private void registerJCoServer(String gatewayHost,
        String gatewayService,
        Integer connectionCount,
        String programId,
        Map<String, Object> additionalJCOProperties) {
        // register server properties
        Properties properties = new Properties();
        properties.put(ServerDataProvider.JCO_GWHOST, gatewayHost);
        properties.put(ServerDataProvider.JCO_GWSERV, gatewayService);
        properties.put(ServerDataProvider.JCO_PROGID, programId);
        properties.put(ServerDataProvider.JCO_CONNECTION_COUNT, connectionCount.toString());
        properties.put(ServerDataProvider.JCO_REP_DEST, this.destination.getDestinationName());
        U.addProperties(properties, additionalJCOProperties);
        SapJCoDataProvider.getInstance().registerServer(this.config.hashCode(), programId, properties);
    }

    /**
     * Create IDoc server.
     *
     * @param gatewayHost
     *     the gateway host
     * @param gatewayService
     *     the gateway service
     * @param connectionCount
     *     the connection count
     * @param programId
     *     the program id
     * @param handledIdocType
     *     the handled idoc type
     * @param encoding
     *     the encoding
     * @param additionalJCOProperties
     *     the additional jco properties
     * @param sourceCallback
     *     the source callback
     * @return the sap JCo server
     */
    public SapJCoServer createIDocServer(String gatewayHost,
        String gatewayService,
        Integer connectionCount,
        String programId,
        @Nullable String handledIdocType,
        String encoding,
        Map<String, Object> additionalJCOProperties,
        SourceCallback<InputStream, SapJCoIDocSourceAttributes> sourceCallback) {
        try {
            // register server properties
            registerJCoServer(gatewayHost, gatewayService, connectionCount, programId, additionalJCOProperties);
            // obtain JCoIDocServer server instance
            JCoIDocServer iDocServer = JCoIDoc.getServer(programId);
            // register idoc handler
            SapJCoServerImpl<SapJCoIDocSourceAttributes> sapJCoServer
                = new SapJCoServerImpl<SapJCoIDocSourceAttributes>(this, iDocServer, this.destination, sourceCallback,
                encoding, handledIdocType);
            iDocServer.setIDocHandlerFactory(sapJCoServer);
            return sapJCoServer;
        } catch (JCoException e) {
            throw new SapJCoServerException(programId, e);
        }
    }

    /**
     * Creates JCo function server.
     *
     * @param gatewayHost
     *     the gateway host
     * @param gatewayService
     *     the gateway service
     * @param connectionCount
     *     the connection count
     * @param programId
     *     the program id
     * @param handledFunctionName
     *     the handled function name
     * @param encoding
     *     the encoding
     * @param additionalJCOProperties
     *     the additional jco properties
     * @param sourceCallback
     *     the source callback
     * @return the sap JCo server
     */
    public SapJCoServer createJCoFunctionServer(String gatewayHost,
        String gatewayService,
        Integer connectionCount,
        String programId,
        @Nullable String handledFunctionName,
        String encoding,
        Map<String, Object> additionalJCOProperties,
        SourceCallback<InputStream, SapJCoFunctionSourceAttributes> sourceCallback) {
        try {
            // register server properties
            registerJCoServer(gatewayHost, gatewayService, connectionCount, programId, additionalJCOProperties);
            // obtain JCoServer server instance
            JCoServer server = JCoServerFactory.getServer(programId);
            FunctionHandlerFactory factory = new FunctionHandlerFactory();
            // register the function handler
            SapJCoServerImpl<SapJCoFunctionSourceAttributes> sapJCoServer
                = new SapJCoServerImpl<SapJCoFunctionSourceAttributes>(this, server, this.destination, sourceCallback,
                encoding, handledFunctionName);
            if (X.isEmpty(handledFunctionName)) {
                // can receive any JCoFunction
                factory.registerGenericHandler(sapJCoServer);
            } else {
                // can receive only provided JCoFunction
                factory.registerHandler(handledFunctionName, sapJCoServer);
            }
            server.setCallHandlerFactory(factory);
            return sapJCoServer;
        } catch (JCoException e) {
            SapJCoDataProvider.getInstance().unregisterServer(this.config.hashCode(), programId);
            throw new SapJCoServerException(programId, e);
        }
    }

    /**
     * Create transaction id.
     *
     * @return the string
     */
    public String createTransactionId() {
        try {
            return this.getDestination().createTID();
        } catch (JCoException e) {
            throw new TransactionExecutionException(e);
        }
    }

    /**
     * Confirm transaction.
     *
     * @param transactionId
     *     the transaction id
     */
    public void confirmTransaction(String transactionId) {
        try {
            this.getDestination().confirmTID(transactionId);
        } catch (JCoException e) {
            throw new TransactionExecutionException(e);
        }
    }

    /**
     * Execute callable with timeout.
     *
     * @param task
     *     the task
     * @param timeout
     *     the timeout
     * @param timeoutUnit
     *     the timeout unit
     * @throws Throwable
     *     the throwable
     */
    public void executeWithTimeout(Callable<Void> task, long timeout, TimeUnit timeoutUnit) throws Throwable {
        Future futureTask = ASYNC_EXECUTOR_SERVICE.submit(task);
        try {
            long init = System.currentTimeMillis();
            if (timeout == 0L) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Running operation with no timeout...");
                }
                // waits until completed or error
                futureTask.get();
            } else {
                if (logger.isDebugEnabled()) {
                    logger.debug(String.format("Waiting %s %s for operation ends...", timeout, timeoutUnit));
                }
                futureTask.get(timeout, timeoutUnit);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Operation executed in {} ms", (System.currentTimeMillis() - init));
            }
        } catch (TimeoutException e) {
            throw new OperationTimeoutException("Operation timeout.", e);
        } catch (InterruptedException e) {
            throw new OperationExecutionException("Operation interrupted.", e);
        } catch (ExecutionException e) {
            throw e.getCause();
        } finally {
            try {
                futureTask.cancel(true);
            } catch (Throwable e) {
                //silent
            }
        }
    }

    /**
     * Execute JCo function over sRFC.
     *
     * @param function
     *     the function
     * @param timeout
     *     the timeout
     * @param timeoutUnit
     *     the timeout unit
     * @throws Throwable
     *     the throwable
     */
    public void executeJCoFunction(JCoFunction function, long timeout, TimeUnit timeoutUnit) throws Throwable {
        if (logger.isDebugEnabled()) {
            logger.debug("Execute function [{}] over sRFC.", function.getName());
        }
        this.executeWithTimeout(() -> {
            function.execute(this.destination);
            return null;
        }, timeout, timeoutUnit);
    }

    /**
     * Execute JCo function over tRFC.
     *
     * @param function
     *     the function
     * @param transactionId
     *     the transaction id
     * @param timeout
     *     the timeout
     * @param timeoutUnit
     *     the timeout unit
     * @throws Throwable
     *     the throwable
     */
    public void executeJCoFunction(JCoFunction function, String transactionId, long timeout, TimeUnit timeoutUnit)
        throws Throwable {
        if (logger.isDebugEnabled()) {
            logger.debug("Execute function [{}] over tRFC within transaction [{}].", function.getName(), transactionId);
        }
        this.executeWithTimeout(() -> {
            function.execute(this.destination, transactionId);
            return null;
        }, timeout, timeoutUnit);
    }

    /**
     * Execute JCo function over qRFC.
     *
     * @param function
     *     the function
     * @param transactionId
     *     the transaction id
     * @param queueName
     *     the queue name
     * @param timeout
     *     the timeout
     * @param timeoutUnit
     *     the timeout unit
     * @throws Throwable
     *     the throwable
     */
    public void executeJCoFunction(JCoFunction function,
        String transactionId,
        String queueName,
        long timeout,
        TimeUnit timeoutUnit) throws Throwable {
        if (logger.isDebugEnabled()) {
            logger.debug("Execute function [{}] over qRFC to queue [{}] within transaction [{}].", function.getName(),
                queueName, transactionId);
        }
        this.executeWithTimeout(() -> {
            function.execute(this.destination, transactionId, queueName);
            return null;
        }, timeout, timeoutUnit);
    }

    /**
     * Send IDoc over tRFC.
     *
     * @param iDocDocumentList
     *     the doc document list
     * @param version
     *     the version
     * @param transactionId
     *     the transaction id
     * @param timeout
     *     the timeout
     * @param timeoutUnit
     *     the timeout unit
     * @throws Throwable
     *     the throwable
     */
    public void sendIDoc(IDocDocumentList iDocDocumentList,
        char version,
        String transactionId,
        long timeout,
        TimeUnit timeoutUnit) throws Throwable {
        if (logger.isDebugEnabled()) {
            logger
                .debug("Send IDoc [{}] version [{}] over tRFC within transaction [{}].", iDocDocumentList.getIDocType(),
                    version, transactionId);
        }
        this.executeWithTimeout(() -> {
            JCoIDoc.send(iDocDocumentList, version, this.destination, transactionId);
            return null;
        }, timeout, timeoutUnit);
    }

    /**
     * Send IDoc over qRFC.
     *
     * @param iDocDocumentList
     *     the doc document list
     * @param version
     *     the version
     * @param transactionId
     *     the transaction id
     * @param queueName
     *     the queue name
     * @param timeout
     *     the timeout
     * @param timeoutUnit
     *     the timeout unit
     * @throws Throwable
     *     the throwable
     */
    public void sendIDoc(IDocDocumentList iDocDocumentList,
        char version,
        String transactionId,
        String queueName,
        long timeout,
        TimeUnit timeoutUnit) throws Throwable {
        if (logger.isDebugEnabled()) {
            logger.debug("Send IDoc [{}] version [{}] over qRFC to queue [{}] within transaction [{}].",
                iDocDocumentList.getIDocType(), version, queueName, transactionId);
        }
        this.executeWithTimeout(() -> {
            JCoIDoc.send(iDocDocumentList, version, this.destination, transactionId, queueName);
            return null;
        }, timeout, timeoutUnit);
    }

    /**
     * Gets class for JCo field type.
     *
     * @param type
     *     the type
     * @return the class for JCo field type
     */
    public static Class getClassForJCoFieldType(int type) {
        switch (type) {
            case JCoMetaData.TYPE_CHAR:
            case JCoMetaData.TYPE_NUM:
            case JCoMetaData.TYPE_STRING:
                return java.lang.String.class;
            case JCoMetaData.TYPE_DATE:
            case JCoMetaData.TYPE_TIME:
                return java.util.Date.class;
            case JCoMetaData.TYPE_BCD:
            case JCoMetaData.TYPE_DECF16:
            case JCoMetaData.TYPE_DECF34:
                return java.math.BigDecimal.class;
            case JCoMetaData.TYPE_BYTE:
            case JCoMetaData.TYPE_XSTRING:
                return byte[].class;
            case JCoMetaData.TYPE_FLOAT:
                return java.lang.Double.class;
            case JCoMetaData.TYPE_INT:
            case JCoMetaData.TYPE_INT1:
            case JCoMetaData.TYPE_INT2:
                return java.lang.Integer.class;
            case JCoMetaData.TYPE_STRUCTURE:
            case JCoMetaData.TYPE_BOX:
            case JCoMetaData.TYPE_GENERIC_BOX:
                return com.sap.conn.jco.JCoStructure.class;
            case JCoMetaData.TYPE_INT8:
                return java.lang.Long.class;
            case JCoMetaData.TYPE_TABLE:
                return com.sap.conn.jco.JCoTable.class;
            default:
                return java.lang.Object.class;
        }
    }

    /**
     * Gets QName for JCo field type.
     *
     * @param type
     *     the type
     * @return the q name for JCo field type
     */
    public static QName getQNameForJCoFieldType(int type) {
        switch (type) {
            case JCoMetaData.TYPE_CHAR:
            case JCoMetaData.TYPE_NUM:
            case JCoMetaData.TYPE_STRING:
                return XmlConstants.XSD_STRING;
            case JCoMetaData.TYPE_DATE:
                return XmlConstants.XSD_DATE;
            case JCoMetaData.TYPE_TIME:
                return XmlConstants.XSD_TIME;
            case JCoMetaData.TYPE_BCD:
            case JCoMetaData.TYPE_DECF16:
            case JCoMetaData.TYPE_DECF34:
                return XmlConstants.XSD_DECIMAL;
            case JCoMetaData.TYPE_BYTE:
            case JCoMetaData.TYPE_XSTRING:
                return XmlConstants.XSD_BASE64;
            case JCoMetaData.TYPE_FLOAT:
                return XmlConstants.XSD_FLOAT;
            case JCoMetaData.TYPE_INT:
                return XmlConstants.XSD_INTEGER;
            case JCoMetaData.TYPE_INT1:
                return XmlConstants.XSD_BYTE;
            case JCoMetaData.TYPE_INT2:
                return XmlConstants.XSD_SHORT;
            case JCoMetaData.TYPE_INT8:
                return XmlConstants.XSD_LONG;
            default:
                return XmlConstants.XSD_ANY;
        }
    }

}
