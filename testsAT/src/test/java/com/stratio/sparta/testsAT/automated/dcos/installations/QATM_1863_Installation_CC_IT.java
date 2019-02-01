/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.testsAT.automated.dcos.installations;

import com.stratio.qa.cucumber.testng.CucumberRunner;
import com.stratio.qa.utils.BaseTest;
import cucumber.api.CucumberOptions;
import org.testng.annotations.Test;


@CucumberOptions(features = {"src/test/resources/features/automated/dcos/01_installations/QATM_1863_Installation_CC.feature"
},format = "json:target/cucumber.json")
public class QATM_1863_Installation_CC_IT extends BaseTest {

    public QATM_1863_Installation_CC_IT() {
    }

    @Test(enabled = true, groups = {"Installation_CC"})
    public void AppWithoutSecurityTest() throws Exception {
        new CucumberRunner(this.getClass()).runCukes();
    }

}
