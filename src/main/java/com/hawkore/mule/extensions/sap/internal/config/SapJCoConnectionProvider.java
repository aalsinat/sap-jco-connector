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
package com.hawkore.mule.extensions.sap.internal.config;

import java.util.Map;
import java.util.Properties;
import java.util.UUID;

import com.hawkore.mule.extensions.sap.api.config.auth.Authentication;
import com.hawkore.mule.extensions.sap.api.config.host.RemoteHostConfig;
import com.hawkore.mule.extensions.sap.api.config.trace.JCoTraceConfig;
import com.hawkore.mule.extensions.sap.internal.exceptions.InvalidConfigurationException;
import com.hawkore.mule.extensions.sap.internal.factory.dataprovider.SapJCoDataProvider;
import com.hawkore.mule.extensions.sap.internal.utils.U;
import com.hawkore.mule.extensions.sap.internal.utils.X;
import com.sap.conn.jco.ext.DestinationDataProvider;
import javax.inject.Inject;
import org.mule.runtime.api.artifact.Registry;
import org.mule.runtime.api.connection.CachedConnectionProvider;
import org.mule.runtime.api.connection.ConnectionException;
import org.mule.runtime.api.connection.ConnectionValidationResult;
import org.mule.runtime.api.meta.ExpressionSupport;
import org.mule.runtime.api.meta.ExternalLibraryType;
import org.mule.runtime.extension.api.annotation.Expression;
import org.mule.runtime.extension.api.annotation.ExternalLib;
import org.mule.runtime.extension.api.annotation.ExternalLibs;
import org.mule.runtime.extension.api.annotation.dsl.xml.ParameterDsl;
import org.mule.runtime.extension.api.annotation.param.NullSafe;
import org.mule.runtime.extension.api.annotation.param.Optional;
import org.mule.runtime.extension.api.annotation.param.Parameter;
import org.mule.runtime.extension.api.annotation.param.ParameterGroup;
import org.mule.runtime.extension.api.annotation.param.RefName;
import org.mule.runtime.extension.api.annotation.param.display.DisplayName;
import org.mule.runtime.extension.api.annotation.param.display.Example;
import org.mule.runtime.extension.api.annotation.param.display.Placement;
import org.mule.runtime.extension.api.annotation.param.display.Summary;
import org.mule.runtime.extension.api.runtime.config.ConfigurationProvider;

/**
 * SAP JCo Connection provider
 * <p>
 * For addtional JCo properties see sapjco3 javadoc com.sap.conn.jco.ext.DestinationDataProvider
 *
 * @author Alex Alsina de la Torre (alex.alsina@mango.com)
 */
@ExternalLibs({
    // IDoc Library, since version 3.0.11 jar name file can not be changed, allowed values:
    // com.sap.conn.idoc.sapidoc3.*.jar or sapjco3.jar
    @ExternalLib(name = "IDoc Java Library", description = "IDocument java library for SAP",
        type = ExternalLibraryType.JAR, nameRegexpMatcher = "(com\\.sap\\.conn\\.idoc\\.sapidoc3.*|sapidoc3)\\.jar",
        requiredClassName = "com.sap.conn.idoc.IDocDocument"),
    // IDoc Library, since version 3.0.11 jar name file can not be changed, allowed values:
    // com.sap.conn.jco.sapjco3.*.jar or sapjco3.jar
    @ExternalLib(name = "JCo Java Library", description = "Java Connector library for SAP",
        type = ExternalLibraryType.JAR, nameRegexpMatcher = "(com\\.sap\\.conn\\.jco\\.sapjco3.*|sapjco3)\\.jar",
        requiredClassName = "com.sap.conn.jco.JCo"),
    // JCo Native
    @ExternalLib(name = "JCo Native Library", type = ExternalLibraryType.NATIVE,
        description = "JCo Native Library (depends on the OS running mule application)",
        nameRegexpMatcher = "(com\\.sap\\.conn\\.jco\\.)?(lib)?sapjco3.*\\.(so|dll|jnilib)")})
