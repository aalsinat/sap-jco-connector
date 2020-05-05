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
package com.hawkore.mule.extensions.sap.internal.factory.server;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.hawkore.mule.extensions.sap.api.operations.SapJCoAttributes;
import com.hawkore.mule.extensions.sap.api.operations.SapJCoFunctionSourceAttributes;
import com.hawkore.mule.extensions.sap.api.operations.SapJCoIDocSourceAttributes;
import com.hawkore.mule.extensions.sap.internal.exceptions.OperationExecutionException;
import com.hawkore.mule.extensions.sap.internal.exceptions.TransactionExecutionException;
import com.hawkore.mule.extensions.sap.internal.factory.JCoFactory;
import com.hawkore.mule.extensions.sap.internal.utils.U;
import com.hawkore.mule.extensions.sap.internal.utils.X;
import com.sap.conn.idoc.IDocDocument;
import com.sap.conn.idoc.IDocDocumentIterator;
import com.sap.conn.idoc.IDocDocumentList;
import com.sap.conn.idoc.jco.JCoIDocHandler;
import com.sap.conn.idoc.jco.JCoIDocHandlerFactory;
import com.sap.conn.idoc.jco.JCoIDocQueueHandler;
import com.sap.conn.idoc.jco.JCoIDocServerContext;
import com.sap.conn.jco.AbapClassException;
import com.sap.conn.jco.AbapException;
import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoException;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoParameterList;
import com.sap.conn.jco.server.JCoServer;
import com.sap.conn.jco.server.JCoServerContext;
import com.sap.conn.jco.server.JCoServerContextInfo;
import com.sap.conn.jco.server.JCoServerErrorListener;
import com.sap.conn.jco.server.JCoServerExceptionListener;
import com.sap.conn.jco.server.JCoServerFunctionHandler;
import com.sap.conn.jco.server.JCoServerState;
import com.sap.conn.jco.server.JCoServerStateChangedListener;
import com.sap.conn.jco.server.JCoServerTIDHandler;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.mule.runtime.extension.api.runtime.operation.Result;
import org.mule.runtime.extension.api.runtime.source.SourceCallback;
import org.mule.runtime.extension.api.runtime.source.SourceCallbackContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SapJCoServerImpl implementation.
 *
 * @param <T>
 *     the type of SapJCoAttributes
 */
