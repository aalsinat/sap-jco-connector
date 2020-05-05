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

import com.hawkore.mule.extensions.sap.internal.factory.dataprovider.SapJCoDataProvider;
import org.mule.runtime.api.lifecycle.Disposable;
import org.mule.runtime.api.lifecycle.Initialisable;
import org.mule.runtime.api.lifecycle.InitialisationException;

/**
 * SapJCo Configuration Data Provider Life cycle
 * <p>
 * Register/unregister Singleton SapJCoDataProvider into SAP Environment to resolve connection properties.
 *
 * @author Alex Alsina de la Torre (alex.alsina@mango.com)
 */
public interface SapJCoConfigurationDataProviderLifeCycle extends Initialisable, Disposable {

    /**
     * Initialise.
     *
     * @throws InitialisationException
     *     the initialisation exception
     */
    @Override
    default void initialise() throws InitialisationException {
        SapJCoDataProvider.register();
    }

    /**
     * Dispose.
     */
    @Override
    default void dispose() {
        SapJCoDataProvider.unregister(this.hashCode());
    }

}
