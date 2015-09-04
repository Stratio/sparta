(function() {
  'use strict';

  /*POLICY DESCRIPTION CONTROLLER*/
  angular
    .module('webApp')
    .controller('PolicyDescriptionCtrl', PolicyDescriptionCtrl);

  PolicyDescriptionCtrl.$inject = ['policyModelFactory', 'PolicyStaticDataFactory'];

  function PolicyDescriptionCtrl(PolicyModelFactory, PolicyStaticDataFactory ) {
    var vm = this;
    vm.policy = PolicyModelFactory.GetCurrentPolicy();

    vm.sparkStreamingWindowData = PolicyStaticDataFactory.sparkStreamingWindow;
    vm.checkpointIntervalData = PolicyStaticDataFactory.checkpointInterval;
    vm.checkpointAvailabilityData = PolicyStaticDataFactory.checkpointAvailability;
  }
})();
