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
package com.hawkore.mule.extensions.sap.internal.factory.trace;

import com.sap.conn.jco.JCoTraceListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple trace listener to log JCo messages
 *
 * @author Alex Alsina de la Torre (alex.alsina@mango.com)
 */
public class SapJCoTraceListener implements JCoTraceListener {

    private static final Logger logger = LoggerFactory.getLogger(SapJCoTraceListener.class);

    /**
     * Trace.
     * <p>
     * Levels from documentation:
     * <p>
     * Disabled:
     * <ol>
     * <li>NONE	0
     * </ol>
     * Error
     * <ol>
     * <li>ERRORS 1
     * <li>ERROR_WARNING 2
     * <li>INFO_PATH_API 3
     * </ol>
     * Info
     * <ol>
     * <li>PATH_API 4
     * <li>INFO_PATH 5
     * <li>SHORT_DEBUG 6
     * </ol>
     * Debug
     * <ol>
     * <li>FULL_PATH 7
     * <li>FULL_DEBUG 8
     * </ol>
     *
     * @param level
     *     the level
     * @param message
     *     the message
     */
    @Override
    public void trace(int level, String message) {
        if (level > NONE && level <= INFO_PATH_API) {
            logger.error(message);
        } else if (level > NONE && level <= SHORT_DEBUG) {
            logger.info(message);
        } else if (level > NONE && logger.isDebugEnabled()) {
            logger.debug(message);
        }
    }

}
