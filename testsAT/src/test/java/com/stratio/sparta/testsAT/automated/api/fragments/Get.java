package com.stratio.sparta.testsAT.automated.api.fragments;

import com.stratio.sparta.testsAT.utils.BaseTest;
import org.testng.annotations.Test;

import com.stratio.cucumber.testng.CucumberRunner;

import cucumber.api.CucumberOptions;

@CucumberOptions(features = { "src/test/resources/features/automated/api/fragments/getFragments.feature" })
public class Get extends BaseTest {

    public Get() {
    }

    @Test(enabled = true, groups = {"api","basic"})
    public void fragmentsTest() throws Exception {
        new CucumberRunner(this.getClass()).runCukes();
    }
}