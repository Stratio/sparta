/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.qa.utils;

import com.stratio.qa.utils.BaseGTest;
import com.stratio.qa.utils.ThreadProperty;
import org.testng.ITestContext;
import org.testng.annotations.*;

import java.lang.reflect.Method;

abstract public class BaseTest extends BaseGTest {

    protected String browser = "";

    @BeforeSuite(alwaysRun = true)
    public void beforeSuite(ITestContext context) {
    }

    @AfterSuite(alwaysRun = true)
    public void afterSuite(ITestContext context) {
    }

    @BeforeClass(alwaysRun = true)
    public void beforeClass(ITestContext context) {
    }

    @BeforeMethod(alwaysRun = true)
    public void beforeMethod(Method method) {
        ThreadProperty.set("browser", this.browser);
    }

    @AfterMethod(alwaysRun = true)
    public void afterMethod(Method method) {
    }

    @AfterClass()
    public void afterClass() {
    }
}
