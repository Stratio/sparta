/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.testsAT.automated.dcos.executions;

import com.stratio.qa.cucumber.testng.CucumberRunner;
import com.stratio.qa.utils.BaseTest;
import cucumber.api.CucumberOptions;
import org.testng.annotations.Test;
import org.testng.annotations.Factory;
import com.stratio.qa.data.BrowsersDataProvider;

@CucumberOptions(features = {
        "src/test/resources/features/automated/dcos/02_executions/SPARTA_1183_InstallAndExecuteWorkflow_IT.feature"

})

public class SPARTA_1183_InstallAndExecuteWorkflow_IT extends BaseTest  {
    @Factory(enabled = false, dataProviderClass = BrowsersDataProvider.class, dataProvider = "availableUniqueBrowsers")
    public SPARTA_1183_InstallAndExecuteWorkflow_IT(String browser) {
        this.browser = browser;
    }

    @Test(enabled = true, groups = {"dcos"})
    public void AppWithSecurityES() throws Exception {
        new CucumberRunner(this.getClass()).runCukes();
    }
}
