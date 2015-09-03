'use strict';

/*POLICIES STEP CONTROLLER*/
angular
  .module('webApp')
  .controller('NewPolicyCtrl', NewPolicyCtrl);

NewPolicyCtrl.$inject = ['PolicyStaticDataService'];
function NewPolicyCtrl(PolicyStaticDataService) {
  var vm = this;

  vm.steps = PolicyStaticDataService.steps;
  vm.sparkStreamingWindow = PolicyStaticDataService.sparkStreamingWindow;
  vm.checkpointInterval = PolicyStaticDataService.checkpointInterval;
  vm.checkpointAvailability = PolicyStaticDataService.checkpointAvailability;

  vm.currentStep = 0;
  vm.sparkStreamingWindow = 0;
  vm.checkpointInterval = 0;
  vm.checkpointAvailability = 0;
  vm.checkpointPath = 0;
};
