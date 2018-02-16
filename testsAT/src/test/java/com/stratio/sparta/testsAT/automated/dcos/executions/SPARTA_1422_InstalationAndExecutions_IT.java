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
import com.stratio.qa.utils.BaseTest;
import com.stratio.qa.utils.ThreadProperty;
import cucumber.api.CucumberOptions;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


@CucumberOptions(features = {
        "src/test/resources/features/automated/dcos/01_installations/SPARTA_1161_FullSecurityInstalation_IT.feature",
        "src/test/resources/features/automated/dcos/01_installations/SPARTA_1162_Gosec_AddzookeperPolicy_IT.feature",
        "src/test/resources/features/automated/dcos/01_installations/SPARTA_1238_InstalationwithMustache_IT.feature",
        "src/test/resources/features/automated/dcos/01_installations/SPARTA_1278_AddConfigurationForSparta_IT.feature",
        "src/test/resources/features/automated/dcos/02_executions/SPARTA-1279_KafkaPostgres_IT.feature"
})
public class SPARTA_1422_InstalationAndExecutions_IT extends BaseTest {

    @BeforeClass(groups = {"sparta_eos"})
    public void setUp() {
        ThreadProperty.set("Driver", "inst");

    }

    public SPARTA_1422_InstalationAndExecutions_IT() {this.browser = browser;
    }

    @Test(enabled = true, groups = {"dcos_installations_executions"})
    public void AppWithoutSecurityTest() throws Exception {
        new CucumberRunner(this.getClass()).runCukes();
    }

}