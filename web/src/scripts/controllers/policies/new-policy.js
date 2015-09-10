(function () {
  'use strict';

  /*POLICIES STEP CONTROLLER*/
  angular
    .module('webApp')
    .controller('NewPolicyCtrl', NewPolicyCtrl);

  NewPolicyCtrl.$inject = ['PolicyStaticDataFactory', 'PolicyModelFactory', 'PolicyFactory', '$q', '$modal'];
  function NewPolicyCtrl(PolicyStaticDataFactory, PolicyModelFactory, PolicyFactory, $q, $modal) {
    var vm = this;

    vm.steps = PolicyStaticDataFactory.steps;
    vm.policy = PolicyModelFactory.GetCurrentPolicy();
    vm.status = PolicyModelFactory.GetStatus();
    vm.confirmPolicy = confirmPolicy;
    vm.successfullySentPolicy = false;
    vm.error = null;

    function confirmPolicy() {
      var defer = $q.defer();
      var modalInstance = $modal.open({
        animation: true,
        templateUrl: 'templates/policies/st-confirm-policy-modal.tpl.html',
        controller: 'ConfirmPolicyModalCtrl as vm',
        size: 'lg'
      });

      modalInstance.result.then(function () {
        PolicyFactory.CreatePolicy(vm.policy).then(function () {
          defer.resolve();
        }, function (error) {
          vm.error = error;
          defer.reject();
        });

      },function () {
        defer.resolve();
      });

      return defer.promise;
    };
  };
})();
