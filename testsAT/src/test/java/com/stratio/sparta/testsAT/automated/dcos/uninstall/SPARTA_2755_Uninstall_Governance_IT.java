/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.testsAT.automated.dcos.uninstall;

import com.stratio.qa.cucumber.testng.CucumberRunner;
import com.stratio.qa.data.BrowsersDataProvider;
import com.stratio.qa.utils.BaseTest;
import cucumber.api.CucumberOptions;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

@CucumberOptions(features = {
        "src/test/resources/features/automated/dcos/03_uninstall/SPARTA_2811_Uninstall_Governance.feature"
},format = "json:target/cucumber.json")
public class SPARTA_2755_Uninstall_Governance_IT extends BaseTest {

    @Factory(enabled = false, dataProviderClass = BrowsersDataProvider.class, dataProvider = "availableUniqueBrowsers")
    public SPARTA_2755_Uninstall_Governance_IT(String browser) {
        this.browser = browser;
    }

    @Test(enabled = true, groups = {"Governance_Uninstall"})
    public void AppWithSecurityES() throws Exception {
        new CucumberRunner(this.getClass()).runCukes();
    }

}