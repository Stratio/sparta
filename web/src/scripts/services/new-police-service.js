(function () {
  'use strict';

  angular
    .module('webApp')
    .service('NewPoliceService', NewPoliceService);

  function NewPoliceService() {
    var vm = this;
    var policy = {};

    initPolicy();

    function initPolicy() {
      policy.name = "";
      policy.currentStep = 0;
      policy.sparkStreamingWindow = 0;
      policy.checkpointInterval = 0;
      policy.checkpointAvailability = 0;
      policy.checkpointPath = "";
    };

    vm.GetCurrentPolicy = function () {
      return policy;
    }

  }

})();


