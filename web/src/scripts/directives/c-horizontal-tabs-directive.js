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

  /* Accordion directive */

  angular
    .module('webApp')
    .directive('cHorizontalTabs', cHorizontalTabs);

  cHorizontalTabs.$inject = [];

  function cHorizontalTabs() {

    var directive = {
      restrict: 'E',
      scope: {
        activeOption: "=",
        options: "="
      },
      replace: "true",
      templateUrl: 'templates/components/c-horizontal-tabs.tpl.html',
      link: link
    };

    return directive;

    function link(scope) {
      scope.activateOption = function (option) {
        if (option.isDisabled) {
          return;
        }
        if (scope.activeOption !== option.name) {
          scope.activeOption = option.name;
          scope.$emit("newTabOptionValue", scope.activeOption);
        }

      }
    }
  }
})();
