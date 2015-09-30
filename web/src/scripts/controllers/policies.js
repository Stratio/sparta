(function () {
  'use strict';

  angular
    .module('webApp')
    .controller('PoliciesCtrl', PoliciesCtrl);

  PoliciesCtrl.$inject = ['PolicyFactory', '$modal', '$state', '$translate', '$interval', '$filter', '$scope', '$timeout'];

  function PoliciesCtrl(PolicyFactory, $modal, $state, $translate, $interval, $filter, $scope, $timeout) {
    /*jshint validthis: true*/
    var vm = this;

    vm.policiesData = {};
    vm.policiesData.list = undefined;
    vm.policiesJsonData = {};
    vm.deletePolicy = deletePolicy;
    vm.runPolicy = runPolicy;
    vm.stopPolicy = stopPolicy;
    vm.editPolicy = editPolicy;
    vm.error = false;
    vm.success = false;
    vm.errorMessage = '';
    vm.successMessage = '';
    init();

    /////////////////////////////////

    function init() {
      getPolicies();

      /*Stop $interval when changing the view*/
      $scope.$on("$destroy",function(){
          if (angular.isDefined(vm.checkPoliciesStatus)) {
            $interval.cancel(vm.checkPoliciesStatus);
          }
      });
    }

    function getPolicies() {
      var policiesList = PolicyFactory.getAllPolicies();

      policiesList.then(function (result) {
        vm.error = false;
        vm.policiesData.list = result;

        vm.checkPoliciesStatus = $interval(function() {
          var policiesStatus = PolicyFactory.getPoliciesStatus();

          policiesStatus.then(function (result) {
            for (var i=0; i < result.length; i++) {
              var policyData = $filter('filter')(vm.policiesData.list, {'policy':{'id':result[i].id}}, true)[0];
              if (policyData) {
                policyData.status = result[i].status;
              }
            }
          },function (error) {
          });
        }, 5000);

      },function (error) {
        $translate('_INPUT_ERROR_' + error.data.i18nCode + '_').then(function(value){
            vm.error = true;
            vm.success = false;
            vm.successMessage = value;
          });
      });
    };

    function editPolicy(route, policyId, policyStatus) {
      if(policyStatus.toLowerCase() === 'notstarted' || policyStatus.toLowerCase() === 'failed' || policyStatus.toLowerCase() === 'stopped' || policyStatus.toLowerCase() === 'stopping') {
        $state.go(route,{"id":policyId});
      }
      else {
        $translate('_POLICY_ERROR_EDIT_POLICY_').then(function(value){
          vm.error = true;
          vm.success = false;
          vm.errorMessage = value;
        });
      }
    };

    function deletePolicy(policyId, policyStatus, index) {
      if(policyStatus.toLowerCase() === 'notstarted' || policyStatus.toLowerCase() === 'failed' || policyStatus.toLowerCase() === 'stopped' || policyStatus.toLowerCase() === 'stopping') {
        var policyToDelete =
        {
          'id': policyId,
          'index': index
        };
        deletePolicyConfirm('lg', policyToDelete);
      }
      else {
        $translate('_POLICY_ERROR_DELETE_POLICY_').then(function(value){
          vm.error = true;
          vm.success = false;
          vm.errorMessage = value;
        });
      }
    };

    function runPolicy(policyId, policyStatus, policyName) {
      if (policyStatus.toLowerCase() === 'notstarted' || policyStatus.toLowerCase() === 'failed' || policyStatus.toLowerCase() === 'stopped' || policyStatus.toLowerCase() === 'stopping') {
        var policyRunning = PolicyFactory.runPolicy(policyId);

        policyRunning.then(function (result) {
          $translate('_RUN_POLICY_OK_', {policyName: policyName}).then(function(value){
            vm.error = false;
            vm.success = true;
            vm.successMessage = value;
          });
          $timeout(function(){vm.success = false}, 5000);

        },function (error) {
          $translate('_INPUT_ERROR_' + error.data.i18nCode + '_').then(function(value){
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

    function stopPolicy(policyId, policyStatus, policyName) {
      if (policyStatus.toLowerCase() !== 'notstarted' && policyStatus.toLowerCase() !== 'stopped' && policyStatus.toLowerCase() !== 'stopping') {

        var stopPolicy =
        {
          "id": policyId,
          "status": "Stopping"
        };

        var policyStopping = PolicyFactory.stopPolicy(stopPolicy);

        policyStopping.then(function (result) {
          $translate('_STOP_POLICY_OK_', {policyName: policyName}).then(function(value){
            vm.error = false;
            vm.success = true;
            vm.successMessage = value;
          });
          $timeout(function(){vm.success = false}, 5000);

        },function (error) {
          $translate('_INPUT_ERROR_' + error.data.i18nCode + '_').then(function(value){
            vm.error = true;
            vm.success = false;
            vm.errorMessage = value;
            vm.errorMessageExtended = 'Error: ' + error.data.message;
          });
        });
      }
      else {
        $translate('_STOP_POLICY_KO_', {policyName: policyName}).then(function(value){
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
        $translate('_POLICY_DELETE_OK_').then(function(value){
          vm.error = false;
          vm.success = true;
          vm.successMessage = value;
          $timeout(function(){vm.success = false}, 5000);
        });

      },function () {
      });
    };

  }
})();
