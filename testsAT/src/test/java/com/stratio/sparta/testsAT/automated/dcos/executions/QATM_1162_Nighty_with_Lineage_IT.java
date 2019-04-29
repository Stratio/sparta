/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.testsAT.automated.dcos.executions;

import com.stratio.qa.cucumber.testng.CucumberRunner;
import com.stratio.qa.data.BrowsersDataProvider;
import com.stratio.qa.utils.BaseTest;
import cucumber.api.CucumberOptions;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

@CucumberOptions(features = {
        "src/test/resources/features/automated/dcos/01_installations/QATM_1863_Installation_CC.feature",
        "src/test/resources/features/automated/dcos/02_executions/SPARTA-1279_KafkaPostgres_fullSec_IT.feature",
        "src/test/resources/features/automated/dcos/02_executions/SPARTA_1890_GeneraliBatchworkflow_fullSec_IT.feature",
        "src/test/resources/features/automated/dcos/02_executions/SPARTA_1895_CarrefourBatchworkflow_fullSec_IT.feature",
        "src/test/resources/features/automated/dcos/02_executions/SPARTA-2811_Lineage_fullSec_IT.feature",
        "src/test/resources/features/automated/dcos/03_uninstall/QATM_1863_Uninstall_CC.feature"
},format = "json:target/cucumber.json")
public class QATM_1162_Nighty_with_Lineage_IT extends BaseTest {

    @Factory(enabled = false, dataProviderClass = BrowsersDataProvider.class, dataProvider = "availableUniqueBrowsers")
    public QATM_1162_Nighty_with_Lineage_IT(String browser) {
        this.browser = browser;
    }

    @Test(enabled = true, groups = {"nightly_lineage"})
    public void AppWithSecurityES() throws Exception {
        new CucumberRunner(this.getClass()).runCukes();
    }

}