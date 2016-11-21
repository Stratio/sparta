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

  angular
      .module('webApp')
      .directive('formFieldArray', formFieldArray);

  formFieldArray.$inject = ['$document'];
  function formFieldArray($document) {
    var directive = {
      link: link,
      templateUrl: 'stratio-ui/template/form/form_field_array.html',
      restrict: 'AE',
      replace: true,
      scope: {
        name: '@stName',
        field: '=',
        form: '=',
        model: '=',
        qa: '@'
      }
    }
    return directive;

    function link(scope, element, attrs) {
      scope.name = "";
      scope.showHelp = false;
      scope.itemToAdd = "";
      init();

      function init() {
        /* init array element if doesn't exit */
        if (!scope.model) {
          scope.model = [];
        }
      }

      scope.addItem = function($event) {
        $event.preventDefault();
        if (scope.itemToAdd) {
          scope.model.push(scope.itemToAdd);
          scope.itemToAdd = "";
        }
      };

      scope.removeItem = function(index) {
        scope.model[scope.field.propertyId].splice(index, 1);
      };

      scope.toggleHelp = function(event) {
        if (scope.showHelp) {
          scope.showHelp = false;
          $document.unbind('click', externalClickHandler);
        } else {
          scope.showHelp = true;
          $document.bind('click', externalClickHandler);
        }
      };

      function externalClickHandler(event) {
        if (event.target.id == "help-" + scope.name) {
          return;
        }
        $document.unbind('click', externalClickHandler);
        scope.showHelp = false;
        scope.$apply();
      }
    }
  }
})();
