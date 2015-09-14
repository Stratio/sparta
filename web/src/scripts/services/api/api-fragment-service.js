(function () {
  'use strict';

  angular
    .module('webApp')
    .service('ApiFragmentService', ApiFragmentService);

  ApiFragmentService.$inject = ['$resource', 'apiConfigSettings'];

  function ApiFragmentService($resource, apiConfigSettings) {
    var vm = this;

    vm.GetFragmentById = GetFragmentById;
    vm.GetFragments = GetFragments;
    vm.DeleteFragment = DeleteFragment;
    vm.CreateFragment = CreateFragment;
    vm.UpdateFragment = UpdateFragment;
    vm.GetFakeFragments = GetFakeFragments;

    /////////////////////////////////

    function GetFragmentById() {
      return $resource('/fragment/:type/:id', {type: '@type', id: '@id'},
        {
          'get': {method: 'GET',
            timeout: apiConfigSettings.timeout}
        });
    };

    function GetFragments() {
      return $resource('/fragment/:type', {type: '@type'},
        {
          'get': {method: 'GET', isArray: true,
            timeout: apiConfigSettings.timeout}
        });
    }

    function CreateFragment() {
      return $resource('/fragment/', {},
        {
          'create': {method: 'POST',
            timeout: apiConfigSettings.timeout}
        });
    };

    function UpdateFragment() {
      return $resource('/fragment/', {},
        {
          'update': {method: 'PUT',
            timeout: apiConfigSettings.timeout}
        });
    };

    function DeleteFragment() {
      return $resource('/fragment/:type/:id', {type: '@type', id: '@id'},
        {
          'delete': {method: 'DELETE',
            timeout: apiConfigSettings.timeout}
        });
    };

    function GetFakeFragments() {
      return $resource('/data-templates/fake_data/:type', {type: '@type'},
        {
          'get': {method: 'GET', isArray: true,
            timeout: apiConfigSettings.timeout}
        });
    };
  }
})();
