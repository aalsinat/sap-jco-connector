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
package com.hawkore.mule.extensions.sap.internal.errors;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.hawkore.mule.extensions.sap.api.exceptions.SapJCoModuleErrorType;
import org.mule.runtime.extension.api.annotation.error.ErrorTypeProvider;
import org.mule.runtime.extension.api.error.ErrorTypeDefinition;

/**
 * {@link ErrorTypeProvider} for the generic operation
 *
 * @author Alex Alsina de la Torre (alex.alsina@mango.com)
 */
public class SapJCoErrorTypeProvider implements ErrorTypeProvider {

    /**
     * Gets error types.
     *
     * @return the error types
     */
    @SuppressWarnings("rawtypes")
    @Override
    public Set<ErrorTypeDefinition> getErrorTypes() {
        return Collections.unmodifiableSet(Stream.of(SapJCoModuleErrorType.values()).collect(Collectors.toSet()));
    }

}
