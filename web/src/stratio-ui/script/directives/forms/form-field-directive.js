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
    .directive('formField', formField);

  formField.$inject = ['$timeout'];
  function formField($timeout) {
    var directive = {
      link: link,
      templateUrl: 'stratio-ui/template/form/form_field.html',
      restrict: 'AE',
      replace: true,
      scope: {
        ngFormId: '@',
        name: '@stName',
        field: '=',
        form: '=',
        model: '=',
        listCompressed: '=',
        qa: '@',
        modal: "=",
        deleteClass: "=",
        disabled: '=',
        extraMessage: '='
      }
    };
    return directive;

    function link(scope, element, attrs) {
      $timeout(function () {
        if (scope.field.propertyType !== 'list') {
          var defaultValue = scope.field.default;

          if (defaultValue !== undefined && scope.model[scope.ngFormId] === undefined) {
            scope.model[scope.ngFormId] = defaultValue;
          }
        }

        scope.mimeType = getMimeType();
        scope.uploadFrom = '';

        scope.isVisible = function () {
          scope.modify = {};
          if (scope.field && scope.field.hasOwnProperty('hidden') && scope.field.hidden) {
            scope.model[scope.field.propertyId] = null;
            if (scope.deleteClass) {
              element.removeClass('c-col');
            }
            return false;
          }
          if (scope.field && scope.field.hasOwnProperty('visible')) {
            for (var i = 0; i < scope.field.visible.length; i++) {
              var actuals = scope.field.visible[i];
              var allTrue = true;
              for (var j = 0; j < actuals.length; j++) {
                var actual = actuals[j];
                /*if (actual.value === scope.model[actual.propertyId].value) {*/
                if (actual.value === scope.model[actual.propertyId]) {
                  if (actual.hasOwnProperty('overrideProps')) {
                    for (var f = 0; f < actual.overrideProps.length; f++) {
                      var overrideProps = actual.overrideProps[f];
                      scope.modify[overrideProps.label] = overrideProps.value;
                      scope.field[overrideProps.label] = overrideProps.value;
                    }
                  }
                } else {
                  allTrue = false;
                  scope.model[scope.field.propertyId] = null;
                  break; //TODO: check this break
                }
              }
              if (allTrue) {
                if (scope.deleteClass) {
                  element.addClass('c-col');
                }
                return true;
              }
            }
            if (scope.deleteClass) {
              element.removeClass('c-col');
            }
            return false;
          }
          if (scope.deleteClass) {
            element.addClass('c-col');
          }
          return true;
        };

        scope.isRequired = function () {
          if (scope.field.required != undefined) {
            try{
              return eval(scope.field.required);
            }
          catch(error){
            return false;
          }
          } else {
            return false;
          }
        }
      });

      function getMimeType() {
        var splited = scope.field.propertyType == 'file' ? scope.field.propertyName.split(' ') : null;
        var typeFile = splited ? splited[0].toLowerCase() : null;
        var accept;
        switch (typeFile) {
          case 'csv':
            accept = '.csv';
            break;
          case 'json':
            accept = '.json';
            break;
          case 'excel':
            accept = '.xls, .xlsx';
            break;
          case 'xml':
            accept = '.xml';
            break;
          default:
            accept = '*';
            break;
        }
        return accept;
      }
    }

  }
})();
