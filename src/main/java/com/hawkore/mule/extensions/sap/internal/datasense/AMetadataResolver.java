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
package com.hawkore.mule.extensions.sap.internal.datasense;

import java.util.Optional;

import com.hawkore.mule.extensions.sap.internal.config.SapJCoConfiguration;
import com.hawkore.mule.extensions.sap.internal.config.SapJCoConnection;
import com.hawkore.mule.extensions.sap.internal.exceptions.CommunicationException;
import org.mule.metadata.api.model.MetadataType;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.metadata.MetadataContext;
import org.mule.runtime.api.metadata.MetadataResolvingException;
import org.mule.runtime.api.metadata.resolving.InputTypeResolver;
import org.mule.runtime.api.metadata.resolving.OutputTypeResolver;
import org.mule.runtime.api.metadata.resolving.TypeKeysResolver;
import org.mule.runtime.extension.api.runtime.config.ConfigurationInstance;

/**
 * Abstract SAP JCo metadata resolver.
 *
 * @author Alex Alsina de la Torre (alex.alsina@mango.com)
 */
public abstract class AMetadataResolver
    implements InputTypeResolver<String>, TypeKeysResolver, OutputTypeResolver<String> {

    /**
     * The constant NONE.
     */
    public static final String NONE = "---";

    /**
     * Gets resolver name.
     *
     * @return the resolver name
     */
    @Override
    public String getResolverName() {
        return this.getClass().getSimpleName();
    }

    /**
     * Gets input metadata.
     *
     * @param metadataContext
     *     the metadata context
     * @param key
     *     the key
     * @return the input metadata
     * @throws MetadataResolvingException
     *     the metadata resolving exception
     * @throws ConnectionException
     *     the connection exception
     */
    @Override
    public MetadataType getInputMetadata(MetadataContext metadataContext, String key)
        throws MetadataResolvingException, ConnectionException {
        return this.getMetadataType(metadataContext, key);
    }

    /**
     * Gets output type.
     *
     * @param metadataContext
     *     the metadata context
     * @param key
     *     the key
     * @return the output type
     * @throws MetadataResolvingException
     *     the metadata resolving exception
     * @throws ConnectionException
     *     the connection exception
     */
    @Override
    public MetadataType getOutputType(MetadataContext metadataContext, String key)
        throws MetadataResolvingException, ConnectionException {
        return this.getMetadataType(metadataContext, key);
    }

    /**
     * Gets metadata type.
     *
     * @param metadataContext
     *     the metadata context
     * @param key
     *     the key
     * @return the metadata type
     * @throws MetadataResolvingException
     *     the metadata resolving exception
     * @throws ConnectionException
     *     the connection exception
     */
    protected abstract MetadataType getMetadataType(MetadataContext metadataContext, String key)
        throws MetadataResolvingException, ConnectionException;

    /**
     * Gets config.
     *
     * @param context
     *     the context
     * @return the config
     */
    protected SapJCoConfiguration getConfig(MetadataContext context) {
        Object config = context.getConfig().get();
        return config instanceof SapJCoConfiguration
                   ? (SapJCoConfiguration)config
                   : (SapJCoConfiguration)((ConfigurationInstance)((Optional)context.getConfig().get()).get())
                                              .getValue();
    }

    /**
     * Gets connection.
     *
     * @param context
     *     the context
     * @return the connection
     * @throws ConnectionException
     *     the connection exception
     */
    protected SapJCoConnection getConnection(final MetadataContext context) throws ConnectionException {

        return (SapJCoConnection)context.getConnection().orElseThrow(
            () -> new CommunicationException("A connection is needed for datasense resolution"));
    }

}
