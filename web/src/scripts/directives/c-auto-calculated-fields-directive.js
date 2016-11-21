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
(function() {
  'use strict';

  /* Accordion directive */

  angular
      .module('webApp')
      .directive('cAutoCalculatedFields', cAutoCalculatedFields);

  cAutoCalculatedFields.$inject = ['apiConfigSettings', '$resource'];

  function cAutoCalculatedFields(apiConfigSettings, $resource) {

    var directive = {
      restrict: 'E',
      scope: {
        model: "=",
        form: "="
      },
      replace: true,
      templateUrl: 'templates/components/c-auto-calculated-fields.tpl.html',
      link: link
    };

    return directive;

    function link(scope) {

      if (!scope.model) {
        scope.model = [];
      }
      scope.currentAutoCalculatedFieldType = "";
      scope.addAutoCalculatedField = function() {
        var autoCalculatedField = {};
        autoCalculatedField[scope.currentAutoCalculatedFieldType] = {field: {}};
        scope.model.push(autoCalculatedField);
      };
      scope.removeAutoCalculatedField = function(position) {
        scope.model.splice(position, 1);
      };

      scope.getType = function(autoCalculatedField) {
        return Object.keys(autoCalculatedField)[0];
      };

      getAutoCalculatedFieldTemplate().get().$promise.then(function(template) {
        scope.template = template;
      });
    }

    function getAutoCalculatedFieldTemplate() {
      return $resource('/data-templates/auto-calculated-field.json', {},
          {
            'get': {
              method: 'GET',
              timeout: apiConfigSettings.timeout
            }
          });
    }
  }
})();
