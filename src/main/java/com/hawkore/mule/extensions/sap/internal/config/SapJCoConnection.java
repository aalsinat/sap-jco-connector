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

import java.io.Closeable;
import java.util.concurrent.TimeUnit;

import com.hawkore.mule.extensions.sap.internal.factory.JCoFactory;
import com.hawkore.mule.extensions.sap.internal.factory.trace.SapJCoTraceListener;
import com.sap.conn.jco.JCo;
import com.sap.conn.jco.JCoContext;
import com.sap.conn.jco.JCoDestination;
import com.sap.conn.jco.JCoDestinationManager;
import com.sap.conn.jco.JCoFunction;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.api.i18n.I18nMessageFactory;
import org.mule.runtime.api.tx.TransactionException;
import org.mule.runtime.extension.api.connectivity.TransactionalConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.hawkore.mule.extensions.sap.internal.errors.ModuleExceptionMapper.mapSapException;

/**
 * SAP JCo connector's connection with transaction support
 *
 * @author Alex Alsina de la Torre (alex.alsina@mango.com)
 */
public final class SapJCoConnection implements TransactionalConnection, Closeable {

    private static final Logger logger = LoggerFactory.getLogger(SapJCoConnection.class);
    private static final long DEFAULT_TIMEOUT = 0L;
    private static final SapJCoTraceListener traceListener = new SapJCoTraceListener();
    private final JCoDestination destination;
    private final JCoFactory jCoFactory;
    private static final String BAPI_TRANSACTION_COMMIT = "BAPI_TRANSACTION_COMMIT";
    private static final String BAPI_TRANSACTION_COMMIT_WAIT = "WAIT";
    private static final String BAPI_TRANSACTION_COMMIT_WAIT_X = "X";
    private static final String BAPI_TRANSACTION_ROLLBACK = "BAPI_TRANSACTION_ROLLBACK";

    /**
     * Instantiates a new Sap JCo connection.
     *
     * @param config
     *     the config
     * @param destinationName
     *     the destination name
     * @throws ConnectionException
     *     the connection exception
     */
    public SapJCoConnection(SapJCoConfiguration config, String destinationName) throws ConnectionException {
        try {
            this.destination = JCoDestinationManager.getDestination(destinationName);
            this.jCoFactory = new JCoFactory(config, this.destination);
        } catch (Exception e) {
            throw new ConnectionException(mapSapException(e));
        }
    }

    /**
     * Begin.
     *
     * @throws TransactionException
     *     the transaction exception
     */
    @Override
    public void begin() throws TransactionException {
        try {
            JCoContext.begin(this.destination);
        } catch (Exception e) {
            throw new TransactionException(I18nMessageFactory.createStaticMessage("BEGIN transaction error"), e);
        }
    }

    /**
     * Commit.
     *
     * @throws TransactionException
     *     the transaction exception
     */
    @Override
    public void commit() throws TransactionException {
        try {
            JCoFunction commitFunction = this.jCoFactory.getFunction(BAPI_TRANSACTION_COMMIT, true);
            commitFunction.getImportParameterList()
                .setValue(BAPI_TRANSACTION_COMMIT_WAIT, BAPI_TRANSACTION_COMMIT_WAIT_X);
            if (logger.isDebugEnabled()) {
                logger.debug("Commit without Transaction ID");
            }
            this.jCoFactory.executeJCoFunction(commitFunction, DEFAULT_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (Throwable e) {
            throw new TransactionException(I18nMessageFactory.createStaticMessage("COMMIT transaction error"), e);
        } finally {
            this.end();
        }
    }

    /**
     * Rollback.
     *
     * @throws TransactionException
     *     the transaction exception
     */
    @Override
    public void rollback() throws TransactionException {
        try {
            this.jCoFactory
                .executeJCoFunction(this.jCoFactory.getFunction(BAPI_TRANSACTION_ROLLBACK, true), DEFAULT_TIMEOUT,
                    TimeUnit.MILLISECONDS);
        } catch (Throwable e) {
            throw new TransactionException(I18nMessageFactory.createStaticMessage("ROLLBACK transaction error"), e);
        } finally {
            this.end();
        }
    }

    /**
     * Close.
     */
    @Override
    public void close() {
        JCo.removeTraceListener(this.traceListener);
    }

    /**
     * Gets id.
     *
     * @return the id
     */
    public String getId() {
        return this.destination.getDestinationName();
    }

    /**
     * Disconnect.
     */
    public void disconnect() {
        this.close();
    }

    /**
     * Gets JCo factory.
     *
     * @return the JCo factory
     */
    public JCoFactory getJCoFactory() {
        return jCoFactory;
    }

    /**
     * Validate connection validation.
     *
     * @return the connection validation result.
     */
    public ConnectionValidationResult validate() {
        try {
            this.destination.ping();
            return ConnectionValidationResult.success();
        } catch (Exception e) {
            return ConnectionValidationResult.failure("Invalid connection [" + getId() + "]", mapSapException(e));
        }
    }

    private void end() throws TransactionException {
        try {
            JCoContext.end(this.destination);
        } catch (Exception e) {
            throw new TransactionException(I18nMessageFactory.createStaticMessage("END transaction error"), e);
        }
    }

}
