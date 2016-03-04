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

  /*ICON RESPONSIBLE TO ADD A NEW ELEMENT TO THE INTRODUCED MODEL*/

  angular
    .module('webApp')
    .directive('cAddElement', cAddElement);


  function cAddElement() {
    var directive = {
      restrict: 'E',
      scope: {
        iconClass: "=iconClass",
        model: "=model",
        type: "=type",
        disabled: "=disabled",
        qa: "@",
        pattern: "=",
        placeholder: "@",
        error: '=',
        submittedForm: '=',
        label: '=',
        help: '=',
        limit: '=',
        isObject: "=",
        attribute: "@",
        required:"=",
        width: '='
      },
      replace: "true",
      templateUrl: 'templates/components/c-add-element.tpl.html',
      link: link
    };

    return directive;

    function link(scope) {
      scope.addInput = addInput;
      scope.errorLimit = false;
      function addInput(event) {
        if (scope.inputToAdd !== '' && scope.inputToAdd !== undefined && (event.keyCode == '13' || event.type === "click")) {
          var inputExists = false;
          scope.errorLimit = false;
          if (scope.limit === 0 || scope.model.length < scope.limit) {
            for (var i = 0; i < scope.model.length; i++) {
              if (scope.model[i].name === scope.inputToAdd) {
                inputExists = true;
              }
            }
            if (inputExists) {
              scope.submittedForm = true;
              scope.error = true;
            }
            else {
              scope.submittedForm = false;
              scope.error = false;

              if (scope.isObject) {
                var objectToAdd = {};
                objectToAdd[scope.attribute] =  scope.inputToAdd;
                scope.model.push(objectToAdd);
              } else
                scope.model.push(scope.inputToAdd);
              scope.inputToAdd = '';
            }
            event.preventDefault();
          }
          else {
            scope.submittedForm = true;
            scope.errorLimit = true;
          }
        }
      }
    }
  }
})();
