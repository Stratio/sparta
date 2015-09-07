(function () {
  'use strict';

  angular
    .module('webApp')
    .factory('policyModelFactory', policyModelFactory);

  function policyModelFactory() {
    var policy = null;

    function initPolicy() {
      policy = {};
      policy.name = "";
      policy.currentStep = 0;
      policy.sparkStreamingWindow = 0;
      policy.checkpointInterval = 0;
      policy.checkpointAvailability = 0;
      policy.checkpointPath = "";
      policy.path = "";
      policy.rawData = {};
      policy.rawData.enabled = false;
      policy.rawData.partitionFormat = "day";
      policy.rawData.path = "";
      policy.input = {};
      policy.outputs = [];
    }

    return {
      GetCurrentPolicy: function () {
        if (!policy)
          initPolicy();
        return policy;
      }
    }
  }

})();


