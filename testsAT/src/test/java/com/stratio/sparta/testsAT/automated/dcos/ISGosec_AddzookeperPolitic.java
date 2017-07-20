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
package com.stratio.sparta.testsAT.automated.dcos;

import com.stratio.qa.cucumber.testng.CucumberRunner;
import com.stratio.qa.utils.BaseTest;
import com.stratio.qa.utils.ThreadProperty;
import cucumber.api.CucumberOptions;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;


@CucumberOptions(features = {
        "src/test/resources/features/automated/dcos/Gosec_AddzookeperPolitic.feature"

})
public class ISGosec_AddzookeperPolitic extends BaseTest {

    @BeforeClass(groups = {"sparta_eos"})
    public void setUp() {
        ThreadProperty.set("Driver", "inst");

    }

    public ISGosec_AddzookeperPolitic() {this.browser = browser;
    }

    @Test(enabled = true, groups = {"dcos"})
    public void AppWithoutSecurityTest() throws Exception {
        new CucumberRunner(this.getClass()).runCukes();
    }

}