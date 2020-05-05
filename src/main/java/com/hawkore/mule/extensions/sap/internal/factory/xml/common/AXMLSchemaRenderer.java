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
package com.hawkore.mule.extensions.sap.internal.factory.xml.common;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.List;
import java.util.stream.Stream;

import com.hawkore.mule.extensions.sap.internal.factory.JCoFactory;
import com.hawkore.mule.extensions.sap.internal.factory.constants.XmlSchemaConstants;
import com.hawkore.mule.extensions.sap.internal.utils.X;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Common xml schema generator functions
 *
 * @author Alex Alsina de la Torre (alex.alsina@mango.com)
 */
public abstract class AXMLSchemaRenderer {

    /**
     * Write document as input stream.
     *
     * @param document
     *     the document
     * @param encoding
     *     the encoding
     * @return the input stream
     * @throws TransformerException
     *     the transformer exception
     */
    public static InputStream writeDocument(Document document, String encoding) throws TransformerException {
        // writes schema as input stream
        StringWriter stringWriter = new StringWriter();
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(XmlSchemaConstants.ENCODING, encoding);
        transformer.setOutputProperty(XmlSchemaConstants.OMIT_XML_DECLARATION, XmlSchemaConstants.VALUE_NO);
        transformer.setOutputProperty(XmlSchemaConstants.INDENT, XmlSchemaConstants.VALUE_YES);
        transformer.setOutputProperty(XmlSchemaConstants.METHOD, XmlSchemaConstants.XML);
        transformer.transform(new DOMSource(document), new StreamResult(stringWriter));
        return new ByteArrayInputStream(stringWriter.toString().getBytes(Charset.forName(encoding)));
    }

    /**
     * Creates a detached element.
     *
     * @param document
     *     the document
     * @param name
     *     the name
     * @param description
     *     the description
     * @param bounded
     *     the bounded
     * @param required
     *     the required
     * @param repeatable
     *     the repeatable
     * @return the element
     */
    public static Element createElement(Document document,
        String name,
        String description,
        boolean bounded,
        boolean required,
        boolean repeatable) {
        Element node = document.createElement(XmlSchemaConstants.XSD_ELEMENT);
        node.setAttribute(XmlSchemaConstants.NAME_ATTR, JCoFactory.escapeSapName(name));
        if (bounded || required) {
            node.setAttribute(XmlSchemaConstants.MIN_OCCURS_ATTR,
                required ? XmlSchemaConstants.VALUE_ONE : XmlSchemaConstants.VALUE_ZERO);
            if (!repeatable) {
                node.setAttribute(XmlSchemaConstants.MAX_OCCURS_ATTR, XmlSchemaConstants.VALUE_ONE);
            }
        }
        // add description
        appendDescription(node, description);
        return node;
    }

    /**
     * Creates a detached simple element.
     *
     * @param document
     *     the document
     * @param name
     *     the name
     * @param description
     *     the description
     * @param length
     *     the length
     * @param bounded
     *     the bounded
     * @param required
     *     the required
     * @param repeatable
     *     the repeatable
     * @param attributes
     *     the attributes
     * @return the element
     */
    public static Element createSimpleElement(Document document,
        String name,
        String description,
        int length,
        boolean bounded,
        boolean required,
        boolean repeatable,
        Element... attributes) {
        Element node = document.createElement(XmlSchemaConstants.XSD_ELEMENT);
        node.setAttribute(XmlSchemaConstants.NAME_ATTR, JCoFactory.escapeSapName(name));
        if (bounded || required) {
            node.setAttribute(XmlSchemaConstants.MIN_OCCURS_ATTR,
                required ? XmlSchemaConstants.VALUE_ONE : XmlSchemaConstants.VALUE_ZERO);
            if (!repeatable) {
                node.setAttribute(XmlSchemaConstants.MAX_OCCURS_ATTR, XmlSchemaConstants.VALUE_ONE);
            }
        }
        // add description
        appendDescription(node, description);
        // add maxLength restriction
        if (length > 0) {
            Element simpleType = document.createElement(XmlSchemaConstants.XSD_SIMPLE_TYPE);
            Element restriction = document.createElement(XmlSchemaConstants.XSD_RESTRICTION);
            Element maxLength = document.createElement(XmlSchemaConstants.XSD_MAX_LENGTH);
            maxLength.setAttribute(XmlSchemaConstants.VALUE_ATTR, String.valueOf(length));
            restriction.appendChild(maxLength);
            restriction.setAttribute(XmlSchemaConstants.BASE_ATTR, XmlSchemaConstants.XSD_STRING);
            simpleType.appendChild(restriction);
            node.appendChild(simpleType);
        } else {
            node.setAttribute(XmlSchemaConstants.TYPE_ATTR, XmlSchemaConstants.XSD_STRING);
        }
        // add attributes
        if (!X.isEmpty(attributes)) {
            Stream.of(attributes).forEach(node::appendChild);
        }
        return node;
    }

