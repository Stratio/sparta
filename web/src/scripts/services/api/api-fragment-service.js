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
      return $resource('fragment/:type/id/:fragmentId', {type: '@type', fragmentId: '@fragmentId'},
        {
          'get': {method: 'GET',
            timeout: apiConfigSettings.timeout}
        });
    }

    function getFragments() {
      return $resource('fragment/:type', {type: '@type'},
        {
          'get': {method: 'GET', isArray: true,
            timeout: apiConfigSettings.timeout}
        });
    }

    function createFragment() {
      return $resource('fragment/', {},
        {
          'create': {method: 'POST',
            timeout: apiConfigSettings.timeout}
        });
    }

    function updateFragment() {
      return $resource('fragment/', {},
        {
          'update': {method: 'PUT',
            timeout: apiConfigSettings.timeout}
        });
    }

    function deleteFragment() {
      return $resource('fragment/:type/id/:fragmentId', {type: '@type', fragmentId: '@fragmentId'},
        {
          'delete': {method: 'DELETE',
            timeout: apiConfigSettings.timeout}
        });
    }

    function getFakeFragments() {
      return $resource('data-templates/fake_data/:type', {type: '@type'},
        {
          'get': {method: 'GET', isArray: true,
            timeout: apiConfigSettings.timeout}
        });
    }
  }
})();
