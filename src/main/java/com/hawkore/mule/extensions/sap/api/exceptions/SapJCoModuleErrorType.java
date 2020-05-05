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
package com.hawkore.mule.extensions.sap.api.exceptions;

import java.util.Optional;

import org.mule.runtime.extension.api.error.ErrorTypeDefinition;

/**
 * Error types for the SAP JCo connector
 *
 * @author Alex Alsina de la Torre (alex.alsina@mango.com)
 */
public enum SapJCoModuleErrorType implements ErrorTypeDefinition<SapJCoModuleErrorType> {

    /**
     * Whether operation fails due to AbapException, in error message you will get the ABAP error key
     */
    ABAP_ERROR,

    /**
     * Error parsing xml
     */
    XML_PARSER_ERROR,

    /**
     * Timeout exceeded waiting for response
     */
    OPERATION_TIMEOUT,

    /**
     * Whether operation fails
     */
    OPERATION_ERROR,

    /**
     * Whether configuration is not valid
     */
    INVALID_CONFIGURATION,

    /**
     * Invalid parameter passed at operation
     */
    INVALID_PARAMETER,

    /**
     * Whether resource not found
     */
    RESOURCE_NOT_FOUND,

    /**
     * Whether error on communicacion
     */
    COMMUNICATION_ERROR,

    /**
     * Whether error on transaction
     */
    TRANSACTION_ERROR,

    /**
     * Whether error on JCo Server
     */
    JCO_SERVER_ERROR,

    /**
     * Invalid credentials
     */
    INVALID_CREDENTIALS;

    /**
     * Gets parent.
     *
     * @return the parent
     */
    @Override
    public Optional<ErrorTypeDefinition<? extends Enum<?>>> getParent() {
        return Optional.empty();
    }
}
