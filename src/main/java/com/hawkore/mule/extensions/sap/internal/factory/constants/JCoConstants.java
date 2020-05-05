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
package com.hawkore.mule.extensions.sap.internal.factory.constants;

import com.sap.conn.idoc.IDocDatatype;
import com.sap.conn.idoc.IDocRecordMetaData;
import com.sap.conn.jco.JCoMetaData;

/**
 * JCo constants.
 *
 * @author Alex Alsina de la Torre (alex.alsina@mango.com)
 */
public final class JCoConstants {

    private JCoConstants() {}

    /**
     * The constant EMPTY_STRING.
     */
    public static final String EMPTY_STRING = "";
    /**
     * The constant TYPE_CHAR_DESC.
     */
    public static final String TYPE_CHAR_DESC = "1-byte or multi-byte character";
    /**
     * The constant TYPE_NUM_DESC.
     */
    public static final String TYPE_NUM_DESC = "Digits, fixed size, '0' padded";
    /**
     * The constant TYPE_STRING_DESC.
     */
    public static final String TYPE_STRING_DESC = "UTF-8 encoded string of variable length";
    /**
     * The constant TYPE_DATE_DESC.
     */
    public static final String TYPE_DATE_DESC = "Date (YYYYYMMDD)";
    /**
     * The constant TYPE_TIME_DESC.
     */
    public static final String TYPE_TIME_DESC = "Time (HHMMSS)";
    /**
     * The constant TYPE_BYTE_DESC.
     */
    public static final String TYPE_BYTE_DESC = "Raw data, binary, fixed length, zero padded. HEX encoded string";
    /**
     * The constant TYPE_FLOAT_DESC.
     */
    public static final String TYPE_FLOAT_DESC = "Floating point, double precision";
    /**
     * The constant TYPE_INT_DESC.
     */
    public static final String TYPE_INT_DESC = "4-byte integer";
    /**
     * The constant TYPE_INT1_DESC.
     */
    public static final String TYPE_INT1_DESC = "1-byte integer";
    /**
     * The constant TYPE_INT2_DESC.
     */
    public static final String TYPE_INT2_DESC = "2-byte integer";
    /**
     * The constant TYPE_INT8_DESC.
     */
    public static final String TYPE_INT8_DESC = "8-byte integer";
    /**
     * The constant TYPE_BCD_DESC.
     */
    public static final String TYPE_BCD_DESC = "Packed BCD number, any length between 1 and 16 bytes";
    /**
     * The constant TYPE_DEC_DESC.
     */
    public static final String TYPE_DEC_DESC = "Decimal floating point.";
    /**
     * The constant TYPE_XSTRING_DESC.
     */
    public static final String TYPE_XSTRING_DESC = "Byte array of variable length. HEX encoded string";
    /**
     * The constant DATATYPE_DATE_DESC.
     */
    public static final String DATATYPE_DATE_DESC = "Datatype DATE. May contain a date of format YYYYMMDD";
    /**
     * The constant DATATYPE_TIME_DESC.
     */
    public static final String DATATYPE_TIME_DESC = "Datatype TIME. May contain a time of format HHMMSS";
    /**
     * The constant DATATYPE_BINARY_DESC.
     */
    public static final String DATATYPE_BINARY_DESC = "Datatype BINARY. May contain any bytes. HEX encoded string";
    /**
     * The constant DATATYPE_STRING_DESC.
     */
    public static final String DATATYPE_STRING_DESC = "Datatype STRING. May contain any characters or digits";
    /**
     * The constant DATATYPE_DECIMAL_DESC.
     */
    public static final String DATATYPE_DECIMAL_DESC
        = "Datatype DECIMAL. May contain digits, a sign and a decimal point";
    /**
     * The constant DATATYPE_INTEGER_DESC.
     */
    public static final String DATATYPE_INTEGER_DESC = "Datatype INTEGER. May contain digits and a sign";
    /**
     * The constant DATATYPE_NUMERIC_DESC.
     */
    public static final String DATATYPE_NUMERIC_DESC = "Datatype NUMERIC. May contain digits only";

