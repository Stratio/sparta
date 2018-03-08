/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.testsAT.automated.workflows;

import com.stratio.qa.cucumber.testng.CucumberRunner;
import com.stratio.qa.utils.BaseTest;
import cucumber.api.CucumberOptions;
import org.testng.annotations.Test;
import org.testng.annotations.Factory;
import com.stratio.qa.data.BrowsersDataProvider;

@CucumberOptions(features = { "src/test/resources/features/automated/workflows/SPARTA_1487_executeWorkflow_IT.feature" })
public class SPARTA_1487_ISExecuteWorkflow_IT extends BaseTest {
    public SPARTA_1487_ISExecuteWorkflow_IT() {
    }

    @Test(enabled = true, groups = {"api","basic"})
    public void policiesTest() throws Exception {
        new CucumberRunner(this.getClass()).runCukes();
    }
}