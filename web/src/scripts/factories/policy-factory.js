(function () {
  'use strict';

  angular
    .module('webApp')
    .factory('PolicyFactory', PolicyFactory);

  PolicyFactory.$inject = ['ApiPolicyService', '$filter', '$q'];

  function PolicyFactory(ApiPolicyService, $filter, $q) {
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
      getPoliciesStatus: function () {
        return ApiPolicyService.getPoliciesStatus().get().$promise;
      },
      getFakePolicy: function () {
        return ApiPolicyService.getFakePolicy().get().$promise;
      },
      existsPolicy: function (policyName, policyId) {
        var defer = $q.defer();
        var found = false;
        var policiesList = this.getAllPolicies();
        policiesList.then(function (policiesDataList) {
          var filteredPolicies = $filter('filter')(policiesDataList, {'policy': {'name': policyName.toLowerCase()}}, true);
          if (filteredPolicies.length > 0) {
            var foundPolicy = filteredPolicies[0].policy;
            if (policyId != foundPolicy.id || policyId === undefined) {
              found = true;
            }
          }
          defer.resolve(found);
        }, function () {
          defer.reject();
        });
        return defer.promise;
      }
    };
  };
})();
