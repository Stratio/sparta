(function () {
  'use strict';

  /*POLICY INPUTS CONTROLLER*/
  angular
    .module('webApp')
    .controller('PolicyFinishCtrl', PolicyFinishCtrl);

  PolicyFinishCtrl.$inject = ['PolicyModelFactory', '$modal'];

  function PolicyFinishCtrl(PolicyModelFactory, $modal) {
    var vm = this;

    vm.confirmPolicy = confirmPolicy;

    init();

    ///////////////////////////////////////

    function init() {
      vm.policy = PolicyModelFactory.GetCurrentPolicy();
      vm.testingpolcyData = JSON.stringify(vm.policy, null, 4);
    };

    function confirmPolicy() {
      var modalInstance = $modal.open({
        animation: true,
        templateUrl: 'templates/policies/st-confirm-policy-modal.tpl.html',
        controller: 'ConfirmPolicyModalCtrl as vm',
        size: 'lg'
      });

      modalInstance.result.then(function () {
        // Call a function to create the policy

      },function () {
        console.log('Modal dismissed at: ' + new Date())
      });
    };

  };
})();
