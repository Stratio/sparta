package com.stratio.sparkta.testsAT.automated.gui.policies;

import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.stratio.cucumber.testng.CucumberRunner;
import com.stratio.data.BrowsersDataProvider;
import com.stratio.sparkta.testsAT.utils.BaseTest;

import cucumber.api.CucumberOptions;

@CucumberOptions(features = { "src/test/resources/features/automated/gui/policies/editPolicy.feature" })
public class Edit extends BaseTest {
    
    @Factory(enabled = false, dataProviderClass = BrowsersDataProvider.class, dataProvider = "availableUniqueBrowsers")
    public Edit(String browser) {
	this.browser = browser;
    }

    @Test(enabled = true, groups = {"web"})
    public void checkElementsTest() throws Exception {
        new CucumberRunner(this.getClass()).runCukes();
    }
}