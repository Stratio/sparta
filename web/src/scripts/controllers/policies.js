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
      var policiesList = PolicyFactory.GetAllPolicies();

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
            console.log('********** Error when uploading policies status');
            console.log(error);
          });
        }, 5000);




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
      if (policyStatus.toLowerCase() === 'notstarted' || policyStatus.toLowerCase() === 'failed' || policyStatus.toLowerCase() === 'stopped') {
        var policyRunning = PolicyFactory.RunPolicy(policyId);

        policyRunning.then(function (result) {
          $translate('_RUN_POLICY_OK_', {policyName: policyName}).then(function(value){
            vm.error = false;
            vm.success = true;
            vm.successMessage = value;
          });







          $timeout(function(){vm.success = false}, 5000);




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

    function stopPolicy(policyId, policyStatus, policyName) {
      if (policyStatus.toLowerCase() !== 'notstarted' && policyStatus.toLowerCase() !== 'stopped') {

        var stopPolicy =
        {
          "id": policyId,
          "status": "Stopping"
        };

        var policyStopping = PolicyFactory.StopPolicy(stopPolicy);

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

      },function () {
      });
    };
  }
})();
