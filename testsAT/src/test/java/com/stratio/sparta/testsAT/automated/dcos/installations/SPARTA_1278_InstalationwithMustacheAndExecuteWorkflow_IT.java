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
package com.stratio.sparta.testsAT.automated.dcos.installations;

import com.stratio.qa.cucumber.testng.CucumberRunner;
import com.stratio.qa.data.BrowsersDataProvider;
import com.stratio.qa.utils.BaseTest;
import cucumber.api.CucumberOptions;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

@CucumberOptions(features = {
    "src/test/resources/features/automated/dcos/01_installations/SPARTA_1162_Gosec_AddzookeperPolicy_IT.feature",
    "src/test/resources/features/automated/dcos/01_installations/SPARTA_1273_AddConfigurationForSparta_IT.feature",
    "src/test/resources/features/automated/dcos/01_installations/SPARTA_1238_InstalationwithMustache_IT.feature",
    "src/test/resources/features/automated/dcos/02_executions/SPARTA_1196_GenerateAndExecuteWorkflowWithStreaming_IT.feature"
})

public class SPARTA_1278_InstalationwithMustacheAndExecuteWorkflow_IT extends BaseTest  {
    @Factory(enabled = false, dataProviderClass = BrowsersDataProvider.class, dataProvider = "availableUniqueBrowsers")
    public SPARTA_1278_InstalationwithMustacheAndExecuteWorkflow_IT(String browser) {
        this.browser = browser;
    }

    @Test(enabled = true, groups = {"dcos_executions"}, dependsOnGroups = {"dcos_instalation"})
    public void AppWithSecurityES() throws Exception {
        new CucumberRunner(this.getClass()).runCukes();
    }
}
