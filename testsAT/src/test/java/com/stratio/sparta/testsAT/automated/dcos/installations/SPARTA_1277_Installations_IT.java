/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.testsAT.automated.dcos.installations;

import com.stratio.qa.cucumber.testng.CucumberRunner;
import com.stratio.qa.utils.BaseTest;
import com.stratio.qa.utils.ThreadProperty;
import cucumber.api.CucumberOptions;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


@CucumberOptions(features = {
        "src/test/resources/features/automated/dcos/01_installations/SPARTA_1238_InstalationwithMustache_IT.feature",
        "src/test/resources/features/automated/dcos/01_installations/SPARTA_1278_AddConfigurationForSparta_IT.feature",
})
public class SPARTA_1277_Installations_IT extends BaseTest {

    @BeforeClass(groups = {"sparta_eos"})
    public void setUp() {
        ThreadProperty.set("Driver", "inst");

    }

    public SPARTA_1277_Installations_IT() {this.browser = browser;
    }

    @Test(enabled = true, groups = {"dcos_instalation"})
    public void AppWithoutSecurityTest() throws Exception {
        new CucumberRunner(this.getClass()).runCukes();
    }

}