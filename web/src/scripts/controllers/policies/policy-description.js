(function() {
  'use strict';

  /*POLICY DESCRIPTION CONTROLLER*/
  angular
    .module('webApp')
    .controller('PolicyDescriptionCtrl', PolicyDescriptionCtrl);

  PolicyDescriptionCtrl.$inject = ['PolicyModelFactory', 'PolicyStaticDataFactory'];

  function PolicyDescriptionCtrl(PolicyModelFactory, PolicyStaticDataFactory ) {
    var vm = this;
    vm.policy = PolicyModelFactory.GetCurrentPolicy();
    vm.validateForm = validateForm;

    vm.sparkStreamingWindowData = PolicyStaticDataFactory.sparkStreamingWindow;
    vm.checkpointIntervalData = PolicyStaticDataFactory.checkpointInterval;
    vm.checkpointAvailabilityData = PolicyStaticDataFactory.checkpointAvailability;
    vm.partitionFormatData = PolicyStaticDataFactory.partitionFormat;

    function validateForm() {
      if ( vm.form.$valid ) PolicyModelFactory.NextStep();
    }
  }
})();
