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
(function (angular) {
  'use strict';

  angular
    .module('webApp').directive('cInputFile', ['$parse', function ($parse) {
      return {
        restrict: 'E',
        replace: true,
        scope: {
          fileModel: '=',
          name: '=',
          isRequired: '=',
          form: '='
        },
        link: function (scope, element, attrs) {
          scope.invalid = true;
          var input = element.find("input");
          scope.click = function () {
            input.trigger('click');
          };

          input.bind('change', function () {
            scope.invalid = false;
            scope.$apply(function () {
              scope.fileName = input[0].files[0].name;
              scope.fileModel = input[0].files[0];
            });
          });
        },
        templateUrl: 'templates/components/c-input-file.tpl.html'
      };
    }])
})(window.angular);
