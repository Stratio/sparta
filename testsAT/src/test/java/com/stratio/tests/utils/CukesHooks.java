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
package com.stratio.tests.utils;

import com.stratio.cucumber.testng.ICucumberFormatter;
import com.stratio.cucumber.testng.ICucumberReporter;
import com.stratio.sparta.testsAT.specs.BaseSpec;
import com.stratio.sparta.testsAT.specs.Common;
import gherkin.formatter.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class CukesHooks extends BaseSpec implements ICucumberReporter, ICucumberFormatter {

    private final Logger logger = LoggerFactory.getLogger(this.getClass().getCanonicalName());

    Background background;
    Feature feature;
    Scenario scenario;
    ScenarioOutline scenarioOutline;

    public CukesHooks() {
    }

    public CukesHooks(Common spec) {
        this.commonspec = spec;
    }

    @Override
    public void syntaxError(String state, String event, List<String> legalEvents, String uri, Integer line) {
        // TODO Auto-generated method stub
    }

    @Override
    public void uri(String uri) {
        // TODO Auto-generated method stub
    }

    @Override
    public void examples(Examples examples) {
        // logger.info("Got to examples {} table", examples.getName());
    }

    @Override
    public void startOfScenarioLifeCycle(Scenario scenario) {
        // logger.info("Got to scenario {} life cycle start",
        // scenario.getName());

    }

    @Override
    public void done() {
        // TODO Auto-generated method stub
    }

    @Override
    public void close() {
        // TODO Auto-generated method stub
    }

    @Override
    public void eof() {
        logger.info("Got to end of file");
    }

    @Override
    public void background(Background background) {
        // logger.info("Got to background {}", background.getName());
        this.background = background;
    }

    @Override
    public void feature(Feature feature) {
        // logger.info("Starting running feature {}", feature.getName());
        this.feature = feature;
    }

    @Override
    public void scenario(Scenario scenario) {
        // logger.info("Got to scenario {} ", scenario.getName());
        this.scenario = scenario;
    }

    @Override
    public void scenarioOutline(ScenarioOutline scenarioOutline) {
        // logger.info("Got to scenarioOutline {} ", scenarioOutline.getName());
        this.scenarioOutline = scenarioOutline;
    }

    @Override
    public void step(Step step) {
        // logger.info("Got to step {} ", step.getName());
    }

    @Override
    public void endOfScenarioLifeCycle(Scenario scenario) {
        // logger.info("Got to scenario {} life cycle end", scenario.getName());
    }

    @Override
    public void before(Match match, Result result) {
        // TODO Auto-generated method stub

    }

    @Override
    public void result(Result result) {
        // logger.info("Done executing {}: {}", this.scenario.getName(),
        // result.getStatus());
    }

    @Override
    public void after(Match match, Result result) {
        // TODO Auto-generated method stub
    }

    @Override
    public void match(Match match) {
        // TODO Auto-generated method stub
    }

    @Override
    public void embedding(String mimeType, byte[] data) {
        // TODO Auto-generated method stub
    }

    @Override
    public void write(String text) {
        // TODO Auto-generated method stub
    }

}
