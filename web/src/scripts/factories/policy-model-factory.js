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
(function () {
  'use strict';

  angular
    .module('webApp')
    .factory('PolicyModelFactory', PolicyModelFactory);
  PolicyModelFactory.$inject = ['CubeModelFactory', 'fragmentConstants'];

  function PolicyModelFactory(CubeModelFactory, fragmentConstants) {
    var policy = {};
    var finalJSON = {};
    var template = {};
    var error = {};

    function initPolicy() {
      policy = {};
      policy.name = "";
      policy.description = "";
      policy.input = {};
      policy.outputs = [];
      policy.transformations = {};
      policy.transformations.transformationsPipe = [];
      policy.cubes = [];
      policy.streamTriggers = [];
      policy.sparkConf = {};
      policy.sparkSubmitArguments = [];
      policy.initSqlSentences = [];
      policy.userPluginsJars = [];
      policy.rawData = {};
      delete policy.id;
      ///* Reset policy advanced settings to be loaded from template automatically */
      delete policy.sparkStreamingWindowNumber;
      delete policy.sparkStreamingWindowTime;
      delete policy.storageLevel;
      delete policy.monitoringLink;
      delete policy.checkpointPath;
      delete policy.autoDeleteCheckpoint;
      delete policy.sparkConf;
      delete policy.sparkSubmitArguments;
      delete policy.initSqlSentences;
      delete policy.userPluginsJars;
      delete policy.executionMode;
      delete policy.driverUri;
      delete policy.stopGracefully;
      delete policy.streamTemporalTable;
    }

    function setEditPolicy(inputPolicyJSON) {
      policy = {};
      setPolicy(inputPolicyJSON)
    }

    function setPolicy(inputPolicyJSON) {
      policy.id = inputPolicyJSON.id;
      policy.name = inputPolicyJSON.name;
      policy.driverUri = inputPolicyJSON.driverUri;
      policy.streamTemporalTable = inputPolicyJSON.streamTemporalTable;
      policy.stopGracefully = inputPolicyJSON.stopGracefully;
      policy.description = inputPolicyJSON.description;
      policy.sparkStreamingWindow = inputPolicyJSON.sparkStreamingWindow;
      policy.remember = inputPolicyJSON.remember;
      policy.storageLevel = inputPolicyJSON.storageLevel;
      policy.checkpointPath = inputPolicyJSON.checkpointPath;
      policy.autoDeleteCheckpoint = inputPolicyJSON.autoDeleteCheckpoint;
      policy.executionMode = inputPolicyJSON.executionMode;
      policy.monitoringLink = inputPolicyJSON.monitoringLink;
      policy.transformations = inputPolicyJSON.transformations;
      policy.cubes = setCubes(inputPolicyJSON.cubes);
      policy.rawData = inputPolicyJSON.rawData;
      policy.streamTriggers = setStreamTriggers(inputPolicyJSON.streamTriggers);
      formatAttributes();
      var policyFragments = separateFragments(inputPolicyJSON.fragments);
      policy.input = policyFragments.input;
      policy.sparkConf = inputPolicyJSON.sparkConf;
      policy.sparkSubmitArguments = inputPolicyJSON.sparkSubmitArguments;
      policy.initSqlSentences = inputPolicyJSON.initSqlSentences;
      policy.userPluginsJars = inputPolicyJSON.userPluginsJars;
    }

    function formatAttributes() {
      var sparkStreamingWindow = policy.sparkStreamingWindow.split(/([0-9]+)/);
      policy.sparkStreamingWindowNumber = Number(sparkStreamingWindow[1]);
      policy.sparkStreamingWindowTime = sparkStreamingWindow[2];
      delete policy.sparkStreamingWindow;
      if (policy.remember) {
        var rememberField = policy.remember.split(/([0-9]+)/);
        policy.rememberNumber = Number(rememberField[1]);
        policy.rememberTime = rememberField[2];
      }
      delete policy.remember;
    }

    function setStreamTriggers(streamTriggers) {
      var formattedStreamTriggers = [];
      for (var i = 0; i < streamTriggers.length; ++i) {
        var trigger = streamTriggers[i];
        var overLast = trigger.overLast.split(/([0-9]+)/);
        var computeEvery = trigger.computeEvery.split(/([0-9]+)/);
        trigger.overLastNumber = Number(overLast[1]);
        trigger.overLastTime = overLast[2];
        trigger.computeEveryNumber = Number(computeEvery[1]);
        trigger.computeEveryTime = computeEvery[2];
        delete trigger.overLast;
        delete trigger.computeEvery;
        formattedStreamTriggers.push(trigger);
      }
      return formattedStreamTriggers;
    }

    function setCubes(cubes) {
      var formattedCubes = [];
      for (var i = 0; i < cubes.length; ++i) {
        formattedCubes.push(cubes[i]);
      }
      return formattedCubes;
    }

    function setTemplate(newTemplate) {
      template = newTemplate;
    }

    function getTemplate() {
      return template;
    }

    function separateFragments(fragments) {
      var result = {};
      var input = null;
      var fragment = null;

      for (var i = 0; i < fragments.length; ++i) {
        fragment = fragments[i];
        if (fragment.fragmentType == fragmentConstants.INPUT) {
          input = fragment;
        }
      }

      result.input = input;
      return result;
    }


    function getCurrentPolicy() {
      if (Object.keys(policy).length == 0)
        initPolicy();
      return policy;
    }

    function resetPolicy() {
      initPolicy();
    }

    function getAllModelOutputs() {
      var allModelOutputs = [];
      var models = policy.transformations.transformationsPipe;
      var outputs = [];
      var modelOutputs, output = null;
      for (var i = 0; i < models.length; ++i) {
        modelOutputs = models[i].outputFields;
        for (var j = 0; j < modelOutputs.length; ++j) {
          output = modelOutputs[j];
          if (outputs.indexOf(output) == -1) {
            allModelOutputs.push(output.name);
          }
        }
      }
      return allModelOutputs;
    }

    function getFinalJSON() {
      return finalJSON;
    }

    function setFinalJSON(json) {
      return finalJSON = json;
    }

    function isValidSparkStreamingWindow() {
      var valid = true;
      if (policy.streamTriggers && policy.sparkStreamingWindowNumber > 0) {
        var i = 0;
        while (valid && i < policy.streamTriggers.length) {
          valid = valid &&
            ((policy.streamTriggers[i].overLastNumber % policy.sparkStreamingWindowNumber) == 0) &&
            ((policy.streamTriggers[i].computeEveryNumber % policy.sparkStreamingWindowNumber) == 0);
          ++i;
        }
      }
      return valid;
    }

    function getError() {
      return error;
    }

    function setError(text, type, subErrors) {
      error.text = text;
      error.type = type;
      error.subErrors = subErrors;
    }

    return {
      setPolicy: setPolicy,
      setEditPolicy:setEditPolicy,
      setTemplate: setTemplate,
      getTemplate: getTemplate,
      getCurrentPolicy: getCurrentPolicy,
      resetPolicy: resetPolicy,
      getAllModelOutputs: getAllModelOutputs,
      getFinalJSON: getFinalJSON,
      setFinalJSON: setFinalJSON,
      isValidSparkStreamingWindow: isValidSparkStreamingWindow,
      getError: getError,
      setError: setError
    }
  }

})();


