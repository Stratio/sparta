/*
 * © 2017 Stratio Big Data Inc., Sucursal en España. All rights reserved.
 *
 * This software – including all its source code – contains proprietary information of Stratio Big Data Inc., Sucursal en España and may not be revealed, sold, transferred, modified, distributed or otherwise made available, licensed or sublicensed to third parties; nor reverse engineered, disassembled or decompiled, without express written authorization from Stratio Big Data Inc., Sucursal en España.
 */
package com.stratio.sparta.testsAT.manual;

import com.stratio.qa.utils.BaseTest;
import org.testng.annotations.Test;

import com.stratio.qa.cucumber.testng.CucumberRunner;

import cucumber.api.CucumberOptions;

@CucumberOptions(features = { "src/test/resources/features/manual" },format = "json:target/cucumber.json")
public class ATmanual extends BaseTest {

    public ATmanual() {
    }

    @Test(enabled = true, groups = {"manual"})
    public void manualTest() throws Exception {
        new CucumberRunner(this.getClass()).runCukes();
    }
}