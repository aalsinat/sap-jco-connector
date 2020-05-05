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
package com.hawkore.mule.extensions.sap.internal.factory.dataprovider;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.sap.conn.jco.ext.DestinationDataEventListener;
import com.sap.conn.jco.ext.DestinationDataProvider;
import com.sap.conn.jco.ext.Environment;
import com.sap.conn.jco.ext.ServerDataEventListener;
import com.sap.conn.jco.ext.ServerDataProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SAP JCo Data Provider
 * <p>
 * Manages connection properties for destinations and servers
 *
 * @author Alex Alsina de la Torre (alex.alsina@mango.com)
 */
public class SapJCoDataProvider implements DestinationDataProvider, ServerDataProvider {

    private static final Logger logger = LoggerFactory.getLogger(SapJCoDataProvider.class);
    private static final SapJCoDataProvider INSTANCE = new SapJCoDataProvider();
    private final Map<String, Properties> destinationProperties = new HashMap();
    private final Map<String, Properties> serverProperties = new HashMap();
    private final Map<String, Integer> configOwningDestination = new HashMap();
    private final Map<String, Integer> configOwningServer = new HashMap();
    private DestinationDataEventListener destinationDataEventListener;
    private ServerDataEventListener serverDataListener;

    private SapJCoDataProvider() {}

    /**
     * Gets instance.
     *
     * @return the instance
     */
    public static SapJCoDataProvider getInstance() {
        return INSTANCE;
    }

    /**
     * Gets destination properties.
     *
     * @param destinationName
     *     the destination name
     * @return the destination properties
     */
    @Override
    public Properties getDestinationProperties(String destinationName) {
        if (logger.isDebugEnabled()) {
            logger.debug("Get properties for destination [{}]", destinationName);
        }
        Properties properties = new Properties();
        if (this.destinationProperties.containsKey(destinationName)) {
            properties.putAll((Map)this.destinationProperties.get(destinationName));
        } else {
            logger.warn("No destination properties found for [{}]", destinationName);
        }
        return properties;
    }

    /**
     * Gets server properties.
     *
     * @param programId
     *     the program id
     * @return the server properties
     */
    @Override
    public Properties getServerProperties(String programId) {
        if (logger.isDebugEnabled()) {
            logger.debug("Get server properties for programId [{}]", programId);
        }
        Properties properties = new Properties();
        if (this.serverProperties.containsKey(programId)) {
            properties.putAll((Map)this.serverProperties.get(programId));
        } else {
            logger.warn("No server properties found for programId [{}]", programId);
        }
        return properties;
    }

    /**
     * Sets destination data event listener.
     *
     * @param dataEventListener
     *     the data event listener
     */
    @Override
    public void setDestinationDataEventListener(DestinationDataEventListener dataEventListener) {
        this.destinationDataEventListener = dataEventListener;
    }

    /**
     * Supports events boolean.
     *
     * @return the boolean
     */
    @Override
    public boolean supportsEvents() {
        return true;
    }

    /**
     * Sets server data event listener.
     *
     * @param dataEventListener
     *     the data event listener
     */
    @Override
    public void setServerDataEventListener(ServerDataEventListener dataEventListener) {
        this.serverDataListener = dataEventListener;
    }

    /**
     * Register destination.
     *
     * @param configInstance
     *     the config instance
     * @param destinationName
     *     the destination name
     * @param connectionProperties
     *     the properties
     */
    public void registerDestination(Integer configInstance, String destinationName, Properties connectionProperties) {
        synchronized (configOwningDestination) {
            configOwningDestination.putIfAbsent(destinationName, configInstance);
            Properties registeredDestination = this.destinationProperties.put(destinationName, connectionProperties);
            if (registeredDestination != null && this.destinationDataEventListener != null) {
                this.destinationDataEventListener.updated(destinationName);
                if (logger.isDebugEnabled()) {
                    logger.debug("Notification dispatched to register destination [{}]", destinationName);
                }
            }
            if (logger.isDebugEnabled()) {
                logger.debug("{} destination [{}]", registeredDestination != null ? "Updated" : "Registered",
                    destinationName);
            }
        }
    }

    /**
     * Unregister destination.
     *
     * @param configInstance
     *     the config instance
     * @param destinationName
     *     the destination name
     */
    public void unregisterDestination(Integer configInstance, String destinationName) {
        synchronized (configOwningDestination) {
            if (configOwningDestination.containsKey(destinationName) && !configInstance.equals(
                configOwningDestination.get(destinationName))) {
                logger.warn("Destination [{}] is not owned by configuration [{}]. Unable to unregister it.",
                    destinationName, configInstance);
                return;
            }
            Properties removedDestination = this.destinationProperties.remove(destinationName);
            configOwningDestination.remove(configInstance);
            if (removedDestination != null) {
                if (this.destinationDataEventListener != null) {
                    this.destinationDataEventListener.deleted(destinationName);
                    if (logger.isDebugEnabled()) {
                        logger.debug("Notification dispatched to unregister destination [{}]", destinationName);
                    }
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("Destination [{}] was unregistered", destinationName);
                }
            } else {
                logger.warn("Destination [{}] not found", destinationName);
            }
        }
    }

