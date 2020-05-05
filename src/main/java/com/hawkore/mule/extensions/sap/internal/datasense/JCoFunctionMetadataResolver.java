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

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import com.hawkore.mule.extensions.sap.internal.config.SapJCoConfiguration;
import com.hawkore.mule.extensions.sap.internal.factory.JCoFactory;
import com.hawkore.mule.extensions.sap.internal.factory.xml.function.JCoFunctionXMLSchemaRenderer;
import com.hawkore.mule.extensions.sap.internal.utils.X;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoRecordMetaData;
import com.sap.conn.jco.JCoTable;
import org.mule.metadata.api.model.MetadataFormat;
import org.mule.metadata.api.model.MetadataType;
import org.mule.metadata.xml.api.SchemaCollector;
import org.mule.metadata.xml.api.XmlTypeLoader;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.metadata.MetadataContext;
import org.mule.runtime.api.metadata.MetadataKey;
import org.mule.runtime.api.metadata.MetadataKeyBuilder;
import org.mule.runtime.api.metadata.MetadataResolvingException;
import org.mule.runtime.api.metadata.resolving.FailureCode;

/**
 * JCoFunction metadata resolver.
 *
 * @author Alex Alsina de la Torre (alex.alsina@mango.com)
 */
public class JCoFunctionMetadataResolver extends AMetadataResolver {

    private static final String DATA_SENSE_CATEGORY = "HK_SAP_JCO_FUNCTION";
    /** RFC function search */
    private static final String RFC_FUNCTION_SEARCH = "RFC_FUNCTION_SEARCH";
    /** RFC function search - import param/table field name */
    private static final String FUNCNAME = "FUNCNAME";
    /** RFC function search - import param value filter */
    private static final String FUNCNAME_FILTER = "*";
    /** RFC function search - table parameter */
    private static final String FUNCTIONS = "FUNCTIONS";
    /** RFC function search - table parameter description */
    private static final String STEXT = "STEXT";

    /**
     * Instantiates a new JCo function metadata resolver.
     */
    public JCoFunctionMetadataResolver() {

    }

    /**
     * Gets category name.
     *
     * @return the category name
     */
    @Override
    public String getCategoryName() {
        return DATA_SENSE_CATEGORY;
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
    @Override
    protected MetadataType getMetadataType(MetadataContext metadataContext, String key)
        throws MetadataResolvingException, ConnectionException {
        try {
            if (key.equals(NONE)) {
                return metadataContext.getTypeBuilder().withFormat(MetadataFormat.XML).anyType().build();
            }
            SapJCoConfiguration config = this.getConfig(metadataContext);
            SchemaCollector schemas = SchemaCollector.getInstance();
            JCoFactory jCoFactory = getConnection(metadataContext).getJCoFactory();
            JCoFunction jCoFunction = jCoFactory.getFunction(key, true);
            schemas.addSchema(key, JCoFunctionXMLSchemaRenderer.renderXMLSchema(jCoFunction, config.getEncoding()));
            return new XmlTypeLoader(schemas).load(JCoFactory.escapeSapName(jCoFunction.getName())).get();
        } catch (Exception e) {
            throw new MetadataResolvingException(e.getMessage(), FailureCode.UNKNOWN);
        }
    }

    /**
     * Gets keys.
     *
     * @param metadataContext
     *     the metadata context
     * @return the keys
     * @throws MetadataResolvingException
     *     the metadata resolving exception
     * @throws ConnectionException
     *     the connection exception
     */
    @Override
    public Set<MetadataKey> getKeys(MetadataContext metadataContext)
        throws MetadataResolvingException, ConnectionException {
        try {
            JCoFactory jCoFactory = getConnection(metadataContext).getJCoFactory();
            JCoFunction jCoFunction = jCoFactory.getFunction(RFC_FUNCTION_SEARCH, true);
            jCoFunction.getImportParameterList().setValue(FUNCNAME, FUNCNAME_FILTER);
            jCoFactory.executeJCoFunction(jCoFunction, 0, TimeUnit.MILLISECONDS);
            JCoTable functionsTable = jCoFunction.getTableParameterList().getTable(FUNCTIONS);
            Set<MetadataKey> keys = new HashSet<>();
            keys.add(MetadataKeyBuilder.newKey(NONE).build());
            String name = null;
            String desc = null;
            for (int i = 0; i < functionsTable.getNumRows(); i++) {
                functionsTable.setRow(i);
                JCoRecordMetaData meta = functionsTable.getRecordMetaData();
                name = functionsTable.getString(meta.indexOf(FUNCNAME));
                desc = functionsTable.getString(meta.indexOf(STEXT));
                desc = name + (!X.isEmpty(desc) ? " - " + desc : "");
                keys.add(MetadataKeyBuilder.newKey(name).withDisplayName(desc).build());
            }
            return keys;
        } catch (Throwable e) {
            throw new MetadataResolvingException(e.getMessage(), FailureCode.UNKNOWN);
        }
    }

}
