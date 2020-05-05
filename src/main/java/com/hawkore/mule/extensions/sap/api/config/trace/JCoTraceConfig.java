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
package com.hawkore.mule.extensions.sap.api.config.trace;

import java.util.Properties;

import com.hawkore.mule.extensions.sap.internal.config.SapJCoPropertiesConfigurator;
import com.hawkore.mule.extensions.sap.internal.utils.U;
import com.sap.conn.jco.ext.DestinationDataProvider;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.annotation.param.display.Summary;

/**
 * JCo Tracing configuration
 *
 * @author Alex Alsina de la Torre (alex.alsina@mango.com)
 */
public class JCoTraceConfig implements SapJCoPropertiesConfigurator {

    /**
     * Enable/disable RFC trace (jco.client.trace)
     */
    @Parameter
    @DisplayName("Enable/disable RFC trace")
    @Optional(defaultValue = "false")
    @Placement(tab = "Advanced", order = 5)
    @Summary("Enable/disable RFC trace (jco.client.trace)")
    private boolean enabledRFCtrace;
    /**
     * Enable/disable CPIC trace (jco.client.cpic_trace)
     */
    @Parameter
    @DisplayName("Enable/disable CPIC trace")
    @Optional
    @Placement(tab = "Advanced", order = 6)
    @Summary("Enable/disable CPIC trace (jco.client.cpic_trace)")
    private JCoCPICTraceLevel cpicTraceLevel;

    /**
     * Instantiates a new Trace config.
     */
    public JCoTraceConfig() {
    }

    /**
     * Is enabled rf ctrace boolean.
     *
     * @return the boolean
     */
    public boolean isEnabledRFCtrace() {
        return this.enabledRFCtrace;
    }

    /**
     * Sets enabled rf ctrace.
     *
     * @param enabledRFCtrace
     *     the enabled rf ctrace
     */
    public void setEnabledRFCtrace(boolean enabledRFCtrace) {
        this.enabledRFCtrace = enabledRFCtrace;
    }

    /**
     * Gets cpic trace level.
     *
     * @return the cpic trace level
     */
    public JCoCPICTraceLevel getCpicTraceLevel() {
        return this.cpicTraceLevel;
    }

    /**
     * Sets cpic trace level.
     *
     * @param cpicTraceLevel
     *     the cpic trace level
     */
    public void setCpicTraceLevel(JCoCPICTraceLevel cpicTraceLevel) {
        this.cpicTraceLevel = cpicTraceLevel;
    }

    /**
     * Configure.
     *
     * @param target
     *     the target
     */
    @Override
    public void configure(Properties target) {
        U.addProperty(target, DestinationDataProvider.JCO_TRACE, this.isEnabledRFCtrace() ? 1 : 0);
        if (this.getCpicTraceLevel() != null) {
            U.addProperty(target, DestinationDataProvider.JCO_CPIC_TRACE, this.getCpicTraceLevel().getLevel());
        }
    }

}
