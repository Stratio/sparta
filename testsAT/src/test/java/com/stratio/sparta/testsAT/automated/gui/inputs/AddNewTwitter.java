package com.stratio.sparta.testsAT.automated.gui.inputs;

import com.stratio.sparta.testsAT.utils.BaseTest;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import com.stratio.cucumber.testng.CucumberRunner;
import com.stratio.data.BrowsersDataProvider;

import cucumber.api.CucumberOptions;

@CucumberOptions(features = { "src/test/resources/features/automated/gui/inputs/addNewInputTwitter.feature" })
public class AddNewTwitter extends BaseTest {
    
    @Factory(enabled = false, dataProviderClass = BrowsersDataProvider.class, dataProvider = "availableUniqueBrowsers")
    public AddNewTwitter(String browser) {
	this.browser = browser;
    }

    @Test(enabled = true, groups = {"web"})
    public void addNewTest() throws Exception {
        new CucumberRunner(this.getClass()).runCukes();
    }
}