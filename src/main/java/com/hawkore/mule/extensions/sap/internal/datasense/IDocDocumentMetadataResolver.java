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
import com.hawkore.mule.extensions.sap.internal.factory.xml.idoc.IDocXMLSchemaRenderer;
import com.hawkore.mule.extensions.sap.internal.utils.X;
import com.sap.conn.idoc.IDocDocument;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoRecordMetaData;
import com.sap.conn.jco.JCoTable;
import org.apache.commons.lang3.StringUtils;
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
 * IDocDocument metadata resolver.
 *
 * @author Alex Alsina de la Torre (alex.alsina@mango.com)
 */
public class IDocDocumentMetadataResolver extends AMetadataResolver {

    private static final String DATA_SENSE_CATEGORY = "HK_SAP_JCO_IDOC";
    /** RFC IDOCTYPES_LIST_WITH_MESSAGES */
    private static final String IDOCTYPES_LIST_WITH_MESSAGES = "IDOCTYPES_LIST_WITH_MESSAGES";
    /** response table IDoc types */
    private static final String PT_IDOCTYPES = "PT_IDOCTYPES";
    /** response table field IDoc type */
    private static final String IDOCTYP = "IDOCTYP";
    /** response table field IDoc description */
    private static final String DESCRP = "DESCRP";
    /** response table IDoc extended types */
    private static final String PT_EXTTYPES = "PT_EXTTYPES";
    /** response table field extended type */
    private static final String CIMTYP = "CIMTYP";

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
            String[] type = key.split("-");
            IDocDocument idoc = jCoFactory.createIDoc(X.idxValue(type, 0), X.idxValue(type, 1), X.idxValue(type, 2),
                X.idxValue(type, 3));
            schemas.addSchema(key, IDocXMLSchemaRenderer.renderXMLSchema(idoc, config.getEncoding()));
            // root iDocument Type - try sub-type, if empty use type
            String typeIdentifier = StringUtils.defaultString(X.idxValue(type, 1), X.idxValue(type, 0));
            return new XmlTypeLoader(schemas).load(JCoFactory.escapeSapName(typeIdentifier)).get();
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
            JCoFunction jCoFunction = jCoFactory.getFunction(IDOCTYPES_LIST_WITH_MESSAGES, true);
            jCoFactory.executeJCoFunction(jCoFunction, 0, TimeUnit.MILLISECONDS);
            Set<MetadataKey> keys = new HashSet<>();
            keys.add(MetadataKeyBuilder.newKey(NONE).build());
            // iDoc types
            JCoTable idocTypesTable = jCoFunction.getTableParameterList().getTable(PT_IDOCTYPES);
            String type = null;
            String desc = null;
            for (int i = 0; i < idocTypesTable.getNumRows(); i++) {
                idocTypesTable.setRow(i);
                JCoRecordMetaData meta = idocTypesTable.getRecordMetaData();
                type = idocTypesTable.getString(meta.indexOf(IDOCTYP));
                desc = idocTypesTable.getString(meta.indexOf(DESCRP));
                desc = type + (!X.isEmpty(desc) ? " - " + desc : "");
                keys.add(MetadataKeyBuilder.newKey(type).withDisplayName(desc).build());
            }
            // iDoc extended types
            idocTypesTable = jCoFunction.getTableParameterList().getTable(PT_EXTTYPES);
            for (int i = 0; i < idocTypesTable.getNumRows(); i++) {
                idocTypesTable.setRow(i);
                JCoRecordMetaData meta = idocTypesTable.getRecordMetaData();
                type = idocTypesTable.getString(meta.indexOf(IDOCTYP));
                try {
                    // composed IDoc type
                    type += "-" + idocTypesTable.getString(meta.indexOf(CIMTYP));
                } catch (Exception e) {
                    // silent
                }
                desc = idocTypesTable.getString(meta.indexOf(DESCRP));
                desc = type + (!X.isEmpty(desc) ? " - " + desc : "");
                keys.add(MetadataKeyBuilder.newKey(type).withDisplayName(desc).build());
            }
            return keys;
        } catch (Throwable e) {
            throw new MetadataResolvingException(e.getMessage(), FailureCode.UNKNOWN);
        }
    }

}
