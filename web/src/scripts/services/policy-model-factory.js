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
      policy.rawData = {};
      policy.rawData.enabled = false;
      policy.rawData.path = "";
      policy.input = {};
      policy.outputs = [];
      policy.transformations = [];
      policy.cubes = [];
    }

    function setPolicy(inputPolicyJSON) {
      status.currentStep = -1;
      policy.id = inputPolicyJSON.id;
      policy.name = inputPolicyJSON.name;
      policy.description = inputPolicyJSON.description;
      policy.sparkStreamingWindow = inputPolicyJSON.sparkStreamingWindow;
      policy.storageLevel = inputPolicyJSON.storageLevel;
      policy.checkpointPath = inputPolicyJSON.checkpointPath;
      policy.rawData = inputPolicyJSON.rawData;
      policy.rawData.enabled = (inputPolicyJSON.rawData.enabled == "true");
      policy.transformations = inputPolicyJSON.transformations;
      policy.cubes = inputPolicyJSON.cubes;

      var policyFragments = separateFragments(inputPolicyJSON.fragments);
      policy.input = policyFragments.input;
      policy.outputs = policyFragments.outputs;
    }

    function setTemplate(newTemplate){
      template = newTemplate;
    }

    function getTemplate(){
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
        } else
          input = fragment;
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

    function enableNextStep(){
      status.nextStepAvailable = true;
    }

    function disableNextStep(){
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
      enableNextStep:enableNextStep,
      disableNextStep:disableNextStep,
      getProcessStatus: getProcessStatus,
      resetPolicy: resetPolicy,
      getAllModelOutputs: getAllModelOutputs,
      getFinalJSON: getFinalJSON,
      setFinalJSON: setFinalJSON
    }
  }

})();


