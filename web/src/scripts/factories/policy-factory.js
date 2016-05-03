/*
 * Copyright (C) 2015 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
      deletePolicy: function (policyId) {
        return ApiPolicyService.deletePolicy().delete({'id': policyId}).$promise;
      },
      runPolicy: function (policyId) {
        return ApiPolicyService.runPolicy().get({'id': policyId}).$promise;
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
      downloadPolicy: function (policyId) {
        return ApiPolicyService.downloadPolicy().get({'id': policyId}).$promise;
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
  }
})();
