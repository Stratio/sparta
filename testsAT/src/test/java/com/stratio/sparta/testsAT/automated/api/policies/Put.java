package com.stratio.sparta.testsAT.automated.api.policies;

import com.stratio.sparta.testsAT.utils.BaseTest;
import org.testng.annotations.Test;

import com.stratio.cucumber.testng.CucumberRunner;

import cucumber.api.CucumberOptions;

@CucumberOptions(features = { "src/test/resources/features/automated/api/policies/putPolicies.feature" })
public class Put extends BaseTest {

    public Put() {
    }

    @Test(enabled = true, groups = {"api"})
    public void policiesTest() throws Exception {
        new CucumberRunner(this.getClass()).runCukes();
    }
}