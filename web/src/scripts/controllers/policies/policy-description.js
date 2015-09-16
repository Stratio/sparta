(function () {
  'use strict';

  /*POLICY DESCRIPTION CONTROLLER*/
  angular
    .module('webApp')
    .controller('PolicyDescriptionCtrl', PolicyDescriptionCtrl);

  PolicyDescriptionCtrl.$inject = ['PolicyModelFactory', 'PolicyStaticDataFactory', 'PolicyFactory', '$filter'];

  function PolicyDescriptionCtrl(PolicyModelFactory, PolicyStaticDataFactory, PolicyFactory, $filter) {
    var vm = this;

    vm.validateForm = validateForm;

    init();

    function init(){
      vm.policy = PolicyModelFactory.getCurrentPolicy();
      vm.sparkStreamingWindowData = PolicyStaticDataFactory.sparkStreamingWindow;
      vm.checkpointIntervalData = PolicyStaticDataFactory.checkpointInterval;
      vm.checkpointAvailabilityData = PolicyStaticDataFactory.checkpointAvailability;
      vm.partitionFormatData = PolicyStaticDataFactory.partitionFormat;
      vm.storageLevelData = PolicyStaticDataFactory.storageLevel;
      vm.helpLink = PolicyStaticDataFactory.helpLinks.description;
      vm.error = false;
    }

    function validateForm() {
      if (vm.form.$valid) {
        /*Check if the name of the policy already exists*/
        var policiesList = PolicyFactory.getAllPolicies();

        policiesList.then(function (result) {
          var policiesDataList = result;

          var filteredPolicies = $filter('filter')(policiesDataList, {'name': vm.policy.name.toLowerCase()}, true);
          if (filteredPolicies.length === 0) {

            vm.error = false;
            if (vm.policy.rawData.enabled === false) {
              delete vm.policy.rawData['path'];
              delete vm.policy.rawData['partitionFormat'];
            }
            PolicyModelFactory.nextStep();
          }
          else {
            var foundPolicy = filteredPolicies[0];
            if (vm.policy.id != foundPolicy.id) {
              vm.error = true;
            } else
              PolicyModelFactory.nextStep();
          }
        }, function () {
          console.log('There was an error while getting the policies list');
        });
      }
    }
  }
})();
