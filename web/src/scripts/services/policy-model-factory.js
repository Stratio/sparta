(function () {
  'use strict';

  angular
    .module('webApp')
    .factory('PolicyModelFactory', PolicyModelFactory);
  PolicyModelFactory.$inject = ['fragmentConstants'];

  function PolicyModelFactory(fragmentConstants) {
    var policy = null;
    var allModelOutputs = null;
    var status = {};

    function initPolicy() {
      status.currentStep = 0;
      policy = {};
      policy.name = "";
      policy.description = "";
      policy.sparkStreamingWindow = "6000";
      policy.storageLevel = "MEMORY_AND_DISK_SER";
      policy.checkpointPath = "/tmp/checkpoint";
      policy.rawData = {};
      policy.rawData.enabled = false;
      policy.rawData.partitionFormat = "day";
      policy.rawData.path = "";
      policy.input = {};
      policy.outputs = [];
      policy.models = [];
      policy.cubes = [];
    }

    function setPolicy(inputPolicy) {
      policy = {};
      status.currentStep = 0;
      policy.id = inputPolicy.id;
      policy.name = inputPolicy.name;
      policy.description = inputPolicy.description;
      policy.sparkStreamingWindow = inputPolicy.sparkStreamingWindow;
      policy.storageLevel = inputPolicy.storageLevel;
      policy.checkpointPath = inputPolicy.checkpointPath;
      policy.rawData = inputPolicy.rawData;
      policy.models = inputPolicy.transformations; //TODO Change policy models attribute to transformations
      policy.cubes = inputPolicy.cubes;

      var policyFragments = separateFragments(inputPolicy.fragments);
      policy.input = policyFragments.input;
      policy.outputs = policyFragments.outputs;
    }

    function separateFragments(fragments) {
      var result = {};
      var input = null;
      var outputs = [];
      var fragment = null;

      for (var i = 0; i < fragments.length; ++i) {
        fragment = fragments[i];
        if (fragment.fragmentType == fragmentConstants.OUTPUT) {
          outputs.push(fragment);
        } else
          input = fragment;
      }

      result.input = input;
      result.outputs = outputs;
      return result;
    }

    function getCurrentPolicy() {
      if (!policy)
        initPolicy();
      return policy;
    }

    function nextStep() {
      status.currentStep++;
    }

    function getProcessStatus() {
      return status;
    }

    function resetPolicy() {
      initPolicy();
    }

    function getAllModelOutputs() {
      if (!allModelOutputs) {
        allModelOutputs = [];
        var models = policy.models;
        var outputs = [];
        var modelOutputs, output = null;
        for (var i = 0; i < models.length; ++i) {
          modelOutputs = models[i].outputFields;
          for (var j = 0; j < modelOutputs.length; ++j) {
            output = modelOutputs[j];
            if (outputs.indexOf(output) == -1) {
              allModelOutputs.push(output);
            }
          }
        }
      }
      return allModelOutputs;
    }


    return {
      setPolicy: setPolicy,
      getCurrentPolicy: getCurrentPolicy,
      nextStep: nextStep,
      getProcessStatus: getProcessStatus,
      resetPolicy: resetPolicy,
      getAllModelOutputs: getAllModelOutputs
    }
  }

})();


