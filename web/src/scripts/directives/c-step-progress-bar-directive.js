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

  /*STEP DIRECTIVE*/
  angular
    .module('webApp')
    .directive('cStepProgressBar', stepsComponent);

  function stepsComponent() {

    return {
      restrict: 'E',
      scope: {
        steps: '=steps',
        current: '=currentStep',
        nextStepAvailable: '=',
        editionMode: "=",
        onClickNextStep: "&",
        parentClass:"="
      },
      replace: 'true',
      templateUrl: 'templates/components/c-step-progress-bar.tpl.html',

      link: function (scope) {
        scope.visited = [];
        scope.showHelp = true;
        scope.hideHelp = function () {
          scope.showHelp = false;
        };
        scope.chooseStep = function (index) {
          if ((scope.editionMode && scope.nextStepAvailable) || (index == scope.current + 1 && scope.nextStepAvailable) || (index < scope.current)) {
            scope.visited[scope.current] = true;
            scope.current = index;
          }else{
            if (index == scope.current + 1){
              scope.onClickNextStep();
            }
          }
          scope.showHelp = true;
        };
        
        scope.thereAreAlternativeSteps = function (step) {
          return step.subSteps != undefined;
        };

        scope.showCurrentStepMessage = function(){
          return !scope.nextStepAvailable && !scope.visited[scope.current+1] || scope.current == scope.steps.length -1;
        };

        scope.$watchCollection(
          "nextStepAvailable",
          function (nextStepAvailable) {
            if (nextStepAvailable) {
              scope.showHelp = true;
            }
          });
      }
    };
  }
})();
