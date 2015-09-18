(function () {
  'use strict';

  /*POLICY DESCRIPTION CONTROLLER*/
  angular
    .module('webApp')
    .controller('PolicyDescriptionCtrl', PolicyDescriptionCtrl);

  PolicyDescriptionCtrl.$inject = ['PolicyModelFactory', 'PolicyStaticDataFactory', 'PolicyFactory', '$filter', '$q'];

  function PolicyDescriptionCtrl(PolicyModelFactory, PolicyStaticDataFactory, PolicyFactory, $filter, $q) {
    var vm = this;

    vm.validateForm = validateForm;

    init();

    function init() {
      vm.policy = PolicyModelFactory.getCurrentPolicy();
      vm.sparkStreamingWindowData = PolicyStaticDataFactory.getSparkStreamingWindow();
      vm.checkpointIntervalData = PolicyStaticDataFactory.getCheckpointInterval();
      vm.checkpointAvailabilityData = PolicyStaticDataFactory.getCheckpointAvailability();
      vm.partitionFormatData = PolicyStaticDataFactory.getPartitionFormat();
      vm.storageLevelData = PolicyStaticDataFactory.getStorageLevel();
      vm.helpLink = PolicyStaticDataFactory.getHelpLinks().description;
      vm.error = false;
    }

    function validateForm() {
      var defer = $q.defer();
      if (vm.form.$valid) {
        vm.error = false;
        /*Check if the name of the policy already exists*/
        findPolicyWithSameName().then(function (found) {
          vm.error = found;
          defer.resolve();
        }, function () {
          defer.reject();
        });
      }
      return defer.promise;
    }

    function findPolicyWithSameName() {
      var defer = $q.defer();
      var found = false;
      var policiesList = PolicyFactory.getAllPolicies();
      policiesList.then(function (result) {
        var policiesDataList = result;
        var filteredPolicies = $filter('filter')(policiesDataList, {'name': vm.policy.name.toLowerCase()}, true);

        if (filteredPolicies.length > 0) {
          var foundPolicy = filteredPolicies[0];
          if (vm.policy.id != foundPolicy.id || vm.policy.id === undefined) {
            found = true;
          }
        }
        if (!found) {
          vm.policy.rawData.enabled = vm.policy.rawData.enabled.toString();
          PolicyModelFactory.nextStep();
        }
        defer.resolve(found);
      }, function () {
        defer.reject();
        console.log('There was an error while getting the policies list');
      });
      return defer.promise;
    }
  }
})();
