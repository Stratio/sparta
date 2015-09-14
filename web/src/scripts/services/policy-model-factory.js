(function () {
  'use strict';

  angular
    .module('webApp')
    .factory('PolicyModelFactory', PolicyModelFactory);

  function PolicyModelFactory() {
    var policy = null;
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

    function getCurrentPolicy() {
      if (!policy)
        initPolicy();
      return policy;
    }

    function nextStep() {
      status.currentStep++;
    }

    function getStatus() {
      return status;
    }

    function resetPolicy() {
      initPolicy();
    }

    return {
      getCurrentPolicy: getCurrentPolicy,
      nextStep: nextStep,
      getStatus: getStatus,
      resetPolicy: resetPolicy
    }
  }

})();


