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

  /*LINE WITH A FORM CONTROL AND A LIST OF INPUT FIELDS*/

  angular
    .module('webApp')
    .directive('cOutputFieldList', cOutputFieldList);


  function cOutputFieldList() {
    var directive = {
      restrict: 'E',
      scope: {
        label: '=',
        editionMode: '=',
        labelControlText: "=",
        labelControlClass: '=',
        inputControlClass: '=',
        formControlClass: "=",
        placeholder: '=',
        inputText: "=",
        rightText: "=",
        model: "=",
        inputType: "=",
        pattern: "=",
        inputs: "=",
        readonly: "=",
        enableDelete: "=",
        required: "=",
        outputTypes: "=",
        qa: "@",
        help: '@',
        helpQa: '@',
        defaultType: '=',
        disabledType: '=',
        deleteTransformInput: '='
      },
      replace: "true",
      templateUrl: 'templates/components/c-output-field-list.tpl.html',
      link: link
    };

    return directive;

    function link(scope) {
      scope.deleteInput = deleteInput;

      function deleteInput(index) {
        if(scope.deleteTransformInput){
          scope.deleteTransformInput(index);
          return;
        }
        scope.inputs.splice(index,1);
      }
    }
  }
})();
