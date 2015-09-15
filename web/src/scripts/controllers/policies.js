(function () {
  'use strict';

  angular
    .module('webApp')
    .controller('PoliciesCtrl', PoliciesCtrl);

  PoliciesCtrl.$inject = ['PolicyFactory', '$modal'];

  function PoliciesCtrl(PolicyFactory, $modal) {
    /*jshint validthis: true*/
    var vm = this;

    vm.policiesData = {};
    vm.policiesData.list = [];
    vm.policiesJsonData = {};
    vm.deletePolicy = deletePolicy;
    vm.runPolicy = runPolicy;
    init();

    /////////////////////////////////

    function init() {
      getPolicies();
    }

    function getPolicies() {
      var policiesList = PolicyFactory.getAllPolicies();

      policiesList.then(function (result) {
        vm.policiesData.list = result;
      },function (error) {
        console.log('There was an error while loading the policies!');
        console.log(error);
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

    function runPolicy(policyId) {
      var policyRunning = PolicyFactory.RunPolicy(policyId);

      policyRunning.then(function (result) {

      },function (error) {
        console.log('There was an error while running the policy!');
        console.log(error);
      });
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
