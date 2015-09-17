(function () {
  'use strict';

  angular
    .module('webApp')
    .controller('PoliciesCtrl', PoliciesCtrl);

  PoliciesCtrl.$inject = ['PolicyFactory', '$modal', '$state', '$translate'];

  function PoliciesCtrl(PolicyFactory, $modal, $state, $translate) {
    /*jshint validthis: true*/
    var vm = this;

    vm.policiesData = {};
    vm.policiesData.list = undefined;
    vm.policiesJsonData = {};
    vm.deletePolicy = deletePolicy;
    vm.runPolicy = runPolicy;
    vm.error = false;
    vm.success = false;
    vm.errorMessage = '';
    vm.successMessage = '';
    init();

    /////////////////////////////////

    function init() {
      getPolicies();
    }

    function getPolicies() {
      var policiesList = PolicyFactory.GetAllPolicies();

      policiesList.then(function (result) {
        vm.error = false;
        vm.policiesData.list = result;
      },function (error) {
        $translate('_INPUT_ERROR_' + error.data.i18nCode + '_').then(function(value){
            console.log(error);
            vm.error = true;
            vm.success = false;
            vm.successMessage = value;
          });
      });
    };

    function deletePolicy(policyId, index) {
      var policyToDelete =
      {
        'id': policyId,
        'index': index
      };
      deletePolicyConfirm('lg', policyToDelete);
    };

    function runPolicy(policyId, policyStatus, policyName) {
      if (policyStatus.toLowerCase() === 'notstarted' || policyStatus.toLowerCase() === 'failed') {
        var policyRunning = PolicyFactory.RunPolicy(policyId);

        policyRunning.then(function (result) {
          $translate('_RUN_POLICY_OK_', {policyName: policyName}).then(function(value){
            vm.error = false;
            vm.success = true;
            vm.successMessage = value;
          });

        },function (error) {
          $translate('_INPUT_ERROR_' + error.data.i18nCode + '_').then(function(value){
            console.log(error);
            vm.error = true;
            vm.success = false;
            vm.errorMessage = value;
            vm.errorMessageExtended = 'Error: ' + error.data.message;
          });
        });
      }
      else {
        $translate('_RUN_POLICY_KO_', {policyName: policyName}).then(function(value){
          vm.error = true;
          vm.success = false;
          vm.errorMessage = value;
        });
      }
    };

    function deletePolicyConfirm(size, policy) {

      var modalInstance = $modal.open({
        animation: true,
        templateUrl: 'templates/policies/st-delete-policy-modal.tpl.html',
        controller: 'DeletePolicyModalCtrl as vm',
        size: size,
        resolve: {
            item: function () {
                return policy;
            }
        }
      });

      modalInstance.result.then(function (selectedPolicy) {
        vm.policiesData.list.splice(selectedPolicy.index, 1);

      },function () {
      });
    };
  }
})();
