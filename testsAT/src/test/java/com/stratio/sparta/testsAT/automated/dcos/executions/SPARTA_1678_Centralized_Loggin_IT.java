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

@CucumberOptions(features = {

        "src/test/resources/features/automated/dcos/02_executions/SPARTA_1678_Centralized_Logging_App_IT.feature",
        "src/test/resources/features/automated/dcos/02_executions/SPARTA_1678_Centralized_Logging_Workflow_IT.feature"

},format = "json:target/cucumber.json")

public class SPARTA_1678_Centralized_Loggin_IT extends BaseTest  {
    public SPARTA_1678_Centralized_Loggin_IT() {this.browser = browser;
    }
    @Test(enabled = true, groups = {"dcos_centralLoggin"})
    public void ExecuteWorkflow() throws Exception {
        new CucumberRunner(this.getClass()).runCukes();
    }
}
