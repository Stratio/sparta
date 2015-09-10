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
    vm.deletePolicy = deletePolicy;
    init();

    /////////////////////////////////

    function init() {
      getPolicies();
    }

    function getPolicies() {
      var policiesList = PolicyFactory.GetAllPolicies();

      policiesList.then(function (result) {
        console.log('--> Getting wizard');
        console.log('> Getting list of wizard');
        console.log(result);
        vm.policiesData.list = result;
      });
    };

    function deletePolicy(policyId, index) {
      console.log('--> Deleting policy');
      var policyToDelete =
      {
        'id': policyId,
        'index': index
      };
      deletePolicyConfirm('lg', policyToDelete);
    };

    function deletePolicyConfirm(size, policy) {

      var modalInstance = $modal.open({
        animation: true,
        templateUrl: 'templates/wizard/st-delete-policy-modal.tpl.html',
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
        console.log(vm.policiesData.list);

      },function () {
        console.log('Modal dismissed at: ' + new Date())
      });
    };

  }
})();
