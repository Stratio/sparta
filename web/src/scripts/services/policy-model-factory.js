(function () {
  'use strict';

  angular
    .module('webApp')
    .factory('PolicyModelFactory', PolicyModelFactory);

  function PolicyModelFactory() {
    var policy = null;

    function initPolicy() {
      policy = {};
      policy.currentStep = 0;
      policy.name = "";
      policy.description = "";
      policy.sparkStreamingWindow = "6000";
      policy.storageLevel = "MEMORY_AND_DISK_SER";
      policy.checkpointPath = "/tmp/checkpoint";
      policy.rawData = {};
      policy.rawData.enabled = false;
      policy.rawData.partitionFormat = "day";
      policy.rawData.path = "";
      policy.currentStep = 0;
      policy.input = {};
      policy.outputs = [];
      policy.models = [];
      policy.cubes = [];
    }

    return {
      GetCurrentPolicy: function () {
        if (!policy)
          initPolicy();
        return policy;
      },
      NextStep: function() {
        policy.currentStep++;
      }
    }
  }

})();


