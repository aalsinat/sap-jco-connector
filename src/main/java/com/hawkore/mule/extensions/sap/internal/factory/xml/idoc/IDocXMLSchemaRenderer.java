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
package com.hawkore.mule.extensions.sap.internal.factory.xml.idoc;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.hawkore.mule.extensions.sap.internal.SapJCoExtension;
import com.hawkore.mule.extensions.sap.internal.exceptions.XmlParserException;
import com.hawkore.mule.extensions.sap.internal.factory.constants.IDocConstants;
import com.hawkore.mule.extensions.sap.internal.factory.constants.JCoConstants;
import com.hawkore.mule.extensions.sap.internal.factory.constants.XmlSchemaConstants;
import com.hawkore.mule.extensions.sap.internal.factory.xml.common.AXMLSchemaRenderer;
import com.hawkore.mule.extensions.sap.internal.utils.X;
import com.sap.conn.idoc.IDocDocument;
import com.sap.conn.idoc.IDocRecordMetaData;
import com.sap.conn.idoc.IDocSegmentMetaData;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Generates XML schema from IDocDocument metadata
 * <p>
 * NOTE: All xml element types are defined as xsd:string, JCo library will auto encode/decode into theirs respective
 * java types when parse/render
 *
 * @author Alex Alsina de la Torre (alex.alsina@mango.com)
 */
public class IDocXMLSchemaRenderer extends AXMLSchemaRenderer {

    private static final Logger logger = LoggerFactory.getLogger(IDocXMLSchemaRenderer.class);

    /**
     * Render IDocDocument XML schema as input stream.
     *
     * @param idoc
     *     the IDocDocument to process
     * @param encoding
     *     the encoding
     * @return the input stream
     */
    public static InputStream renderXMLSchema(IDocDocument idoc, String encoding) {
        try {
            Document schema = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
            Element root = schema.createElementNS(XMLConstants.W3C_XML_SCHEMA_NS_URI, XmlSchemaConstants.XSD_SCHEMA);
            root.setAttribute(XmlSchemaConstants.VERSION_ATTR, XmlSchemaConstants.VERSION_1_0);
            // append schema description
            appendDescription(root, IDocConstants.SAP_IDOC_SCHEMA_DESC + StringUtils.join(
                Arrays.asList(idoc.getIDocType(), idoc.getIDocTypeExtension()), "-"), XmlSchemaConstants.APP_INFO,
                SapJCoExtension.VENDOR_URL);

            // the IDocDocument type tag name
            String idocTypeTagName = X.isEmpty(idoc.getIDocTypeExtension())
                                         ? idoc.getIDocType()
                                         : idoc.getIDocTypeExtension();

            // the IDocDocument element
            Element idocTypeElement = createElement(schema, idocTypeTagName, null, false, false, false);

            // the IDOC element - REAPETABLE (IDoc package (IDocDocumentList))
            Element IDOC = createElement(schema, IDocConstants.IDOC_TAG, null, true, true, true);
            List<Element> IDOC_CHILDS = new ArrayList<>();

            // the EDI tag name
            String EDI_DC_TAG_NAME = X.isEmpty(idoc.getTableStructureName())
                                         ? IDocConstants.EDI_DC_40_TAG
                                         : idoc.getTableStructureName();

            // The control record EDIC_DC element
            Element EDI_DC = createElement(schema, EDI_DC_TAG_NAME, IDocConstants.EDI_DC_DESC, true, true, false);

            IDocRecordMetaData recordMetaData = idoc.getRecordMetaData();
            List<Element> recordMetadataFields = new ArrayList<>();
            for (int i = 0; i < recordMetaData.getNumFields(); i++) {
                String desc = StringUtils.join(Arrays.asList(recordMetaData.getDescription(i),
                    JCoConstants.descriptionForIDocDatatype(recordMetaData.getDatatype(i)),
                    JCoConstants.descriptionForIDocRecordMetaDataType(recordMetaData.getType(i))), " \n");
                recordMetadataFields.add(
                    createSimpleElement(schema, recordMetaData.getName(i), desc, recordMetaData.getLength(i), true,
                        false, false));
            }
            // EDI_DC's record metadata
            appendChilds(EDI_DC, recordMetadataFields, Arrays.asList(
                createAttribute(schema, IDocConstants.SEGMENT_ATTR, null, false, XmlSchemaConstants.VALUE_ONE)), false);

            // add EDI_DC to IDOC childs
            IDOC_CHILDS.add(EDI_DC);

            // process root segment's record metadata
            IDocSegmentMetaData rootSegmentMetaData = idoc.getRootSegment().getSegmentMetaData();
            IDocSegmentMetaData[] segments = rootSegmentMetaData.getChildren();
            Set<String> processed = new HashSet<>();
            for (int i = 0; i < segments.length; i++) {
                IDocSegmentMetaData child = segments[i];
                if (!processed.contains(child.getType())) {
                    // process child segments
                    IDOC_CHILDS.add(createSegment(schema, child));
                    processed.add(child.getType());
                }
            }
            // add child elements to IDOC element
            appendChilds(IDOC, IDOC_CHILDS, Arrays.asList(
                createAttribute(schema, IDocConstants.BEGIN_ATTR, null, true, XmlSchemaConstants.VALUE_ONE)), false);
            // add IDOC element to IDocDocument
            appendChilds(idocTypeElement, Arrays.asList(IDOC), null, false);
            // add IDocDocument to schema
            root.appendChild(idocTypeElement);
            schema.appendChild(root);
            // writes schema as input stream
            return writeDocument(schema, encoding);
        } catch (Throwable e) {
            throw new XmlParserException("Unable to render XML Schema for IDoc: " + e.getMessage(), e);
        }
    }

    private static Element createSegment(Document document, IDocSegmentMetaData segment) {
        // the current segment element
        Element segmentElement = createElement(document, segment.getType(), segment.getDescription(), false, false,
            false);
        List<Element> childs = new ArrayList<>();
        // process segment's record metadata
        IDocRecordMetaData recordMetaData = segment.getRecordMetaData();
        if (recordMetaData != null) {
            for (int i = 0; i < recordMetaData.getNumFields(); i++) {
                String desc = StringUtils.join(Arrays.asList(recordMetaData.getDescription(i),
                    JCoConstants.descriptionForIDocDatatype(recordMetaData.getDatatype(i)),
                    JCoConstants.descriptionForIDocRecordMetaDataType(recordMetaData.getType(i))), " \n");
                childs.add(
                    createSimpleElement(document, recordMetaData.getName(i), desc, recordMetaData.getLength(i), true,
                        false, false));
            }
        }
        // process child segments
        IDocSegmentMetaData[] segments = segment.getChildren();
        if (!X.isEmpty(segments)) {
            Set<String> processed = new HashSet<>();
            for (int i = 0; i < segments.length; i++) {
                IDocSegmentMetaData child = segments[i];
                if (!processed.contains(child.getType())) {
                    // process child segments
                    childs.add(createSegment(document, child));
                    processed.add(child.getType());
                }
            }
        }
        if (!X.isEmpty(childs)) {
            // add child segments
            appendChilds(segmentElement, childs, Arrays.asList(
                createAttribute(document, IDocConstants.SEGMENT_ATTR, null, false, XmlSchemaConstants.VALUE_ONE)),
                false);
        }
        return segmentElement;
    }

}
