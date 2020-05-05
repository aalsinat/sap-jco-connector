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

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;

import com.hawkore.mule.extensions.sap.internal.exceptions.XmlParserException;
import com.hawkore.mule.extensions.sap.internal.factory.JCoFactory;
import com.hawkore.mule.extensions.sap.internal.factory.constants.JCoFunctionConstants;
import com.hawkore.mule.extensions.sap.internal.factory.constants.XmlSchemaConstants;
import com.hawkore.mule.extensions.sap.internal.utils.U;
import com.hawkore.mule.extensions.sap.internal.utils.X;
import com.sap.conn.jco.AbapException;
import com.sap.conn.jco.JCoField;
import com.sap.conn.jco.JCoFieldIterator;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoRecord;
import com.sap.conn.jco.JCoRecordField;
import com.sap.conn.jco.JCoRecordFieldIterator;
import com.sap.conn.jco.JCoStructure;
import com.sap.conn.jco.JCoTable;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JCoFunction XML renderer with low memory footprint
 * <p>
 * NOTE: All JCoFunction values will be written as String, JCo library will auto encode into
 * theirs String representation
 *
 * @author Alex Alsina de la Torre (alex.alsina@mango.com)
 */
public class JCoFunctionXMLRenderer {

    private static final Logger logger = LoggerFactory.getLogger(JCoFunctionXMLParser.class);
    private XMLEventFactory eventFactory = null;
    private int indentLevel = 0;

    /**
     * Render JCoFunction as XML input stream.
     *
     * @param function
     *     the function
     * @param encoding
     *     the encoding
     * @return the input stream
     */
    public InputStream renderXML(JCoFunction function, String encoding) {
        XMLOutputFactory factory = XMLOutputFactory.newInstance();
        eventFactory = XMLEventFactory.newInstance();
        StringWriter stringWriter = new StringWriter();
        InputStream result = null;
        XMLEventWriter writer = null;
        String functionName = JCoFactory.escapeSapName(function.getName());
        try {
            // getting instance of writer
            writer = factory.createXMLEventWriter(stringWriter);
            // writing start document
            writeStartDocument(writer, encoding, XmlSchemaConstants.VERSION_1_0);
            // writing function start element
            writeStartElement(writer, functionName);
            // writing Import
            writeJCoRecord(writer, JCoFunctionConstants.IMPORT_TAG, function.getImportParameterList());
            // writing Export
            writeJCoRecord(writer, JCoFunctionConstants.EXPORT_TAG, function.getExportParameterList());
            // writing changing
            writeJCoRecord(writer, JCoFunctionConstants.CHANGING_TAG, function.getChangingParameterList());
            // writing Tables
            writeJCoRecord(writer, JCoFunctionConstants.TABLES_TAG, function.getTableParameterList());
            // writing Errors
            writeErrorsElement(writer, function.getExceptionList());
            // writing function end element
            writeEndElement(writer, functionName, true);
            // close document
            writeEndDocument(writer);
            writer.flush();
            result = new ByteArrayInputStream(stringWriter.toString().getBytes(encoding));
        } catch (Throwable e) {
            throw new XmlParserException("Unable to render JCoFunction: "+e.getMessage(), e);
        } finally {
            U.closeQuiet(writer);
            U.closeQuiet(stringWriter);
        }
        return result;
    }

    private void writeJCoRecord(XMLEventWriter writer, String elementName, JCoRecord jCoRecord)
        throws XMLStreamException {
        if (jCoRecord == null) {
            return;
        }
        String escapedTagName = JCoFactory.escapeSapName(elementName);
        writeStartElement(writer, escapedTagName);

        JCoFieldIterator fieldIterator = jCoRecord.getFieldIterator();
        while (fieldIterator.hasNextField()) {
            JCoField jCoField = fieldIterator.nextField();
            if (jCoField.isTable()) {
                // TABLE
                JCoTable jcoTable = jCoField.getTable();
                String name = JCoFactory.escapeSapName(jCoField.getName());
                writeStartElement(writer, name);
                // element is TABLE
                writeAttribute(writer, JCoFunctionConstants.TABLE_ATTR, XmlSchemaConstants.VALUE_ONE);
                int numRows = jcoTable.getNumRows();
                for (int i = 0; i < numRows; i++) {
                    jcoTable.setRow(i);
                    // wraps FIELDs into "row" element
                    writeStartElement(writer, JCoFunctionConstants.TABLE_ROW_TAG);
                    JCoRecordFieldIterator iterator = jcoTable.getRecordFieldIterator();
                    // process FIELDs inside TABLE
                    while (iterator.hasNextField()) {
                        JCoRecordField recordField = iterator.nextRecordField();
                        writeField(writer, jcoTable, recordField);
                    }
                    writeEndElement(writer, JCoFunctionConstants.TABLE_ROW_TAG, true);
                }
                writeEndElement(writer, name, true);
            } else if (jCoField.isStructure()) {
                // STRUCTURE
                String name = JCoFactory.escapeSapName(jCoField.getName());
                JCoStructure structure = jCoField.getStructure();
                writeStartElement(writer, name);
                // element is structure
                writeAttribute(writer, JCoFunctionConstants.STRUCTURE_ATTR, XmlSchemaConstants.VALUE_ONE);
                JCoRecordFieldIterator recordFieldIterator = structure.getRecordFieldIterator();
                // process FIELDs inside STRUCTURE
                while (recordFieldIterator.hasNextField()) {
                    JCoRecordField recordField = recordFieldIterator.nextRecordField();
                    writeField(writer, structure, recordField);
                }
                writeEndElement(writer, name, true);
            } else {
                // FIELD
                writeField(writer, null, jCoField);
            }
        }
        writeEndElement(writer, escapedTagName, true);
    }

