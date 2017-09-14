/*
 * Copyright (C) 2015 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.stratio.sparta.testsAT.automated.dcos.executions;
import com.stratio.qa.cucumber.testng.CucumberRunner;
//import com.stratio.tests.utils.BaseTest;
import com.stratio.qa.utils.BaseTest;
import cucumber.api.CucumberOptions;
import org.testng.annotations.Test;
import org.testng.annotations.Factory;
import com.stratio.qa.data.BrowsersDataProvider;

@CucumberOptions(features = { "src/test/resources/features/automated/dcos/executions/SPARTA_1192_KhermesExecution_IT.feature" })
public class SPARTA_1192_KhermesExecution_IT extends BaseTest {

    @Factory(enabled = false, dataProviderClass = BrowsersDataProvider.class, dataProvider = "availableUniqueBrowsers")
    public SPARTA_1192_KhermesExecution_IT(String browser) {
        this.browser = browser;
    }

    @Test(enabled = true, groups = {"execution"})
    public void execution() throws Exception {
        new CucumberRunner(this.getClass()).runCukes();
    }
}