public class SapJCoServerImpl<T extends SapJCoAttributes>
    implements SapJCoServer, JCoServerStateChangedListener, JCoServerErrorListener, JCoServerExceptionListener,
                   JCoServerTIDHandler, JCoIDocHandlerFactory, JCoServerFunctionHandler, JCoIDocHandler,
                   JCoIDocQueueHandler {

    private static final Logger logger = LoggerFactory.getLogger(SapJCoServerImpl.class);
    private final Set<String> aliveTransactions = new HashSet();
    private final JCoServer jCoServer;
    private final JCoFactory jCoFactory;
    private final SourceCallback<InputStream, T> sourceCallback;
    private final String encoding;
    private JCoDestination destination;
    private final String handleOnlyThisAbapObjectIdentifier;
    private final Predicate<String> abapObjectIdentifierFilter;

    /**
     * Instantiates a new Sap JCo server.
     *
     * @param jCoFactory
     *     the JCo factory
     * @param jCoServer
     *     the server
     * @param destination
     *     the destination
     * @param sourceCallback
     *     the source callback
     * @param encoding
     *     the encoding
     * @param handleOnlyThisAbapObjectIdentifier
     *     the uni
     */
    public SapJCoServerImpl(JCoFactory jCoFactory,
        JCoServer jCoServer,
        JCoDestination destination,
        SourceCallback<InputStream, T> sourceCallback,
        String encoding,
        @Nullable String handleOnlyThisAbapObjectIdentifier) {
        this.jCoServer = jCoServer;
        this.jCoFactory = jCoFactory;
        this.destination = destination;
        this.sourceCallback = sourceCallback;
        this.encoding = encoding; // == null ? "UTF-8" : encoding;
        this.handleOnlyThisAbapObjectIdentifier = handleOnlyThisAbapObjectIdentifier;
        this.abapObjectIdentifierFilter = X.isEmpty(handleOnlyThisAbapObjectIdentifier)
                                              ? null
                                              : id -> id.equals(handleOnlyThisAbapObjectIdentifier);
    }

    /**
     * Start.
     */
    @Override
    public synchronized void start() {
        this.jCoServer.addServerStateChangedListener(this);
        this.jCoServer.addServerErrorListener(this);
        this.jCoServer.addServerExceptionListener(this);
        this.jCoServer.setTIDHandler(this);
        this.jCoServer.start();
    }

    /**
     * Stop.
     */
    @Override
    public synchronized void stop() {
        if (StringUtils.equalsAny(this.jCoServer.getState().name(), JCoServerState.STARTED.name(),
            JCoServerState.ALIVE.name())) {
            // stops only if server is running (ALIVE or STARTED)
            this.jCoServer.stop();
        }
    }

    /**
     * On success.
     *
     * @param transactionId
     *     the transaction id
     */
    @Override
    public synchronized void onSuccess(String transactionId) {
        try {
            this.destination.confirmTID(transactionId);
        } catch (JCoException e) {
            throw new TransactionExecutionException(e);
        }
    }

    /**
     * On error.
     *
     * @param transactionId
     *     the transaction id
     */
    @Override
    public void onError(String transactionId) {
    }

    /**
     * Do notify success.
     *
     * @param context
     *     the context
     */
    @Override
    public synchronized void doNotifySuccess(SourceCallbackContext context) {
        context.notify();
    }

    /**
     * Do notify exception.
     *
     * @param context
     *     the context
     */
    @Override
    public synchronized void doNotifyException(SourceCallbackContext context) {
        context.notify();
    }

    /**
     * Gets jco server.
     *
     * @return the jco server
     */
    @Override
    public JCoServer getJCoServer() {
        return this.jCoServer;
    }

    /**
     * Gets JCo factory.
     *
     * @return the JCo factory
     */
    @Override
    public JCoFactory getJCoFactory() {
        return jCoFactory;
    }

    /**
     * Server error occurred.
     *
     * @param jCoServer
     *     the JCo server
     * @param connectionId
     *     the connection id
     * @param contextInfo
     *     the context info
     * @param error
     *     the error
     */
    @Override
    public synchronized void serverErrorOccurred(JCoServer jCoServer,
        String connectionId,
        JCoServerContextInfo contextInfo,
        Error error) {
        logger.error("JCoServer for programId [{}] reported an ERROR: '{}'. Stopping connection [{}]",
            jCoServer.getProgramID(), error.getMessage(), connectionId);
        this.stop();
    }

    /**
     * Server exception occurred.
     *
     * @param jCoServer
     *     the JCo server
     * @param connectionId
     *     the connection id
     * @param serverCtx
     *     the JCoServer serverCtx
     * @param exception
     *     the exception
     */
    @Override
    public synchronized void serverExceptionOccurred(JCoServer jCoServer,
        String connectionId,
        JCoServerContextInfo serverCtx,
        Exception exception) {
        logger.warn("JCoServer for programId [{}] reported an EXCEPTION. Connection [{}].", jCoServer.getProgramID(),
            connectionId, exception);
    }

    /**
     * Server state change occurred.
     *
     * @param jCoServer
     *     the JCo server
     * @param oldState
     *     the old state
     * @param newState
     *     the new state
     */
    @Override
    public synchronized void serverStateChangeOccurred(JCoServer jCoServer,
        JCoServerState oldState,
        JCoServerState newState) {
        if (logger.isDebugEnabled()) {
            logger
                .debug("JCoServer for programId [{}] reported a STATUS CHANGE: '{}' -> '{}'.", jCoServer.getProgramID(),
                    oldState.name(), newState.name());
        }
    }

    /**
     * Check tid boolean.
     *
     * @param jCoServerContext
     *     the JCo server context
     * @param transactionId
     *     the transaction id
     * @return the boolean
     */
    @Override
    public synchronized boolean checkTID(JCoServerContext jCoServerContext, String transactionId) {
        return !this.aliveTransactions.contains(transactionId);
    }

    /**
     * Confirm tid.
     *
     * @param jCoServerContext
     *     the JCo server context
     * @param transactionId
     *     the transaction id
     */
    @Override
    public synchronized void confirmTID(JCoServerContext jCoServerContext, String transactionId) {
        this.aliveTransactions.remove(transactionId);
    }

    /**
     * Commit.
     *
     * @param jCoServerContext
     *     the JCo server context
     * @param transactionId
     *     the transaction id
     */
    @Override
    public void commit(JCoServerContext jCoServerContext, String transactionId) {
        // nothing to do
    }

    /**
     * Rollback.
     *
     * @param jCoServerContext
     *     the JCo server context
     * @param transactionId
     *     the transaction id
     */
    @Override
    public synchronized void rollback(JCoServerContext jCoServerContext, String transactionId) {
        this.aliveTransactions.remove(transactionId);
    }

    /**
     * Gets IDoc handler.
     *
     * @param jCoIDocServerContext
     *     the JCo IDoc server context
     * @return the IDoc handler
     */
    @Override
    public JCoIDocHandler getIDocHandler(JCoIDocServerContext jCoIDocServerContext) {
        return this;
    }

    /**
     * Handle request for incoming IDoc
     *
     * @param jCoServerContext
     *     the JCo server context
     * @param iDocDocumentList
     *     the doc document list (of same IDoc type)
     */
    @Override
    public synchronized void handleRequest(JCoServerContext jCoServerContext, IDocDocumentList iDocDocumentList) {
        if (iDocDocumentList != null) {
            String transactionId = jCoServerContext.getTID();
            IDocDocumentIterator it = iDocDocumentList.iterator();
            // for each IDoc within package -> send it to source callback for processing
            while (it.hasNext()) {
                IDocDocument idoc = it.next();
                // compute idoc identifier
                String idocType = X.isEmpty(idoc.getIDocTypeExtension())
                                      ? idoc.getIDocType()
                                      : idoc.getIDocType() + "-" + idoc.getIDocTypeExtension();
                // compute idoc name
                String idocName = X.isEmpty(idoc.getIDocTypeExtension())
                                      ? idoc.getIDocType()
                                      : idoc.getIDocTypeExtension();
                if (abapObjectIdentifierFilter != null && !abapObjectIdentifierFilter.test(idocType)) {
                    // received Idoc not match
                    if (logger.isDebugEnabled()) {
                        logger.debug(
                            "Received IDoc package of type '{}' will be discarded, does not match '{}' handled by "
                                + "this server", idocType, handleOnlyThisAbapObjectIdentifier);
                    }
                    // finish processing as iDoc type is the same within a package
                    break;
                }
                // add transactionId into alive transactions if not present
                this.aliveTransactions.add(transactionId);

                InputStream idocStream = JCoFactory.renderXML(idoc, this.encoding);
                if (logger.isDebugEnabled()) {
                    try {
                        idocStream = U.logRawContent("IDoc received -> Send it to source callback for processing:\n",
                            idocStream, "\n");
                    } catch (Exception e) {
                        logger.warn("Unable to log IDoc as XML", e);
                    }
                }
                SourceCallbackContext context = this.sourceCallback.createContext();
                // set transaction id variable into context
                context.addVariable(JCO_SERVER_TRANSACTION_ID, transactionId);
                this.sourceCallback.handle(Result.<InputStream, T>builder().attributes(
                    (T)new SapJCoIDocSourceAttributes(idocType, idocName, transactionId)).output(idocStream).build(),
                    context);
            }
        } else {
            logger.warn("Server received a NULL incomming IDoc package");
        }
    }

    /**
     * Handle request for incoming IDoc package list
     *
     * @param jCoIDocServerContext
     *     the JCo IDoc server context
     * @param iDocDocumentLists
     *     the doc document lists
     */
    @Override
    public synchronized void handleRequest(JCoIDocServerContext jCoIDocServerContext,
        IDocDocumentList[] iDocDocumentLists) {
        if (!X.isEmpty(iDocDocumentLists)) {
            // for each IDoc package -> send it to source callback for processing
            Stream.of(iDocDocumentLists)
                .forEach(idocList -> handleRequest(jCoIDocServerContext.getJCoServerContext(), idocList));
        } else {
            logger.warn("Server received a NULL incomming IDoc package LIST");
        }
    }

    /**
     * Handle request for incoming function
     *
     * <pre>
     * 1. Receive Incoming function to be handle
     * 2. Send it to source callback (process/implements the function response) and wait for response
     * 3. If response is emmited update incoming function (export parameters, changing parameters and table parameters)
     * </pre>
     *
     * @param jCoServerContext
     *     the JCo server context
     * @param incomingJCoFunction
     *     the incoming JCoFunction to process
     * @throws AbapException
     *     the abap exception
     * @throws AbapClassException
     *     the abap class exception
     */
    @Override
    public synchronized void handleRequest(JCoServerContext jCoServerContext, JCoFunction incomingJCoFunction)
        throws AbapException, AbapClassException {
        if (incomingJCoFunction != null) {
            if (abapObjectIdentifierFilter != null && !abapObjectIdentifierFilter.test(incomingJCoFunction.getName())) {
                // received JCoFunction not match
                if (logger.isDebugEnabled()) {
                    logger.debug(
                        "Received JCoFunction '{}' will be discarded, does not match '{}' handled by this server",
                        incomingJCoFunction.getName(), handleOnlyThisAbapObjectIdentifier);
                }
                return;
            }
            InputStream jcoFunctionStream = JCoFactory.renderXML(incomingJCoFunction, this.encoding);
            if (logger.isDebugEnabled()) {
                try {
                    jcoFunctionStream = U.logRawContent(
                        "JCoFunction received -> Send it to source callback for processing:\n", jcoFunctionStream,
                        "\n");
                } catch (Exception e) {
                    logger.warn("Unable to log JCoFunction as XML", e);
                }
            }
            SourceCallbackContext context = this.sourceCallback.createContext();
            String transactionId = jCoServerContext.getTID();
            // store transaction id into alive transactions
            this.aliveTransactions.add(transactionId);
            // set transaction id variable into context
            context.addVariable(JCO_SERVER_TRANSACTION_ID, transactionId);
            try {
                // send it to source callback for processing
                this.sourceCallback.handle(Result.<InputStream, T>builder().attributes(
                    (T)new SapJCoFunctionSourceAttributes(incomingJCoFunction.getName(), transactionId))
                                               .output(jcoFunctionStream).build(), context);
                // waits for finish processing incoming function
                context.wait();
            } catch (InterruptedException e) {
                throw new OperationExecutionException(
                    "Interrupted while waiting for JCoFunction '" + incomingJCoFunction.getName() + "' processing", e);
            }
            // checks if SourceCallback Context emmits a jcoFunction response
            JCoFunction emmitedJCoFunctionResponse = (JCoFunction)context.getVariable(EMMITED_JCO_FUNC_RESPONSE)
                                                                      .orElse(null);
            // Response function to update incoming function
            if (emmitedJCoFunctionResponse != null) {
                if (!incomingJCoFunction.getName().equals(emmitedJCoFunctionResponse.getName())) {
                    logger.error(
                        "The emmited JCoFunction '{}' as response is NOT EQUALS to incomming JCoFunction '{}'. "
                            + "Incoming JCoFunction '{}' will not be updated. Please, fix response JCoFunction or "
                            + "return null as response if you don't need to update incoming JCoFunction.",
                        emmitedJCoFunctionResponse.getName(), incomingJCoFunction.getName(),
                        incomingJCoFunction.getName());
                    return;
                }
                if (logger.isDebugEnabled()) {
                    try {
                        U.logRawContent("Emmited JCoFunction response has been received -> update incoming one '"
                                            + emmitedJCoFunctionResponse.getName() + "' with:\n",
                            JCoFactory.renderXML(emmitedJCoFunctionResponse, this.encoding), "\n");
                    } catch (Exception e) {
                        logger.warn("Unable to log emmited JCoFunction Response as XML", e);
                    }
                }
                // UPDATE incoming jCoFunction with emmited response
                JCoParameterList incomingExportParameterList = incomingJCoFunction.getExportParameterList();
                // EXPORT PARAMS: incoming function allow update export params (defined in metadata)
                if (incomingExportParameterList != null) {
                    JCoParameterList responseExportParameterList = emmitedJCoFunctionResponse.getExportParameterList();
                    responseExportParameterList
                        .forEach(p -> incomingExportParameterList.setValue(p.getName(), p.getValue()));
                }
                // CHANGING PARAMS: incoming function allow update changing params (defined in metadata)
                JCoParameterList incomingChangingParameterList = incomingJCoFunction.getChangingParameterList();
                if (incomingChangingParameterList != null) {
                    JCoParameterList responseChangingParameterList = emmitedJCoFunctionResponse
                                                                         .getChangingParameterList();
                    responseChangingParameterList
                        .forEach(p -> incomingChangingParameterList.setValue(p.getName(), p.getValue()));
                }
                // TABLE PARAMS: (OBSOLETE by SAP) incoming function allows update table params (defined in metadata)
                JCoParameterList incomingTableParameterList = incomingJCoFunction.getTableParameterList();
                if (incomingTableParameterList != null) {
                    JCoParameterList responseTableParameterList = emmitedJCoFunctionResponse.getTableParameterList();
                    responseTableParameterList
                        .forEach(p -> incomingTableParameterList.setValue(p.getName(), p.getValue()));
                }
                if (logger.isDebugEnabled()) {
                    try {
                        U.logRawContent(
                            "Incoming JCoFunction '" + incomingJCoFunction.getName() + "' has been updated:\n",
                            JCoFactory.renderXML(incomingJCoFunction, this.encoding), "\n");
                    } catch (Exception e) {
                        logger.warn("Unable to log incoming JCoFunction as XML", e);
                    }
                }
            }
        } else {
            logger.warn("Server received a NULL incomming JCoFunction");
        }
    }

}
