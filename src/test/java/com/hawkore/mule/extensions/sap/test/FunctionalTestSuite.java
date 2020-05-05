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
package com.hawkore.mule.extensions.sap.test;

import com.hawkore.mule.extensions.sap.test.functional.SapOperationsTestCase;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * The type Functional test suite.
 */
@RunWith(Suite.class)
@SuiteClasses({SapOperationsTestCase.class})
public class FunctionalTestSuite {

    /**
     * Initialise suite.
     */
    @BeforeClass
    public static synchronized void initialiseSuite() {

    }

    /**
     * Shutdown suite.
     */
    @AfterClass
    public static synchronized void shutdownSuite() {

    }

}
