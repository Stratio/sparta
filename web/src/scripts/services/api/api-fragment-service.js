(function () {
  'use strict';

  angular
    .module('webApp')
    .service('ApiFragmentService', ApiFragmentService);

  ApiFragmentService.$inject = ['$resource', 'apiConfigSettings'];

  function ApiFragmentService($resource, apiConfigSettings) {
    var vm = this;

    vm.getFragmentById = getFragmentById;
    vm.getFragments = getFragments;
    vm.deleteFragment = deleteFragment;
    vm.createFragment = createFragment;
    vm.updateFragment = updateFragment;
    vm.getFakeFragments = getFakeFragments;

    /////////////////////////////////

    function getFragmentById() {
      return $resource('/fragment/:type/:id', {type: '@type', id: '@id'},
        {
          'get': {method: 'GET',
            timeout: apiConfigSettings.timeout}
        });
    };

    function getFragments() {
      return $resource('/fragment/:type', {type: '@type'},
        {
          'get': {method: 'GET', isArray: true,
            timeout: apiConfigSettings.timeout}
        });
    }

    function createFragment() {
      return $resource('/fragment/', {},
        {
          'create': {method: 'POST',
            timeout: apiConfigSettings.timeout}
        });
    };

    function updateFragment() {
      return $resource('/fragment/', {},
        {
          'update': {method: 'PUT',
            timeout: apiConfigSettings.timeout}
        });
    };

    function deleteFragment() {
      return $resource('/fragment/:type/:id', {type: '@type', id: '@id'},
        {
          'delete': {method: 'DELETE',
            timeout: apiConfigSettings.timeout}
        });
    };

    function getFakeFragments() {
      return $resource('/data-templates/fake_data/:type', {type: '@type'},
        {
          'get': {method: 'GET', isArray: true,
            timeout: apiConfigSettings.timeout}
        });
    };
  }
})();
