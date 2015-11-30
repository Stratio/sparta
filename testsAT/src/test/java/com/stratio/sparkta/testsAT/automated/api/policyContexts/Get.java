package com.stratio.sparkta.testsAT.automated.api.policyContexts;

import org.testng.annotations.Test;

import com.stratio.cucumber.testng.CucumberRunner;
import com.stratio.sparkta.testsAT.utils.BaseTest;

import cucumber.api.CucumberOptions;

@CucumberOptions(features = { "src/test/resources/features/automated/api/policyContexts/getPolicyContexts.feature" })
public class Get extends BaseTest {

    public Get() {
    }

    @Test(enabled = true, groups = {"api","basic"})
    public void policyContextsTest() throws Exception {
        new CucumberRunner(this.getClass()).runCukes();
    }
}
