package com.stratio.sparkta.testsAT.automated.endtoend;

import org.testng.annotations.Test;

import com.stratio.cucumber.testng.CucumberRunner;
import com.stratio.sparkta.testsAT.utils.BaseTest;

import cucumber.api.CucumberOptions;

@CucumberOptions(features = { "src/test/resources/features/automated/endtoend/iSocketoCassandra.feature" })
public class ISocketOCassandra extends BaseTest {

    public ISocketOCassandra() {
    }

    @Test(enabled = true, groups = {"endtoend"})
    public void iSocketoCassandraTest() throws Exception {
        new CucumberRunner(this.getClass()).runCukes();
    }
}