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
    .service('ApiTemplateService', ApiTemplateService);

  ApiTemplateService.$inject = ['$resource', 'apiConfigSettings'];

  function ApiTemplateService($resource, apiConfigSettings) {
    var vm = this;

    vm.getFragmentTemplateByType = getFragmentTemplateByType;
    vm.getPolicyTemplate = getPolicyTemplate;
    vm.getDimensionTemplateByType = getDimensionTemplateByType;
    vm.getOperatorTemplateByType = getOperatorTemplateByType;
    vm.getTriggerTemplateByType = getTriggerTemplateByType;
    vm.getPolicyJsonTemplate = getPolicyJsonTemplate;
    vm.getRawDataJsonTemplate = getRawDataJsonTemplate;

    /////////////////////////////////

    function getFragmentTemplateByType() {
      return $resource('data-templates/:type', {
        type: '@type'
      }, {
        'get': {
          method: 'GET',
          isArray: true,
          timeout: apiConfigSettings.timeout
        }
      });
    }

    function getPolicyTemplate() {
      return $resource('data-templates/policy.json', {}, {
        'get': {
          method: 'GET',
          timeout: apiConfigSettings.timeout
        }
      });
    }

    function getPolicyJsonTemplate() {
      return $resource('data-templates/policy-modal.json', {}, {
        'get': {
          method: 'GET',
          timeout: apiConfigSettings.timeout
        }
      });
    }


    function getRawDataJsonTemplate() {
      return $resource('data-templates/raw-data.json', {}, {
        'get': {
          method: 'GET',
          timeout: apiConfigSettings.timeout
        }
      });
    }

    function getDimensionTemplateByType() {
      return $resource('data-templates/dimension/:type', {
        type: '@type'
      }, {
        'get': {
          method: 'GET',
          timeout: apiConfigSettings.timeout
        }
      });
    }

    function getDimensionTemplateByType() {
      return $resource('data-templates/dimension/:type', {
        type: '@type'
      }, {
        'get': {
          method: 'GET',
          timeout: apiConfigSettings.timeout
        }
      });
    }

    function getOperatorTemplateByType() {
      return $resource('data-templates/operator/:type', {
        type: '@type'
      }, {
        'get': {
          method: 'GET',
          timeout: apiConfigSettings.timeout
        }
      });
    }

    function getTriggerTemplateByType() {
      return $resource('data-templates/trigger/:type', {
        type: '@type'
      }, {
        'get': {
          method: 'GET',
          timeout: apiConfigSettings.timeout
        }
      });
    }
  }
})();
