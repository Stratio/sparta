(function () {
  'use strict';

  /*POLICY EDITION CONTROLLER*/
  angular
    .module('webApp')
    .controller('EditPolicyCtrl', EditPolicyCtrl);

  EditPolicyCtrl.$inject = ['PolicyStaticDataFactory', 'PolicyModelFactory', 'PolicyFactory', '$q', '$modal', '$state', '$stateParams'];
  function EditPolicyCtrl(PolicyStaticDataFactory, PolicyModelFactory, PolicyFactory, $q, $modal, $state, $stateParams) {
    var vm = this;

    vm.confirmPolicy = confirmPolicy;

    init();

    function init() {
      var defer = $q.defer();
      var id = $stateParams.id;
      vm.steps = PolicyStaticDataFactory.steps;
      vm.status = PolicyModelFactory.getProcessStatus();
      vm.successfullySentPolicy = false;
      vm.error = null;
      PolicyFactory.getPolicyById(id).then(
        function (policyJSON) {
          PolicyModelFactory.setPolicy(policyJSON);
          vm.policy = PolicyModelFactory.getCurrentPolicy();
          defer.resolve();
        }
        , function () {
          defer.reject();
        });
      return defer.promise;
    }

    function confirmPolicy() {
      var defer = $q.defer();
      var modalInstance = $modal.open({
        animation: true,
        templateUrl: 'templates/policies/st-confirm-policy-modal.tpl.html',
        controller: 'ConfirmPolicyModalCtrl as vm',
        size: 'lg'
      });

      modalInstance.result.then(function () {
        PolicyFactory.savePolicy(vm.policy).then(function () {
          PolicyModelFactory.resetPolicy();
          $state.go("dashboard.policies");

          defer.resolve();
        }, function (error) {
          if (error) {
            vm.error = error.data;
          }
          defer.reject();
        });

      }, function () {
        defer.resolve();
      });

      return defer.promise;
    };
  };
})();
