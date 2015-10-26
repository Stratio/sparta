package com.stratio.sparkta.testsAT.automated.api.policies;

import org.testng.annotations.Test;

import com.stratio.cucumber.testng.CucumberRunner;
import com.stratio.sparkta.testsAT.utils.BaseTest;

import cucumber.api.CucumberOptions;

@CucumberOptions(features = { "src/test/resources/features/automated/api/policies/deletePolicies.feature" })
public class Delete extends BaseTest {

    public Delete() {
    }

    @Test(enabled = true, groups = {"api"})
    public void policiesTest() throws Exception {
        new CucumberRunner(this.getClass()).runCukes();
    }
}