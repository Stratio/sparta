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
    .service('ApiPolicyService', ApiPolicyService);

  ApiPolicyService.$inject = ['$resource', 'apiConfigSettings'];

  function ApiPolicyService($resource, apiConfigSettings) {
    var vm = this;

    vm.getPolicyById = getPolicyById;
    vm.getPolicyByFragmentId = getPolicyByFragmentId;
    vm.getAllPolicies = getAllPolicies;
    vm.createPolicy = createPolicy;
    vm.deletePolicy = deletePolicy;
    vm.runPolicy = runPolicy;
    vm.getFakePolicy = getFakePolicy;
    vm.savePolicy = savePolicy;
    vm.stopPolicy = stopPolicy;
    vm.getPoliciesStatus = getPoliciesStatus;
    vm.downloadPolicy = downloadPolicy;

    /////////////////////////////////

    function getPolicyById() {
      return $resource('/policy/find/:id', {id: '@id'},
        {
          'get': {method: 'GET'},
          timeout: apiConfigSettings.timeout
        });
    }

    function getPolicyByFragmentId() {
      return $resource('/policy/fragment/:type/:id', {type: '@type', id: '@id'},
        {
          'get': {method: 'GET', isArray: true},
          timeout: apiConfigSettings.timeout
        });
    }

    function getAllPolicies() {
      return $resource('/policy/all', {},
        {
          'get': {
            method: 'GET', isArray: true,
            timeout: apiConfigSettings.timeout
          }
        });
    }

    function createPolicy() {
      return $resource('/policy', {},
        {
          'create': {
            method: 'POST',
            timeout: apiConfigSettings.timeout
          }
        });
    }

    function deletePolicy() {
      return $resource('/policy/:id', {id: '@id'},
        {
          'delete': {
            method: 'DELETE',
            timeout: apiConfigSettings.timeout
          }
        });
    }

    function runPolicy() {
      return $resource('/policy/run/:id', {id: '@id'},
        {
          'get': {
            method: 'GET',
            timeout: apiConfigSettings.timeout
          }
        });
    }

    function stopPolicy() {
      return $resource('/policyContext', {},
        {
          'update': {
            method: 'PUT',
            timeout: apiConfigSettings.timeout
          }
        });
    }

    function getFakePolicy() {
      return $resource('/data-templates/fake_data/create_policies.json', {},
        {
          'get': {
            method: 'GET',
            timeout: apiConfigSettings.timeout
          }
        });
    }

    function savePolicy() {
      return $resource('/policy', {},
        {
          'put': {
            method: 'PUT',
            timeout: apiConfigSettings.timeout
          }
        });
    }

    function getPoliciesStatus() {
      return $resource('/policyContext', {},
        {
          'get': {
            method: 'GET',
            timeout: apiConfigSettings.timeout
          }
        });
    }


    function downloadPolicy() {
      return $resource('/policy/download/:id', {id: '@id'},
        {
          'get': {method: 'GET'},
          timeout: apiConfigSettings.timeout
        });
    }
  }
})();
