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


@CucumberOptions(features = {"src/test/resources/features/automated/dcos/02_executions/SPARTA-1333_CopyHdfs_IT.feature"
},format = "json:target/cucumber.json")
public class SPARTA_1161_CopyHDFS_IT extends BaseTest {

    public SPARTA_1161_CopyHDFS_IT() {
    }
    @Test(enabled = true, groups = {"copy_hdfs"})
    public void AppWithoutSecurityTest() throws Exception {
        new CucumberRunner(this.getClass()).runCukes();
    }
}
