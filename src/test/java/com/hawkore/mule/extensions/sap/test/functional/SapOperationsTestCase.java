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
package com.hawkore.mule.extensions.sap.test.functional;

import java.io.InputStream;

import javax.inject.Inject;
import org.junit.Test;
import org.mule.functional.junit4.MuleArtifactFunctionalTestCase;
import org.mule.runtime.api.artifact.Registry;
import org.mule.runtime.api.component.Component;
import org.mule.runtime.api.component.ComponentIdentifier;
import org.mule.runtime.api.component.location.ConfigurationComponentLocator;
import org.mule.runtime.api.component.location.Location;
import org.mule.runtime.api.metadata.MetadataService;
import org.mule.runtime.api.streaming.bytes.CursorStreamProvider;
import org.mule.test.runner.ArtifactClassLoaderRunnerConfig;
import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

@ArtifactClassLoaderRunnerConfig(
    providedExclusions = {"org.mule.tests:*:*:*:*", "com.mulesoft.compatibility.tests:*:*:*:*"},
    testExclusions = {"org.mule.runtime:*:*:*:*", "org.mule.modules*:*:*:*:*", "org.mule.transports:*:*:*:*",
        "org.mule.mvel:*:*:*:*", "org.mule.extensions:*:*:*:*", "org.mule.connectors:*:*:*:*",
        "org.mule.tests.plugin:*:*:*:*", "com.mulesoft.mule.runtime*:*:*:*:*", "com.mulesoft.licm:*:*:*:*"},
    testInclusions = {"*:*:jar:tests:*", "*:*:test-jar:*:*"},
    applicationSharedRuntimeLibs = {"com.sap:com.sap.conn.idoc.sapidoc3", "com.sap:com.sap.conn.jco.sapjco3",
        "com.sap:com.sap.conn.jco.sapjco3-native"},
    testRunnerExportedRuntimeLibs = {"org.mule.tests:mule-tests-functional"})
public class SapOperationsTestCase extends MuleArtifactFunctionalTestCase {

    /**
     * The constant LOGGER.
     */
    public static final Logger LOGGER = getLogger(SapOperationsTestCase.class);
    /**
     * The Locator.
     */
    @Inject
    protected ConfigurationComponentLocator locator;
    /**
     * The Reg.
     */
    @Inject
    protected Registry reg;

    @Override
    protected String getConfigFile() {
        return "test-mule-sap-jco-connector.xml";
    }

    protected void loadMetadataForOperation(MetadataService service, String operation) {
        for (Component component : locator.find(
            ComponentIdentifier.builder().namespace("sap-jco").name(operation).build())) {
            LOGGER
                .debug("Processing metadata for sap-jco:" + operation + " -> " + component.getLocation().getLocation());
            Location l = Location.builderFromStringRepresentation(component.getLocation().getLocation()).build();
            service.getEntityKeys(l);
            service.getMetadataKeys(l);
            service.getOperationMetadata(l);
        }
    }

    private InputStream getInputStream(Object value) {
        InputStream returnedInputStream;
        if (value instanceof CursorStreamProvider) {
            returnedInputStream = ((CursorStreamProvider)value).openCursor();
        } else if (value instanceof InputStream) {
            returnedInputStream = (InputStream)value;
        } else {
            throw new IllegalArgumentException("Result was not of expected type");
        }
        return returnedInputStream;
    }

    @Test
    public void metadata() throws Exception {
        MetadataService service = reg.lookupByType(MetadataService.class).get();
        loadMetadataForOperation(service, "idoc-instance");
        loadMetadataForOperation(service, "function-instance");
    }

    @Test
    public void executeSendIdoc() throws Exception {
        flowRunner("send-idoc").run();
    }

    @Test
    public void executeIDocSchema() throws Exception {
        flowRunner("idoc-schema").run();
    }

    @Test
    public void executeSendIdocPackage() throws Exception {
        flowRunner("send-idoc-package").run();
    }

    @Test
    public void executeFunctionInstance() throws Exception {
        flowRunner("function-instance").run();
    }

    @Test
    public void executeFunctionSchema() throws Exception {
        flowRunner("function-schema").run();
    }

    @Test
    public void executeFunctionInvoke() throws Exception {
        flowRunner("invoke-function-list-idoc-types").run();
    }

    @Test
    public void executeFunctionInvoke2() throws Exception {
        flowRunner("invoke-function-list-of-functions").run();
    }

}