public class SapJCoConnectionProvider
    implements SapJCoPropertiesConfigurator, CachedConnectionProvider<SapJCoConnection> {

    @Parameter
    @DisplayName("Authentication")
    @Placement(order = 1)
    @Expression(ExpressionSupport.NOT_SUPPORTED)
    @ParameterDsl(allowInlineDefinition = true, allowReferences = false)
    private Authentication authentication;
    @Parameter
    @DisplayName("SAP Server to connect to")
    @Placement(order = 2)
    @Expression(ExpressionSupport.NOT_SUPPORTED)
    @ParameterDsl(allowInlineDefinition = true, allowReferences = false)
    private RemoteHostConfig remoteHostConfig;
    @Parameter
    @DisplayName("SAP system number")
    @Placement(order = 3)
    @Summary("The two-digit system number (sysnr)")
    @Example("00")
    private String sysnr;
    @Parameter
    @DisplayName("SAP client ID")
    @Placement(order = 4)
    @Summary("Three-digit client number, preserve leading zeros if they appear in the number")
    @Example("001")
    private String client;
    @Parameter
    @Optional
    @Placement(tab = Placement.ADVANCED_TAB, order = 1)
    @DisplayName("Destination name")
    @Summary("Destination name is used as unique identifier to store JCo connection properties locally. A random "
                 + "destination name will be generated whether leave it blank")
    private String destination;
    @Parameter
    @Optional(defaultValue = "EN")
    @Placement(tab = Placement.ADVANCED_TAB, order = 2)
    @Example("EN")
    @DisplayName("Login language")
    @Summary(
        "ISO two-character language code (for example, EN, DE, FR) or SAP-specific single-character language code.")
    private String lang;
    @NullSafe
    @ParameterGroup(name = "Local JCo trace configuration")
    @Placement(tab = Placement.ADVANCED_TAB, order = 3)
    private JCoTraceConfig traceConfig;
    @Parameter
    @Optional
    @NullSafe
    @Placement(tab = Placement.ADVANCED_TAB, order = 6)
    @DisplayName("Additional Client JCo properties")
    @Summary("See sapjco3 javadoc for supported properties (com.sap.conn.jco.ext.DestinationDataProvider)")
    private Map<String, Object> additionalJCOProperties;
    @Inject
    private Registry registry;
    /**
     * The Config name.
     */
    @RefName
    private String configName;

    /**
     * Instantiates a new Sap JCo connection provider.
     */
    public SapJCoConnectionProvider() {
    }

    /**
     * Gets authentication.
     *
     * @return the authentication
     */
    public Authentication getAuthentication() {
        return authentication;
    }

    /**
     * Gets remote host config.
     *
     * @return the remote host config
     */
    public RemoteHostConfig getRemoteHostConfig() {
        return remoteHostConfig;
    }

    /**
     * Gets sysnr.
     *
     * @return the sysnr
     */
    public String getSysnr() {
        return sysnr;
    }

    /**
     * Gets client.
     *
     * @return the client
     */
    public String getClient() {
        return client;
    }

    /**
     * Gets destination.
     *
     * @return the destination
     */
    public String getDestination() {
        return destination;
    }

    /**
     * Gets lang.
     *
     * @return the lang
     */
    public String getLang() {
        return lang;
    }

    /**
     * Gets trace config.
     *
     * @return the trace config
     */
    public JCoTraceConfig getTraceConfig() {
        return traceConfig;
    }

    /**
     * Gets additional jco properties.
     *
     * @return the additional jco properties
     */
    public Map<String, Object> getAdditionalJCOProperties() {
        return additionalJCOProperties;
    }

    /**
     * Connect
     *
     * @return the sap JCo connection
     * @throws ConnectionException
     *     the connection exception
     */
    @Override
    public SapJCoConnection connect() throws ConnectionException {
        ConfigurationProvider configProvider = (ConfigurationProvider)registry.lookupByName(configName).orElseThrow(
            () -> new InvalidConfigurationException("Unable to find config by name " + configName));

        SapJCoConfiguration config = (SapJCoConfiguration)configProvider.get(null).getValue();
        Properties properties = new Properties();
        configure(properties);
        String customDestination = X.isEmpty(this.destination) ? UUID.randomUUID().toString() : this.destination;
        SapJCoDataProvider.getInstance().registerDestination(config.hashCode(), customDestination, properties);
        return new SapJCoConnection(config, customDestination);
    }

    /**
     * Disconnect.
     *
     * @param connection
     *     the connection
     */
    @Override
    public void disconnect(SapJCoConnection connection) {
        connection.disconnect();
    }

    /**
     * Validate connection validation result.
     *
     * @param connection
     *     the connection
     * @return the connection validation result
     */
    @Override
    public ConnectionValidationResult validate(SapJCoConnection connection) {
        return connection.validate();
    }

    /**
     * Configure.
     *
     * @param properties
     *     the properties
     */
    @Override
    public void configure(Properties properties) {
        // add additional JCO properties (will be overwritten by connector basic config's ones)
        U.addProperties(properties, this.additionalJCOProperties);
        // configure sap client authentication
        if (this.authentication == null) {
            throw new InvalidConfigurationException("You must provide an authentication method");
        }
        this.authentication.configure(properties);
        // checks remote SAP server configuration to connect to
        if (this.remoteHostConfig == null) {
            throw new InvalidConfigurationException(
                "You must provide a remote SAP server configuration to connect to: SAP Application Server OR "
                    + "SAP Message server");
        }
        // configure remote SAP server
        this.remoteHostConfig.configure(properties);
        // configure sap client
        U.addProperty(properties, DestinationDataProvider.JCO_CLIENT, this.client);
        U.addProperty(properties, DestinationDataProvider.JCO_SYSNR, this.sysnr);
        U.addProperty(properties, DestinationDataProvider.JCO_LANG, this.lang);
        // Configure tracing
        if (this.traceConfig != null) {
            this.traceConfig.configure(properties);
        }
    }

}
