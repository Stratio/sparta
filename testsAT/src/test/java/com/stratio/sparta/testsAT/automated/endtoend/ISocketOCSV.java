package com.stratio.sparta.testsAT.automated.endtoend;

import com.stratio.sparta.testsAT.utils.BaseTest;
import org.testng.annotations.Test;

import com.stratio.cucumber.testng.CucumberRunner;

import cucumber.api.CucumberOptions;

@CucumberOptions(features = { "src/test/resources/features/automated/endtoend/iSocketoCSV.feature" })
public class ISocketOCSV extends BaseTest {

    public ISocketOCSV() {
    }

    @Test(enabled = true, groups = {"endtoend"})
    public void iSocketoCSVTest() throws Exception {
        new CucumberRunner(this.getClass()).runCukes();
    }
}
