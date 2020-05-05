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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import com.hawkore.mule.extensions.sap.internal.SapJCoExtension;
import com.hawkore.mule.extensions.sap.internal.exceptions.XmlParserException;
import com.hawkore.mule.extensions.sap.internal.factory.constants.JCoConstants;
import com.hawkore.mule.extensions.sap.internal.factory.constants.JCoFunctionConstants;
import com.hawkore.mule.extensions.sap.internal.factory.constants.XmlSchemaConstants;
import com.hawkore.mule.extensions.sap.internal.factory.xml.common.AXMLSchemaRenderer;
import com.hawkore.mule.extensions.sap.internal.utils.X;
import com.sap.conn.jco.AbapException;
import com.sap.conn.jco.JCoField;
import com.sap.conn.jco.JCoFunction;
import com.sap.conn.jco.JCoParameterField;
import com.sap.conn.jco.JCoRecord;
import com.sap.conn.jco.JCoStructure;
import com.sap.conn.jco.JCoTable;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Generates XML schema from JCoFunction metadata.
 * <p>
 * NOTE: All xml element types are defined as xsd:string, JCo library will auto encode/decode into theirs respective
 * java types when read/write
 *
 * @author Alex Alsina de la Torre (alex.alsina@mango.com)
 */
public class JCoFunctionXMLSchemaRenderer extends AXMLSchemaRenderer {

    private static final Logger logger = LoggerFactory.getLogger(JCoFunctionXMLSchemaRenderer.class);
    private static final String FIELD_NAME_IS_BLANK_AUTO_GENERATING_FIELD_NAME
        = "Field name is blank, auto-generating field name {}";

    /**
     * Render JCoFunction xml schema as input stream.
     *
     * @param jCoFunction
     *     the JCoFunction to process
     * @param encoding
     *     the encoding
     * @return the input stream
     */
    public static InputStream renderXMLSchema(JCoFunction jCoFunction, String encoding) {
        try {
            Document schema = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element root = schema.createElementNS(XMLConstants.W3C_XML_SCHEMA_NS_URI, XmlSchemaConstants.XSD_SCHEMA);
            root.setAttribute(XmlSchemaConstants.VERSION_ATTR, XmlSchemaConstants.VERSION_1_0);
            // append schema description
            appendDescription(root, JCoFunctionConstants.SAP_FUNCTION_SCHEMA_DESC + jCoFunction.getName(),
                XmlSchemaConstants.APP_INFO, SapJCoExtension.VENDOR_URL);

            // the function
            Element functionElement = AXMLSchemaRenderer
                                          .createElement(schema, jCoFunction.getName(), null, false, false, false);
            List<Element> functionChilds = new ArrayList<>();
            // import child
            if (jCoFunction.getImportParameterList() != null) {
                functionChilds.add(
                    toElement(schema, JCoFunctionConstants.IMPORT_TAG, JCoFunctionConstants.IMPORT_PARAMETERS_DESC,
                        jCoFunction.getImportParameterList()));
            }
            // export child
            if (jCoFunction.getExportParameterList() != null) {
                functionChilds.add(
                    toElement(schema, JCoFunctionConstants.EXPORT_TAG, JCoFunctionConstants.EXPORT_PARAMETERS_DESC,
                        jCoFunction.getExportParameterList()));
            }
            // changing child
            if (jCoFunction.getChangingParameterList() != null) {
                functionChilds.add(
                    toElement(schema, JCoFunctionConstants.CHANGING_TAG, JCoFunctionConstants.CHANGING_PARAMETERS_DESC,
                        jCoFunction.getChangingParameterList()));
            }
            // tables child
            if (jCoFunction.getTableParameterList() != null) {
                functionChilds.add(
                    toElement(schema, JCoFunctionConstants.TABLES_TAG, JCoFunctionConstants.TABLE_PARAMETERS_DESC,
                        jCoFunction.getTableParameterList()));
            }
            // errors child that can occurs
            if (!X.isEmpty(jCoFunction.getExceptionList())) {
                functionChilds.add(toElement(schema, JCoFunctionConstants.ERRORS_TAG, JCoFunctionConstants.ERRORS_DESC,
                    jCoFunction.getExceptionList()));
            }
            // append childs to function definition
            appendChilds(functionElement, functionChilds, null, true);
            // append function to document
            root.appendChild(functionElement);
            schema.appendChild(root);
            // writes schema as input stream
            return writeDocument(schema, encoding);
        } catch (Throwable e) {
            throw new XmlParserException("Unable to render XML Schema for JCoFunction: " + e.getMessage(), e);
        }
    }

