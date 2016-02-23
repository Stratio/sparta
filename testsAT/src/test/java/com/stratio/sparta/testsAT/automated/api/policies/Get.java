package com.stratio.sparta.testsAT.automated.api.policies;

import com.stratio.sparta.testsAT.utils.BaseTest;
import org.testng.annotations.Test;

import com.stratio.cucumber.testng.CucumberRunner;

import cucumber.api.CucumberOptions;

@CucumberOptions(features = { "src/test/resources/features/automated/api/policies/getPolicies.feature" })
public class Get extends BaseTest {

    public Get() {
    }

    @Test(enabled = true, groups = {"api","basic"})
    public void policiesTest() throws Exception {
        new CucumberRunner(this.getClass()).runCukes();
    }
}
