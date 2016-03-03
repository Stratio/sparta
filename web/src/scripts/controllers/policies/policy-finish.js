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
      for (var c = 0; c < cubes.length; ++c) {
        var cubeOutputs = cubes[c].writer.outputs;
        for (var t = 0; t <  cubes[c].triggers.length; ++t) {
          cubeOutputs.concat(cubes[c].triggers[t].outputs);
        }
      }
      var outputs = UtilsService.getFilteredJSONByArray(allOutputs, cubeOutputs, 'name');
      return outputs;
    }

    function getTriggerOutputs(allOutputs) {
      var usedOutputs = [];

      var triggers = vm.policy.streamTriggers;
      var triggerOutputs = [];
      for (var t = 0; t < triggers.length; ++t) {
        triggerOutputs.push(triggers[t].outputs);
      }
      var outputs = UtilsService.getFilteredJSONByArray(allOutputs, triggerOutputs, 'name');
      usedOutputs.push(outputs);

      return usedOutputs;
    }

    function convertTriggerAttributes(policyJson){
      var triggers = policyJson.streamTriggers;
      for (var i = 0; i < policyJson.cubes.length; ++i) {
       triggers = triggers.concat(policyJson.cubes[i].triggers);
      }
      for (var i = 0; i < triggers.length; ++i) {
        triggers[i].overLast =  triggers[i].overLastNumber +  triggers[i].overLastTime;
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

      if (finalJSON.rawData.enabled == 'false') {
        delete finalJSON.rawData['path'];
      }
      return finalJSON;
    }
  }
})();