    private void writeErrorsElement(XMLEventWriter writer, AbapException[] exceptions) throws XMLStreamException {
        if (!X.isEmpty(exceptions)) {
            writeStartElement(writer, JCoFunctionConstants.ERRORS_TAG);
            for (int i = 0; i < exceptions.length; i++) {
                AbapException abapException = exceptions[i];
                String errorType = JCoFactory.escapeSapName(abapException.getKey());
                writeStartElement(writer, errorType);
                writeCDATACharacters(writer, abapException.getMessage());
                writeEndElement(writer, errorType, false);
            }
            writeEndElement(writer, JCoFunctionConstants.ERRORS_TAG, true);
        }
    }

    private void writeField(XMLEventWriter writer, JCoRecord parent, JCoField jCoField) throws XMLStreamException {
        String name = jCoField.getName();
        if (jCoField.isTable()) {
            // JCoTable
            try {
                writeJCoRecord(writer, name, jCoField.getTable());
                return;
            } catch (Exception e) {
                // continue to write as simple item
            }
        } else if (jCoField.isStructure()) {
            // JCoStructure
            try {
                writeJCoRecord(writer, name, jCoField.getStructure());
                return;
            } catch (Exception e) {
                // continue to write as simple item
            }
        }
        // JCoField
        if (X.isEmpty(name)) {
            if (parent != null) {
                // name could be empty for table records with simple types, in that case jcoField name
                // comes from parent metadata record name
                name = parent.getMetaData().getName();
            }
        }
        name = JCoFactory.escapeSapName(name);
        writeStartElement(writer, name);
        String value = "";
        try {
            value = jCoField.getString().trim();
        } catch (Exception e) {
            //silent
        }
        // checks special String characteres and wraps value into CDATA whether necessary
        if (!X.isEmpty(value) && String.class
                                     .isAssignableFrom(JCoFactory.getClassForJCoFieldType(jCoField.getType()))) {
            // try to scape and test final escaped length
            if (StringEscapeUtils.escapeXml10(value).length() != value.length()) {
                // value was XML escaped
                writeCDATACharacters(writer, value);
            } else {
                writeCharacters(writer, value);
            }
        } else {
            writeCharacters(writer, value);
        }
        writeEndElement(writer, name, false);
    }

    private void writeStartElement(XMLEventWriter writer, String elementName) throws XMLStreamException {
        writeCharacters(writer, "\n");
        if (indentLevel > 0) {
            writeCharacters(writer, StringUtils.repeat("\t", indentLevel));
        }
        XMLEvent event = eventFactory.createStartElement("", "", elementName);
        writer.add(event);
        indentLevel++;
    }

    private void writeEndElement(XMLEventWriter writer, String elementName, boolean complex) throws XMLStreamException {
        indentLevel--;
        if (complex) {
            writeCharacters(writer, "\n");
            if (indentLevel > 0) {
                writeCharacters(writer, StringUtils.repeat("\t", indentLevel));
            }
        }
        XMLEvent event = eventFactory.createEndElement("", "", elementName);
        writer.add(event);
    }

    private void writeAttribute(XMLEventWriter writer, String key, String value) throws XMLStreamException {
        XMLEvent event = eventFactory.createAttribute(key, value);
        writer.add(event);
    }

    private void writeCharacters(XMLEventWriter writer, String characters) throws XMLStreamException {
        XMLEvent event = eventFactory.createCharacters(characters);
        writer.add(event);
    }

    private void writeCDATACharacters(XMLEventWriter writer, String characters) throws XMLStreamException {
        XMLEvent event = eventFactory.createCData(characters);
        writer.add(event);
    }

    private void writeStartDocument(XMLEventWriter writer, String encoding, String version) throws XMLStreamException {
        XMLEvent event = eventFactory.createStartDocument(encoding, version);
        writer.add(event);
    }

    private void writeEndDocument(XMLEventWriter writer) throws XMLStreamException {
        XMLEvent event = eventFactory.createEndDocument();
        writer.add(event);
    }

}
