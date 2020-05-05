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
/**
 * (c) 2018 HAWKORE, S.L. - All Rights Reserved
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * <p>
 * A copy of the license terms has been included with this distribution in the LICENSE.md file.
 * ---
 * Derechos de Autor (C) 2018 HAWKORE, S.L. - Todos los derechos reservados
 * Se prohibe estrictamente la copia sin autorización de este fichero por cualquier medio
 * Propietario y confidencial
 * <p>
 * Se incluye una copia de los términos de la licencia en el archivo LICENSE.md en esta distribución.
 */
package com.hawkore.mule.extensions.sap.internal.errors;

import com.hawkore.mule.extensions.sap.api.exceptions.SapJCoModuleErrorType;
import com.hawkore.mule.extensions.sap.internal.exceptions.AbstractConnectorException;
import com.hawkore.mule.extensions.sap.internal.exceptions.CommunicationException;
import com.hawkore.mule.extensions.sap.internal.exceptions.IConnectorExceptionVisitor;
import com.hawkore.mule.extensions.sap.internal.exceptions.InvalidConfigurationException;
import com.hawkore.mule.extensions.sap.internal.exceptions.InvalidOperationParamException;
import com.hawkore.mule.extensions.sap.internal.exceptions.LogonFailureException;
import com.hawkore.mule.extensions.sap.internal.exceptions.OperationExecutionException;
import com.hawkore.mule.extensions.sap.internal.exceptions.OperationTimeoutException;
import com.hawkore.mule.extensions.sap.internal.exceptions.ResourceNotFoundException;
import com.hawkore.mule.extensions.sap.internal.exceptions.SapJCoAbapException;
import com.hawkore.mule.extensions.sap.internal.exceptions.SapJCoServerException;
import com.hawkore.mule.extensions.sap.internal.exceptions.TransactionExecutionException;
import com.hawkore.mule.extensions.sap.internal.exceptions.XmlParserException;
import com.sap.conn.jco.AbapException;
import com.sap.conn.jco.JCoException;
import org.mule.runtime.extension.api.exception.ModuleException;

/**
 * Maps a exception to a {@link ModuleException}. This class is designed to be
 * used at every operation to map any exception to a mule
 * {@link ModuleException}.<br>
 * <p>
 * See also {@link #map(Throwable)}.
 *
 * @author Alex Alsina de la Torre (alex.alsina@mango.com)
 */
public class ModuleExceptionMapper implements IConnectorExceptionVisitor<ModuleException> {

    /**
     * Private static instance
     */
    private static final ModuleExceptionMapper mapper = new ModuleExceptionMapper();

    /**
     * Non instanceable class
     */
    private ModuleExceptionMapper() {
        // no-op
    }

    /**
     * Convert any exception to a {@link ModuleException}.
     *
     * @param e
     *     exception to map to a {@link ModuleException}
     * @return the {@link ModuleException} to which the given exception is mapped
     */
    public static ModuleException map(final Throwable e) {
        return mapSapException(e).accept(mapper);
    }

    /**
     * Map sap exception abstract connector exception.
     *
     * @param aException
     *     the a exception
     * @return the abstract connector exception
     */
    public static AbstractConnectorException mapSapException(Throwable aException) {
        final AbstractConnectorException thrownException;
        if (aException instanceof JCoException) {
            JCoException e = (JCoException)aException;
            switch (e.getGroup()) {
                case JCoException.JCO_ERROR_CONFIGURATION:
                    thrownException = new InvalidConfigurationException(e);
                    break;
                case JCoException.JCO_ERROR_COMMUNICATION:
                    thrownException = new CommunicationException(e);
                    break;
                case JCoException.JCO_ERROR_LOGON_FAILURE:
                    thrownException = new LogonFailureException(e);
                    break;
                default: {
                    if (e instanceof AbapException) {
                        AbapException e2 = (AbapException)e;
                        thrownException = new SapJCoAbapException(e2.getKey(), e);
                    } else {
                        thrownException = new OperationExecutionException(e);
                    }
                }
            }
        } else if (aException instanceof AbstractConnectorException) {
            thrownException = (AbstractConnectorException)aException;
        } else {
            thrownException = new OperationExecutionException(aException);
        }
        return thrownException;
    }

    /**
     * Accept module exception.
     *
     * @param e
     *     the e
     * @return the module exception
     */
    @Override
    public ModuleException accept(final InvalidConfigurationException e) {
        return new ModuleException(SapJCoModuleErrorType.INVALID_CONFIGURATION, e);
    }

    /**
     * Accept module exception.
     *
     * @param e
     *     the e
     * @return the module exception
     */
    @Override
    public ModuleException accept(final InvalidOperationParamException e) {
        return new ModuleException(SapJCoModuleErrorType.INVALID_PARAMETER, e);
    }

    /**
     * Accept module exception.
     *
     * @param e
     *     the e
     * @return the module exception
     */
    @Override
    public ModuleException accept(final ResourceNotFoundException e) {
        return new ModuleException(SapJCoModuleErrorType.RESOURCE_NOT_FOUND, e);
    }

    /**
     * Accept module exception.
     *
     * @param e
     *     the e
     * @return the module exception
     */
    @Override
    public ModuleException accept(final OperationExecutionException e) {
        return new ModuleException(SapJCoModuleErrorType.OPERATION_ERROR, e);
    }

    /**
     * Accept module exception.
     *
     * @param e
     *     the e
     * @return the module exception
     */
    @Override
    public ModuleException accept(final OperationTimeoutException e) {
        return new ModuleException(SapJCoModuleErrorType.OPERATION_TIMEOUT, e);
    }

    /**
     * Accept module exception.
     *
     * @param e
     *     the e
     * @return the module exception
     */
    @Override
    public ModuleException accept(final LogonFailureException e) {
        return new ModuleException(SapJCoModuleErrorType.INVALID_CREDENTIALS, e);
    }

    /**
     * Accept module exception.
     *
     * @param e
     *     the e
     * @return the module exception
     */
    @Override
    public ModuleException accept(final CommunicationException e) {
        return new ModuleException(SapJCoModuleErrorType.COMMUNICATION_ERROR, e);
    }

    /**
     * Accept module exception.
     *
     * @param e
     *     the e
     * @return the module exception
     */
    @Override
    public ModuleException accept(final TransactionExecutionException e) {
        return new ModuleException(SapJCoModuleErrorType.TRANSACTION_ERROR, e);
    }

    /**
     * Accept module exception.
     *
     * @param e
     *     the e
     * @return the module exception
     */
    @Override
    public ModuleException accept(final SapJCoServerException e) {
        return new ModuleException(SapJCoModuleErrorType.JCO_SERVER_ERROR, e);
    }

    /**
     * Accept module exception.
     *
     * @param e
     *     the e
     * @return the module exception
     */
    @Override
    public ModuleException accept(final XmlParserException e) {
        return new ModuleException(SapJCoModuleErrorType.XML_PARSER_ERROR, e);
    }

    /**
     * Accept module exception.
     *
     * @param e
     *     the e
     * @return the module exception
     */
    @Override
    public ModuleException accept(final SapJCoAbapException e) {
        return new ModuleException(SapJCoModuleErrorType.ABAP_ERROR, e);
    }

}
