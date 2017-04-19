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

  /*STEP DIRECTIVE*/
  angular
      .module('webApp')
      .directive('cStep', step);

  function step() {
    return {
      restrict: 'E',
      scope: {
        index: '=index',
        order: '=order',
        name: '=name',
        icon: '=icon',
        current: '=currentStep',
        isAlternative: '=isAlternative',
        isAvailable: '=',
        hasBeenVisited: "=",
        auxStep: "=",
        stepAuxOrder: "="
      },
      replace: 'true',
      templateUrl: 'templates/components/c-step.tpl.html',
      link: function(scope) {

        var auxIndex = scope.stepAuxOrder || scope.index;

        scope.isSelected = function() {
          return scope.index == scope.current;
        };

        scope.isVisited = function() {
          return (scope.index < scope.current || (scope.hasBeenVisited && !scope.isSelected() ));
        };

        scope.isEnabled = function() {
          return (auxIndex == scope.current + 1 || scope.order <= scope.current + 1) && scope.isAvailable && !scope.isSelected();
        };
      }
    };
  }
})();
