(function () {
  'use strict';

  angular
    .module('webApp')
    .factory('PolicyFactory', PolicyFactory);

  PolicyFactory.$inject = ['ApiPolicyService'];

  function PolicyFactory(ApiPolicyService) {
    return {
      getPolicyById: function (policyId) {
        return ApiPolicyService.getPolicyById().get({'id': policyId}).$promise;
      },
      getPolicyByFragmentId: function (fragmentType, fragmentId) {
        return ApiPolicyService.getPolicyByFragmentId().get({'type': fragmentType, 'id': fragmentId}).$promise;
      },
      getAllPolicies: function () {
        return ApiPolicyService.getAllPolicies().get().$promise;
      },
      createPolicy: function (newPolicyData) {
        return ApiPolicyService.createPolicy().create(newPolicyData).$promise;
      },
      deletePolicy: function (policyid) {
        return ApiPolicyService.deletePolicy().delete({'id': policyid}).$promise;
      },
      runPolicy: function (policyid) {
        return ApiPolicyService.runPolicy().get({'id': policyid}).$promise;
      },
      stopPolicy: function (policy) {
        return ApiPolicyService.stopPolicy().update(policy).$promise;
      },
      savePolicy: function (policyData) {
        return ApiPolicyService.savePolicy().put(policyData).$promise;
      },
      getPoliciesStatus: function() {
        return ApiPolicyService.getPoliciesStatus().get().$promise;
      },
      getFakePolicy: function () {
        return ApiPolicyService.getFakePolicy().get().$promise;
      }
    };
  };
})();
