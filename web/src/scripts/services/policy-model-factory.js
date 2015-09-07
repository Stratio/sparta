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
      policy.checkpointPath = "/tmp/checkpoint";
      policy.rawData = {};
      policy.rawData.enabled = false;
      policy.rawData.partitionFormat = "day";
      policy.rawData.path = "";
      policy.currentStep = 0;
      policy.input = {};
      policy.outputs = [];

      policy.models = [];
    }

    return {
      GetCurrentPolicy: function () {
        if (!policy)
          initPolicy();
        return policy;
      },
      NextStep: function() {
        policy.currentStep++;
        console.log(policy.currentStep);
      }
    }
  }

})();


