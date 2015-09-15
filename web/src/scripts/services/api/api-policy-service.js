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
    vm.getAllpolicies = getAllPolicies;
    vm.createPolicy = createPolicy;
    vm.deletePolicy = deletePolicy;
    vm.runPolicy = runPolicy;
    vm.getFakePolicy = getFakePolicy;
    vm.savePolicy = savePolicy;

    /////////////////////////////////

    function getPolicyById() {
      return $resource('/policy/find/:id', {id: '@id'},
        {
          'get': {method: 'GET'},
          timeout: apiConfigSettings.timeout
        });
    };

    function getPolicyByFragmentId() {
      return $resource('/policy/fragment/:type/:id', {type: '@type', id: '@id'},
        {
          'get': {method: 'GET', isArray: true},
          timeout: apiConfigSettings.timeout
        });
    };

    function getAllPolicies() {
      return $resource('/policy/all', {},
        {
          'get': {
            method: 'GET', isArray: true,
            timeout: apiConfigSettings.timeout
          }
        });
    };

    function createPolicy() {
      return $resource('/policy', {},
        {
          'create': {
            method: 'POST',
            timeout: apiConfigSettings.timeout
          }
        });
    };

    function deletePolicy() {
      return $resource('/policy/:id', {id: '@id'},
        {
          'delete': {
            method: 'DELETE',
            timeout: apiConfigSettings.timeout
          }
        });
    };

    function runPolicy() {
      return $resource('/policy/run/:id', {id: '@id'},
        {
          'get': {
            method: 'GET',
            timeout: apiConfigSettings.timeout
          }
        });
    };

    function getFakePolicy() {
      return $resource('/data-templates/fake_data/create_policies.json', {},
        {
          'get': {
            method: 'GET',
            timeout: apiConfigSettings.timeout
          }
        });
    };

    function savePolicy() {
      return $resource('/policy', {},
        {
          'create': {
            method: 'PUT',
            timeout: apiConfigSettings.timeout
          }
        });
    }
  };
})();
