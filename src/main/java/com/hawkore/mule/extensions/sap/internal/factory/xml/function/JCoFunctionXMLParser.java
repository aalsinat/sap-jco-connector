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
package com.hawkore.mule.extensions.sap.internal.factory.xml.function;

import java.io.InputStream;
import java.util.ArrayList;

import com.hawkore.mule.extensions.sap.internal.exceptions.AbstractConnectorException;
import com.hawkore.mule.extensions.sap.internal.exceptions.XmlParserException;
import com.hawkore.mule.extensions.sap.internal.factory.JCoFactory;
import com.hawkore.mule.extensions.sap.internal.factory.constants.JCoFunctionConstants;
import com.hawkore.mule.extensions.sap.internal.utils.U;
import com.hawkore.mule.extensions.sap.internal.utils.X;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoRecord;
import com.sap.conn.jco.JCoTable;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JCoFunction XML parser with low memory footprint
 * <p>
 * NOTE: All xml element's values will be read as String, JCo library will auto decode into
 * theirs respective java type
 *
 * @author Alex Alsina de la Torre (alex.alsina@mango.com)
 */
public class JCoFunctionXMLParser {

    private static final Logger logger = LoggerFactory.getLogger(JCoFunctionXMLParser.class);
    private final ArrayList<JCoRecord> records = new ArrayList<>();
    private JCoRecord currentRecord = null;
    private final JCoFactory jCoFactory;
    private final boolean disableCache;

    /**
     * Instantiates a new JCoFunctionXMLParser.
     *
     * @param jCoFactory
     *     the JCo factory
     * @param disableCache
     *     the disable cache
     */
    public JCoFunctionXMLParser(JCoFactory jCoFactory, boolean disableCache) {
        this.jCoFactory = jCoFactory;
        this.disableCache = disableCache;
    }

    /**
     * Parse XML input stream as JCoFunction.
     *
     * @param stream
     *     the stream
     * @param encoding
     *     the encoding
     * @return the parsed JCoFunction
     */
    public JCoFunction parseXML(InputStream stream, String encoding) {
        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLStreamReader reader = null;
        String functionName = null;
        String tableName = null;
        String structureName = null;
        String fieldName = null;
        String value = null;
        String localName = null;
        JCoFunction function = null;
        try {
            reader = factory.createXMLStreamReader(stream, encoding);
            while (reader.hasNext()) {
                int eventType = reader.next();
                if (eventType == XMLStreamReader.START_DOCUMENT) {
                    // ignore
                } else if (eventType == XMLStreamReader.START_ELEMENT) {
                    localName = reader.getLocalName();
                    if (function == null) {
                        // root first element tag is always function name
                        functionName = JCoFactory.unEscapeSapName(localName);
                        function = jCoFactory.getFunction(functionName, this.disableCache);
                        if (logger.isDebugEnabled()) {
                            logger.debug("Function = {}", functionName);
                        }
                    } else {
                        if (localName.equals(JCoFunctionConstants.IMPORT_TAG)) {
                            push(function.getImportParameterList());
                        } else if (localName.equals(JCoFunctionConstants.EXPORT_TAG)) {
                            push(function.getExportParameterList());
                        } else if (localName.equals(JCoFunctionConstants.CHANGING_TAG)) {
                            push(function.getChangingParameterList());
                        } else if (localName.equals(JCoFunctionConstants.TABLES_TAG)) {
                            tableName = null;
                            push(function.getTableParameterList());
                        } else if (localName.equals(JCoFunctionConstants.ERRORS_TAG)) {
                            // do not input process errors - internal function (execution) status
                            this.currentRecord = null;
                        } else if (!X.isEmpty(getAttributeValue(JCoFunctionConstants.TABLE_ATTR, reader))) {
                            if (tableName != null) {
                                pop();
                            }
                            tableName = JCoFactory.unEscapeSapName(localName);
                            if (logger.isDebugEnabled()) {
                                logger.debug("Table = {}", tableName);
                            }
                            push(this.currentRecord.getTable(tableName));
                        } else if (!X.isEmpty(getAttributeValue(JCoFunctionConstants.STRUCTURE_ATTR, reader))) {
                            structureName = JCoFactory.unEscapeSapName(localName);
                            if (logger.isDebugEnabled()) {
                                logger.debug("Structure = {}", structureName);
                            }
                            push(this.currentRecord.getStructure(structureName));
                        } else if (localName.equals(JCoFunctionConstants.TABLE_ROW_TAG)) {
                            if (logger.isDebugEnabled()) {
                                logger.debug("Row");
                            }
                            if (this.currentRecord instanceof JCoTable) {
                                ((JCoTable)this.currentRecord).appendRow();
                            }
                        } else if (this.currentRecord != null) {
                            fieldName = JCoFactory.unEscapeSapName(localName);
                            value = reader.getElementText();
                            if (logger.isDebugEnabled()) {
                                logger.debug("Field = {}, value = {}", fieldName, value);
                            }
                            if (this.currentRecord.getFieldCount() == 1) {
                                // set value by index
                                // table with simple type has not field name
                                this.currentRecord.setValue(0, value);
                            } else {
                                // set value by field name
                                this.currentRecord.setValue(fieldName, value);
                            }
                        }
                    }
                } else if (eventType == XMLStreamReader.END_ELEMENT) {
                    //  END_ELEMENT
                    if (localName.equals(JCoFunctionConstants.IMPORT_TAG) || localName.equals(
                        JCoFunctionConstants.EXPORT_TAG) || localName.equals(JCoFunctionConstants.TABLES_TAG)
                            || localName.equals(JCoFunctionConstants.CHANGING_TAG) || localName.equals(
                        JCoFunctionConstants.ERRORS_TAG)) {
                        pop();
                    }
                } else if (eventType == XMLStreamReader.END_DOCUMENT) {
                    // END_DOCUMENT
                }
            }
        } catch (AbstractConnectorException e) {
            throw e;
        } catch (Throwable e) {
            throw new XmlParserException("Unable to parse JCoFunction: "+e.getMessage(), e);
        } finally {
            U.closeQuiet(reader);
            U.closeQuiet(stream);
        }
        return function;
    }

    private String getAttributeValue(String name, XMLStreamReader reader) {
        int count = reader.getAttributeCount();
        for (int i = 0; i < count; i++) {
            if (reader.getAttributeLocalName(i).equals(name)) {
                String value = reader.getAttributeValue(i);
                return value;
            }
        }
        return null;
    }

    private void push(final JCoRecord jCoRecord) {
        if (jCoRecord != null) {
            if (this.currentRecord != null) {
                this.records.add(this.currentRecord);
            }
            this.currentRecord = jCoRecord;
        }
    }

    private void pop() {
        if (!this.records.isEmpty()) {
            this.currentRecord = (JCoRecord)records.remove(this.records.size() - 1);
        } else {
            this.currentRecord = null;
        }
    }

}
