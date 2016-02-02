package com.stratio.sparkta.testsAT.automated.endtoend;

import org.testng.annotations.Test;

import com.stratio.cucumber.testng.CucumberRunner;
import com.stratio.sparkta.testsAT.utils.BaseTest;

import cucumber.api.CucumberOptions;

@CucumberOptions(features = { "src/test/resources/features/automated/endtoend/iSocketoElasticsearch.feature" })
public class ISocketOElasticsearch extends BaseTest {

    public ISocketOElasticsearch() {
    }

    @Test(enabled = true, groups = {"endtoend"})
    public void iSocketoElasticsearchTest() throws Exception {
        new CucumberRunner(this.getClass()).runCukes();
    }
}
