(function () {
  'use strict';

  angular
    .module('webApp')
    .service('ApiPolicyService', ApiPolicyService);

  ApiPolicyService.$inject = ['$resource', 'apiConfigSettings'];

  function ApiPolicyService($resource, apiConfigSettings) {
    var vm = this;

    vm.GetPolicyByFragmentId = GetPolicyByFragmentId;
    vm.GetAllpolicies = GetAllPolicies;
    vm.CreatePolicy = CreatePolicy;
    vm.DeletePolicy = DeletePolicy;
    vm.GetFakePolicy = GetFakePolicy;

    /////////////////////////////////

    function GetPolicyByFragmentId() {
      return $resource('/policy/fragment/:type/:id', {type: '@type', id: '@id'},
        {
          'get': {method: 'GET', isArray: true},
          timeout: apiConfigSettings.timeout
        });
    };

    function GetAllPolicies() {
      return $resource('/policy/all', {},
        {
          'get': {
            method: 'GET', isArray: true,
            timeout: apiConfigSettings.timeout
          }
        });
    };

    function CreatePolicy() {
      return $resource('/policy', {},
        {
          'create': {method: 'POST',
            timeout: apiConfigSettings.timeout}
        });
    };

    function DeletePolicy() {
      return $resource('/policy/:id', {id: '@id'},
        {
          'delete': {method: 'DELETE',
            timeout: apiConfigSettings.timeout}
        });
    };

    function GetFakePolicy() {
      return $resource('/data-templates/fake_data/create_policies.json', {},
        {
          'get': {method: 'GET',
            timeout: apiConfigSettings.timeout}
        });
    };
  };
})();
