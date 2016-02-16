package com.stratio.sparta.testsAT.automated.api.policyContexts;

import org.testng.annotations.Test;

import com.stratio.cucumber.testng.CucumberRunner;
import com.stratio.sparta.testsAT.utils.BaseTest;

import cucumber.api.CucumberOptions;

@CucumberOptions(features = { "src/test/resources/features/automated/api/policyContexts/postPolicyContexts.feature" })
public class Post extends BaseTest {

    public Post() {
    }

    @Test(enabled = true, groups = {"api"})
    public void fragmentsTest() throws Exception {
        new CucumberRunner(this.getClass()).runCukes();
    }
}