    /**
     * Description for JCo meta data type string.
     *
     * @param type
     *     the type
     * @return the string
     */
    public static String descriptionForJCoMetaDataType(int type) {
        switch (type) {
            case JCoMetaData.TYPE_CHAR:
                return TYPE_CHAR_DESC;
            case JCoMetaData.TYPE_NUM:
                return TYPE_NUM_DESC;
            case JCoMetaData.TYPE_STRING:
                return TYPE_STRING_DESC;
            case JCoMetaData.TYPE_DATE:
                return TYPE_DATE_DESC;
            case JCoMetaData.TYPE_TIME:
                return TYPE_TIME_DESC;
            case JCoMetaData.TYPE_BCD:
                return TYPE_BCD_DESC;
            case JCoMetaData.TYPE_DECF16:
            case JCoMetaData.TYPE_DECF34:
                return TYPE_DEC_DESC;
            case JCoMetaData.TYPE_BYTE:
                return TYPE_BYTE_DESC;
            case JCoMetaData.TYPE_XSTRING:
                return TYPE_XSTRING_DESC;
            case JCoMetaData.TYPE_FLOAT:
                return TYPE_FLOAT_DESC;
            case JCoMetaData.TYPE_INT:
                return TYPE_INT_DESC;
            case JCoMetaData.TYPE_INT1:
                return TYPE_INT1_DESC;
            case JCoMetaData.TYPE_INT2:
                return TYPE_INT2_DESC;
            case JCoMetaData.TYPE_INT8:
                return TYPE_INT8_DESC;
            default:
                return EMPTY_STRING;
        }
    }

    /**
     * Description for IDoc record meta data type string.
     *
     * @param type
     *     the type
     * @return the string
     */
    /* extracted from javadoc documentation */
    public static String descriptionForIDocRecordMetaDataType(int type) {
        switch (type) {
            case IDocRecordMetaData.TYPE_CHAR:
                return TYPE_CHAR_DESC;
            case IDocRecordMetaData.TYPE_NUM:
                return TYPE_NUM_DESC;
            case IDocRecordMetaData.TYPE_STRING:
                return TYPE_STRING_DESC;
            case IDocRecordMetaData.TYPE_DATE:
                return TYPE_DATE_DESC;
            case IDocRecordMetaData.TYPE_TIME:
                return TYPE_TIME_DESC;
            case IDocRecordMetaData.TYPE_BCD:
                return TYPE_BCD_DESC;
            case IDocRecordMetaData.TYPE_DECF16:
            case IDocRecordMetaData.TYPE_DECF34:
                return TYPE_DEC_DESC;
            case IDocRecordMetaData.TYPE_BYTE:
                return TYPE_BYTE_DESC;
            case IDocRecordMetaData.TYPE_XSTRING:
                return TYPE_XSTRING_DESC;
            case IDocRecordMetaData.TYPE_FLOAT:
                return TYPE_FLOAT_DESC;
            case IDocRecordMetaData.TYPE_INT:
                return TYPE_INT_DESC;
            case IDocRecordMetaData.TYPE_INT1:
                return TYPE_INT1_DESC;
            case IDocRecordMetaData.TYPE_INT2:
                return TYPE_INT2_DESC;
            case IDocRecordMetaData.TYPE_INT8:
                return TYPE_INT8_DESC;
            default:
                return EMPTY_STRING;
        }
    }

    /**
     * Description for IDoc datatype string.
     *
     * @param iDocDatatype
     *     the doc datatype
     * @return the string
     */
    public static String descriptionForIDocDatatype(IDocDatatype iDocDatatype) {
        switch (iDocDatatype) {
            case DATE:
                return DATATYPE_DATE_DESC;
            case TIME:
                return DATATYPE_TIME_DESC;
            case BINARY:
                return DATATYPE_BINARY_DESC;
            case STRING:
                return DATATYPE_STRING_DESC;
            case DECIMAL:
                return DATATYPE_DECIMAL_DESC;
            case INTEGER:
                return DATATYPE_INTEGER_DESC;
            case NUMERIC:
                return DATATYPE_NUMERIC_DESC;
            default:
                return EMPTY_STRING;
        }
    }

}
