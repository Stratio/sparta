(function() {
  'use strict';

  /*POLICIY DESCRIPTION CONTROLLER*/
  angular
    .module('webApp')
    .controller('PolicyDescriptionCtrl', PolicyDescriptionCtrl);

  PolicyDescriptionCtrl.$inject = ['NewPoliceService', 'PolicyStaticDataService'];

  function PolicyDescriptionCtrl(NewPoliceService, PolicyStaticDataService ) {
    var vm = this;
    vm = angular.extend(vm, NewPoliceService.GetCurrentPolicy());
    vm.sparkStreamingWindowData = PolicyStaticDataService.sparkStreamingWindow;
    vm.checkpointIntervalData = PolicyStaticDataService.checkpointInterval;
    vm.checkpointAvailabilityData = PolicyStaticDataService.checkpointAvailability;
    console.log(vm)
  }
})();