    private static Element toElement(Document document, String name, String description, AbapException[] exceptions) {
        Element errorElement = createElement(document, name, description, true, false, false);
        List<Element> errorTypes = new ArrayList<>();
        Stream.of(exceptions).forEach(abapException -> errorTypes.add(AXMLSchemaRenderer.createSimpleElement(document,
            abapException.getKey(), abapException.getLocalizedMessage(), 0, true, false, false)));
        // add error types to error
        appendChilds(errorElement, errorTypes, null, false);
        return errorElement;
    }

    private static Element toElement(Document document, String name, String description, JCoRecord record) {
        Element parent = createElement(document, name, description, true, false, false);
        List<Element> childs = new ArrayList<>();
        List<Element> attributes = null;
        // create child list
        record.forEach(jCoField -> childs.add(toElement(document, record, jCoField)));

        if (record instanceof JCoTable) {
            // add the TABLE="1" required attribute
            attributes = Arrays.asList(
                createAttribute(document, JCoFunctionConstants.TABLE_ATTR, JCoFunctionConstants.TABLE_ATTR_DESC, true,
                    XmlSchemaConstants.VALUE_ONE));
            // wraps columns into REAPETABLE "row" element
            Element rowElement = AXMLSchemaRenderer.createElement(document, JCoFunctionConstants.TABLE_ROW_TAG,
                JCoFunctionConstants.TABLE_ROW_DESC, true, false, true);
            // add row wrapper
            appendChilds(rowElement, childs, null, false);
            // add row to table
            appendChilds(parent, Arrays.asList(rowElement), attributes, false);
            return parent;
        } else if (record instanceof JCoStructure) {
            // add the STRUCTURE="1" required attribute
            attributes = Arrays.asList(
                createAttribute(document, JCoFunctionConstants.STRUCTURE_ATTR, JCoFunctionConstants.STRUCTURE_ATTR_DESC,
                    true, XmlSchemaConstants.VALUE_ONE));
        }
        // add childs to parent
        appendChilds(parent, childs, attributes, false);
        return parent;
    }

    private static Element toElement(Document document, JCoRecord complexParent, JCoField jCoField) {
        if (jCoField.isTable()) {
            try {
                return toElement(document, jCoField.getName(), jCoField.getDescription(), jCoField.getTable());
            } catch (Exception e) {
                return createSimpleElement(document, null, jCoField);
            }
        }
        if (jCoField.isStructure()) {
            try {
                return toElement(document, jCoField.getName(), jCoField.getDescription(), jCoField.getStructure());
            } catch (Exception e) {
                return createSimpleElement(document, null, jCoField);
            }
        }
        return createSimpleElement(document, complexParent, jCoField);
    }

    /**
     * Create simple element element.
     *
     * @param document
     *     the document
     * @param complexParent
     *     the complex parent
     * @param jCoField
     *     the JCo field
     * @return the element
     */
    public static Element createSimpleElement(Document document, JCoRecord complexParent, JCoField jCoField) {
        String name = jCoField.getName();
        if (X.isEmpty(name)) {
            if (complexParent != null) {
                // name could be empty for table records with simple types, in that case jcoField name
                // comes from parent metadata record name
                name = complexParent.getMetaData().getName();
            }
        }
        // construct description with type info
        String desc = StringUtils.join(
            Arrays.asList(jCoField.getDescription(), JCoConstants.descriptionForJCoMetaDataType(jCoField.getType())),
            " \n");
        // checks if field is required
        boolean required = jCoField instanceof JCoParameterField && !((JCoParameterField)jCoField).isOptional();
        return createSimpleElement(document, name, desc, jCoField.getLength(), true, required, false);
    }

}
