package com.stratio.sparta.testsAT.manual;

import com.stratio.sparta.testsAT.utils.BaseTest;
import org.testng.annotations.Test;

import com.stratio.cucumber.testng.CucumberRunner;

import cucumber.api.CucumberOptions;

@CucumberOptions(features = { "src/test/resources/features/manual" })
public class ATmanual extends BaseTest {

    public ATmanual() {
    }

    @Test(enabled = true, groups = {"manual"})
    public void manualTest() throws Exception {
        new CucumberRunner(this.getClass()).runCukes();
    }
}