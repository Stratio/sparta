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
      policy.input = {};
      policy.outputs = [];

      policy.models = [];
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


