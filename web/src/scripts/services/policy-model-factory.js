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

    return {
      GetCurrentPolicy: function () {
        if (!policy)
          initPolicy();
        return policy;
      },
      NextStep: function() {
        status.currentStep++;
      },
      GetStatus: function(){
        return status;
      }
    }
  }

})();


