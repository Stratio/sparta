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

  /*POLICY FINISH CONTROLLER*/
  angular
    .module('webApp')
    .controller('PolicyFinishCtrl', PolicyFinishCtrl);

  PolicyFinishCtrl.$inject = ['PolicyModelFactory', 'OutputService', 'UtilsService', '$q'];

  function PolicyFinishCtrl(PolicyModelFactory, OutputService, UtilsService, $q) {
    var vm = this;

    init();

    ///////////////////////////////////////

    function init() {
      vm.policy = PolicyModelFactory.getCurrentPolicy();
      generateFinalJSON().then(function (finalJSON) {
        PolicyModelFactory.setFinalJSON(finalJSON);
        vm.policyJson = JSON.stringify(finalJSON, null, 4);
      });
    }

    function generateFinalJSON() {
      var defer = $q.defer();
      var fragments = [];
      var finalJSON = angular.copy(vm.policy);
      finalJSON.rawData = {};
      finalJSON.rawData.enabled = vm.policy.rawDataEnabled.toString();
      if (vm.policy.rawDataEnabled) {
        finalJSON.rawData.path = (vm.policy.rawDataEnabled) ? vm.policy.rawDataPath : null;
      }
      finalJSON.sparkStreamingWindow = finalJSON.sparkStreamingWindowNumber + finalJSON.sparkStreamingWindowTime;
      finalJSON = convertTriggerAttributes(finalJSON);
      fragments.push(finalJSON.input);
      OutputService.getOutputList().then(function (allOutputs) {
        var cubeOutputs = getCubeOutputs(allOutputs);
        var triggerOutputs = getTriggerOutputs(allOutputs);
        fragments = fragments.concat(cubeOutputs);
        fragments = fragments.concat(triggerOutputs);
        fragments = UtilsService.removeDuplicatedJSONs(fragments, 'id');
        finalJSON.fragments = fragments;
        finalJSON = cleanPolicyJSON(finalJSON);
        finalJSON = UtilsService.convertDottedPropertiesToJson(finalJSON);

        defer.resolve(finalJSON);
      });
      return defer.promise;
    }

    function getCubeOutputs(allOutputs) {
      var cubes = vm.policy.cubes;
      var outputs = [];
      var cubeOutputs = [];
      for (var c = 0; c < cubes.length; ++c) {
        var cube = cubes[c];
        cubeOutputs = cubeOutputs.concat(cube.writer.outputs);

        for (var t = 0; t < cube.triggers.length; ++t) {
          cubeOutputs = cubeOutputs.concat(cube.triggers[t].outputs);
        }
      }
      if (allOutputs && cubeOutputs) {
        outputs = UtilsService.getFilteredJSONByArray(allOutputs, cubeOutputs, 'name');
      }
      return outputs;
    }

    function getTriggerOutputs(allOutputs) {
      var outputs = [];
      var triggers = vm.policy.streamTriggers;
      var triggerOutputs = [];
      for (var t = 0; t < triggers.length; ++t) {
        triggerOutputs = triggerOutputs.concat(triggers[t].outputs);
      }
      if (allOutputs && triggerOutputs) {
        outputs = UtilsService.getFilteredJSONByArray(allOutputs, triggerOutputs, 'name');
      }
      return outputs;
    }

    function convertTriggerAttributes(policyJson) {
      var triggers = policyJson.streamTriggers;
      for (var i = 0; i < policyJson.cubes.length; ++i) {
        triggers = triggers.concat(policyJson.cubes[i].triggers);
      }
      for (var i = 0; i < triggers.length; ++i) {
        triggers[i].overLast = triggers[i].overLastNumber + triggers[i].overLastTime;
        delete triggers[i].overLastNumber;
        delete triggers[i].overLastTime;
      }
      return policyJson;
    }

    function cleanPolicyJSON(finalJSON) {
      delete finalJSON.input;
      delete finalJSON.outputs;
      delete finalJSON['rawDataPath'];
      delete finalJSON['rawDataEnabled'];
      delete finalJSON['sparkStreamingWindowNumber'];
      delete finalJSON['sparkStreamingWindowTime'];

      if (finalJSON.rawData.enabled == 'false') {
        delete finalJSON.rawData['path'];
      }
      return finalJSON;
    }
  }
})();
