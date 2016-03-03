(function () {
  'use strict';

  angular
    .module('webApp')
    .factory('PolicyModelFactory', PolicyModelFactory);
  PolicyModelFactory.$inject = ['fragmentConstants'];

  function PolicyModelFactory(fragmentConstants) {
    var policy = {};
    var status = {};
    var finalJSON = {};
    var template = {};

    function initPolicy() {
      status.currentStep = -1;
      status.nextStepAvailable = false;
      policy.name = "";
      policy.description = "";
      policy.input = {};
      policy.outputs = [];
      policy.transformations = [];
      policy.cubes = [];
      policy.streamTriggers = [];
      ///* Reset policy advanced settings to be loaded from template automatically */
      delete policy.checkpointPath;
      delete policy.sparkStreamingWindowNumber;
      delete policy.sparkStreamingWindowTime;
      delete  policy.storageLevel;
      delete  policy.rawDataEnabled;
      delete  policy.rawDataPath;
    }

    function setPolicy(inputPolicyJSON) {
      status.currentStep = 0;
      policy.id = inputPolicyJSON.id;
      policy.name = inputPolicyJSON.name;
      policy.description = inputPolicyJSON.description;
      policy.sparkStreamingWindow = inputPolicyJSON.sparkStreamingWindow;
      policy.storageLevel = inputPolicyJSON.storageLevel;
      policy.checkpointPath = inputPolicyJSON.checkpointPath;
      policy.rawDataEnabled = (inputPolicyJSON.rawData.enabled == "true");
      policy.rawDataPath = inputPolicyJSON.rawData.path;
      policy.transformations = inputPolicyJSON.transformations;
      policy.cubes = inputPolicyJSON.cubes;
      policy.streamTriggers = inputPolicyJSON.streamTriggers;
      status.nextStepAvailable = true;
      formatAttributes();
      var policyFragments = separateFragments(inputPolicyJSON.fragments);
      policy.input = policyFragments.input;
    }

    function formatAttributes() {
      var sparkStreamingWindow = policy.sparkStreamingWindow.split(/([0-9]+)/);
      policy.sparkStreamingWindowNumber = Number(sparkStreamingWindow[1]);
      policy.sparkStreamingWindowTime = sparkStreamingWindow[2];
      delete policy.sparkStreamingWindowTime;
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
      var outputs = [];
      var fragment = null;

      for (var i = 0; i < fragments.length; ++i) {
        fragment = fragments[i];
        if (fragment.fragmentType == fragmentConstants.OUTPUT) {
          outputs.push(fragment);
        } else {
          input = fragment;
        }
      }

      result.input = input;
      result.outputs = outputs;
      return result;
    }


    function getCurrentPolicy() {
      if (Object.keys(policy).length == 0)
        initPolicy();
      return policy;
    }

    function previousStep() {
      status.currentStep--;
    }

    function nextStep() {
      status.currentStep++;
      status.nextStepAvailable = false;
    }

    function enableNextStep() {
      status.nextStepAvailable = true;
    }

    function disableNextStep() {
      status.nextStepAvailable = false;
    }

    function getProcessStatus() {
      return status;
    }

    function resetPolicy() {
      initPolicy();
    }

    function getAllModelOutputs() {
      var allModelOutputs = [];
      var models = policy.transformations;
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


    return {
      setPolicy: setPolicy,
      setTemplate: setTemplate,
      getTemplate: getTemplate,
      getCurrentPolicy: getCurrentPolicy,
      previousStep: previousStep,
      nextStep: nextStep,
      enableNextStep: enableNextStep,
      disableNextStep: disableNextStep,
      getProcessStatus: getProcessStatus,
      resetPolicy: resetPolicy,
      getAllModelOutputs: getAllModelOutputs,
      getFinalJSON: getFinalJSON,
      setFinalJSON: setFinalJSON
    }
  }

})();


