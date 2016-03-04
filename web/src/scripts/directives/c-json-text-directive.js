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

  /*DIRECTIVE TO PASS STRING TO JSON*/

  angular
    .module('webApp')
    .directive('jsonText', function () {
      return {
        restrict: 'A', // only activate on element attribute
        require: 'ngModel', // get a hold of NgModelController
        link: function (scope, element, attrs, ngModelCtrl) {
          var lastValid;
          // push() if faster than unshift(), and avail. in IE8 and earlier (unshift isn't)
          ngModelCtrl.$parsers.push(fromUser);
          ngModelCtrl.$formatters.push(toUser);

          function fromUser(text) {
            // Beware: trim() is not available in old browsers
            if (!text || text.trim() === '') {
              if (scope.required == false) {
                ngModelCtrl.$setValidity('invalidJson', true);
              }
              return {};
            } else {
              try {
                lastValid = angular.fromJson(text);
                ngModelCtrl.$setValidity('invalidJson', true);
              } catch (e) {
                ngModelCtrl.$setValidity('invalidJson', false);
              }
              return lastValid;
            }
          }

          function toUser(object) {
            // better than JSON.stringify(), because it formats + filters $$hashKey etc.
            return angular.toJson(object, true);
          }
        }
      };
    });

})();
