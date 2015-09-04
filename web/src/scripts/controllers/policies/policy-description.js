(function() {
  'use strict';

  /*POLICY DESCRIPTION CONTROLLER*/
  angular
    .module('webApp')
    .controller('PolicyDescriptionCtrl', PolicyDescriptionCtrl);

  PolicyDescriptionCtrl.$inject = ['policyModelFactory', 'PolicyStaticDataService'];

  function PolicyDescriptionCtrl(policyModelFactory, PolicyStaticDataService ) {
    var vm = this;
    vm.policy = policyModelFactory.GetCurrentPolicy();

    vm.sparkStreamingWindowData = PolicyStaticDataService.sparkStreamingWindow;
    vm.checkpointIntervalData = PolicyStaticDataService.checkpointInterval;
    vm.checkpointAvailabilityData = PolicyStaticDataService.checkpointAvailability;
  }
})();