    /**
     * Register server.
     *
     * @param configInstance
     *     the config instance
     * @param programId
     *     the program id
     * @param connectionProperties
     *     the config
     */
    public void registerServer(Integer configInstance, String programId, Properties connectionProperties) {
        synchronized (configOwningServer) {
            configOwningServer.putIfAbsent(programId, configInstance);
            Properties registeredServer = this.serverProperties.put(programId, connectionProperties);
            if (registeredServer != null && this.serverDataListener != null) {
                this.serverDataListener.updated(programId);
                if (logger.isDebugEnabled()) {
                    logger.debug("Notification dispatched to register server for programId [{}]", programId);
                }
            }
            if (logger.isDebugEnabled()) {
                logger.debug("{} server for programId [{}]", registeredServer != null ? "Updated" : "Registered",
                    programId);
            }
        }
    }

    /**
     * Unregister server.
     *
     * @param configInstance
     *     the config instance
     * @param programId
     *     the program id
     */
    public void unregisterServer(Integer configInstance, String programId) {
        synchronized (configOwningServer) {
            if (configOwningServer.containsKey(programId) && !configInstance
                                                                  .equals(configOwningServer.get(programId))) {
                logger.warn("Server with programId [{}] is not owned by configuration [{}]. Unable to unregister it.",
                    programId, configInstance);
                return;
            }
            Properties removedServer = this.serverProperties.remove(programId);
            configOwningServer.remove(configInstance);
            if (removedServer != null) {
                if (this.serverDataListener != null) {
                    this.serverDataListener.deleted(programId);
                    if (logger.isDebugEnabled()) {
                        logger.debug("Notification dispatched to unregister server for programId [{}]", programId);
                    }
                }
                if (logger.isDebugEnabled()) {
                    logger.debug("Server was unregistered for programId [{}]", programId);
                }
            } else {
                logger.warn("Server not found for programId [{}]", programId);
            }
        }
    }

    /*
     * Remove connection properties for owning connection
     */
    private void unregisterForConfigurationInstance(Integer configInstance) {
        synchronized (configOwningDestination) {
            // unregister destinations owned by configuration instance
            configOwningDestination.entrySet().stream().filter(e -> configInstance.equals(e.getValue()))
                .forEach(e -> unregisterDestination(e.getValue(), e.getKey()));
        }
        synchronized (configOwningServer) {
            // unregister destinations owned by configuration instance
            configOwningServer.entrySet().stream().filter(e -> configInstance.equals(e.getValue()))
                .forEach(e -> unregisterServer(e.getValue(), e.getKey()));
        }
    }

    /**
     * Register Data Provider artifacts into Sap JCo Environment.
     * <p>
     * TODO: SAP Environment allows only one DestinationDataProvider instance and one ServerDataProvider instance,
     * checks what happen when SAP JCo classes are located on a parent classloader and this class is located on a
     * child classloader that can be eventually destroyed...
     */
    public static synchronized void register() {
        if (!Environment.isDestinationDataProviderRegistered()) {
            Environment.registerDestinationDataProvider(SapJCoDataProvider.getInstance());
            logger.info("Destination DataProvider (SapJCoDataProvider) registered.");
        } else {
            logger.warn("Destination data provider already registered.");
        }
        if (!Environment.isServerDataProviderRegistered()) {
            Environment.registerServerDataProvider(SapJCoDataProvider.getInstance());
            logger.info("Server DataProvider (SapJCoDataProvider) registered.");
        } else {
            logger.warn("Server data provider already registered.");
        }
    }

    /**
     * Unregister Data Provider artifacts from Sap JCo Environment.
     * <p>
     * As this class is a singleton we must take care to not remove it from SAP Environment once it is registered to
     * avoid lost destination/server configurations for any other configuration instances. Pay attention to:
     * <p>
     * - Working with datasense within Anypoint studio, many connections will be created/destroyed
     * <p>
     * - Runtime classloader hierarchy, depends on Environment class location and this class location
     * <pre>
     * 1. jdk libs
     *   2. mule runtime libs
     *     3. mule domains libs
     *       4. mule application libs
     *         5. mule connector libs ...
     * </pre>
     * <p>
     * TODO: checks what happen when SAP JCo classes are located on a parent classloader and this class is located on a
     * child classloader that can be eventually destroyed...
     *
     * @param configInstance
     *     the config instance
     */
    public static synchronized void unregister(Integer configInstance) {
        // remove destinations and servers if any, owned by config instance
        SapJCoDataProvider.getInstance().unregisterForConfigurationInstance(configInstance);
    }

}
