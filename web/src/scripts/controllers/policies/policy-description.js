(function () {
  'use strict';

  /*POLICY DESCRIPTION CONTROLLER*/
  angular
    .module('webApp')
    .controller('PolicyDescriptionCtrl', PolicyDescriptionCtrl);

  PolicyDescriptionCtrl.$inject = ['PolicyModelFactory', 'PolicyFactory', '$filter', '$q'];

  function PolicyDescriptionCtrl(PolicyModelFactory, PolicyFactory, $filter, $q) {
    var vm = this;

    vm.validateForm = validateForm;

    init();

    function init() {
      vm.template = PolicyModelFactory.getTemplate();
      vm.policy = PolicyModelFactory.getCurrentPolicy();
      vm.sparkStreamingWindowData = vm.template.sparkStreamingWindow;
      vm.checkpointIntervalData = vm.template.checkpointInterval;
      vm.checkpointAvailabilityData = vm.template.checkpointAvailability;
      vm.storageLevelData = vm.template.storageLevel;
      vm.helpLink = vm.template.helpLinks.description;
      vm.error = false;
    }

    function validateForm() {
      if (vm.form.$valid) {
        vm.error = false;
        /*Check if the name of the policy already exists*/
        return findPolicyWithSameName().then(function (found) {
          vm.error = found;
          if (!found) {
            vm.policy.rawData.enabled = vm.policy.rawData.enabled.toString();
            PolicyModelFactory.nextStep();
          }
        });
      }
    }

    function findPolicyWithSameName() {
      var defer = $q.defer();
      var found = false;
      var policiesList = PolicyFactory.getAllPolicies();
      policiesList.then(function (policiesDataList) {
        var filteredPolicies = $filter('filter')(policiesDataList, {'policy': {'name': vm.policy.name.toLowerCase()}}, true);

        if (filteredPolicies.length > 0) {
          var foundPolicy = filteredPolicies[0].policy;
          if (vm.policy.id != foundPolicy.id || vm.policy.id === undefined) {
            found = true;
          }
        }
        defer.resolve(found);
      }, function () {
        defer.reject();
      });
      return defer.promise;
    }
  }
})();