    /**
     * Creates a detached attribute element.
     *
     * @param document
     *     the document
     * @param name
     *     the name
     * @param description
     *     the description
     * @param required
     *     the required
     * @param enumValues
     *     the enum values
     * @return the element
     */
    public static Element createAttribute(Document document,
        String name,
        String description,
        boolean required,
        String... enumValues) {
        Element attribute = document.createElement(XmlSchemaConstants.XSD_ATTRIBUTE);
        attribute.setAttribute(XmlSchemaConstants.NAME_ATTR, name);
        appendDescription(attribute, description);
        if (!X.isEmpty(enumValues)) {
            Element simpleType = document.createElement(XmlSchemaConstants.XSD_SIMPLE_TYPE);
            Element restriction = document.createElement(XmlSchemaConstants.XSD_RESTRICTION);
            restriction.setAttribute(XmlSchemaConstants.BASE_ATTR, XmlSchemaConstants.XSD_STRING);
            Stream.of(enumValues).forEach(value -> {
                Element enumeration = document.createElement(XmlSchemaConstants.XSD_ENUMERATION);
                enumeration.setAttribute(XmlSchemaConstants.VALUE_ATTR, value);
                restriction.appendChild(enumeration);
            });
            simpleType.appendChild(restriction);
            attribute.appendChild(simpleType);
        } else {
            attribute.setAttribute(XmlSchemaConstants.TYPE_ATTR, XmlSchemaConstants.XSD_STRING);
        }
        if (required) {
            attribute.setAttribute(XmlSchemaConstants.USE_ATTR, XmlSchemaConstants.VALUE_REQUIRED);
        }
        return attribute;
    }

    /**
     * Append childs.
     *
     * @param target
     *     the target element to append childs and attributes
     * @param childs
     *     the childs
     * @param attributes
     *     the attributes
     * @param sorted
     *     the sorted whether add sort restriction (xsd:sequence)
     */
    public static void appendChilds(Element target, List<Element> childs, List<Element> attributes, boolean sorted) {
        if (X.isEmpty(attributes) && X.isEmpty(childs)) {
            return;
        }
        Document document = target.getOwnerDocument();
        Element complexElement = document.createElement(XmlSchemaConstants.XSD_COMPLEX_TYPE);
        Element listWrapperElement = document.createElement(
            sorted ? XmlSchemaConstants.XSD_SEQUENCE : XmlSchemaConstants.XSD_ALL);
        // first: append child elements
        if (!X.isEmpty(childs)) {
            childs.forEach(listWrapperElement::appendChild);
        }
        complexElement.appendChild(listWrapperElement);
        // second: append attributes
        if (!X.isEmpty(attributes)) {
            attributes.forEach(complexElement::appendChild);
        }
        target.appendChild(complexElement);
    }

    /**
     * Append description.
     *
     * @param target
     *     the target
     * @param description
     *     the description
     */
    public static void appendDescription(Element target, String description) {
        appendDescription(target, description, null, null);
    }

    /**
     * Append description.
     *
     * @param target
     *     the target element to append description
     * @param description
     *     the description
     * @param appInfo
     *     the app info
     * @param appInfoSource
     *     the app info source
     */
    public static void appendDescription(Element target, String description, String appInfo, String appInfoSource) {
        if ((X.isEmpty(description) || X.isEmpty(description.trim())) && (X.isEmpty(appInfo) || X.isEmpty(
            appInfo.trim()))) {
            return;
        }
        Document document = target.getOwnerDocument();
        Element annotationElement = document.createElement(XmlSchemaConstants.XSD_ANNOTATION);
        if (!X.isEmpty(appInfo)) {
            Element appInfoElement = document.createElement(XmlSchemaConstants.XSD_APPINFO);
            if (!X.isEmpty(appInfoSource)) {
                appInfoElement.setAttribute(XmlSchemaConstants.SOURCE_ATTR, appInfoSource);
            }
            appInfoElement.appendChild(document.createCDATASection(appInfo));
            annotationElement.appendChild(appInfoElement);
        }
        if (!X.isEmpty(description)) {
            Element documentationElement = document.createElement(XmlSchemaConstants.XSD_DOCUMENTATION);
            documentationElement.appendChild(document.createCDATASection(description));
            annotationElement.appendChild(documentationElement);
        }
        target.appendChild(annotationElement);
    }

}
