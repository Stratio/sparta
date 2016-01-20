package com.stratio.sparkta.testsAT.automated.api.policies;

import org.testng.annotations.Test;

import com.stratio.cucumber.testng.CucumberRunner;
import com.stratio.sparkta.testsAT.utils.BaseTest;

import cucumber.api.CucumberOptions;

@CucumberOptions(features = { "src/test/resources/features/automated/api/policies/iSocketoCassandra.feature" })
public class ISocketOCassandra extends BaseTest {

    public ISocketOCassandra() {
    }

    @Test(enabled = true, groups = {"api"})
    public void iSocketoCassandraTest() throws Exception {
        new CucumberRunner(this.getClass()).runCukes();
    }
}