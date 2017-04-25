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
        parentClass: "=",
        validationFn: "=",
        policy: '=',
        input:'='
      },
      replace: 'true',
      templateUrl: 'templates/components/c-step-progress-bar.tpl.html',

      link: function (scope) {
        scope.visited = [];
        scope.showHelp = true;
        scope.structuredSteps = transformSteps(scope.steps);
        scope.hideHelp = function () {
          scope.showHelp = false;
        };
        scope.chooseStep = function (index, order) {
          var transformations = scope.policy.transformations.transformationsPipe;
          var input = scope.policy.input;


          if(order == 2 && (!input || !input.name)){
            return;
          }

          //triggers and cubes validation
          if (order > 2 && (!transformations || !transformations.length)) {
            if (scope.current == 2 && index != 3) {
               scope.onClickNextStep();
            }
            return;
          }

          if (scope.current == 0) {
            return scope.validationFn(index);
          }

          if (index < scope.current) {
            scope.current = index;
            return;
          }

          scope.current = index;
          scope.showHelp = true;
        };

        scope.thereAreAlternativeSteps = function (step) {
          return step.subSteps != undefined;
        };

        scope.showCurrentStepMessage = function () {
          return !scope.nextStepAvailable && !scope.visited[scope.current + 1] || scope.current == scope.steps.length - 1;
        };

        scope.$watchCollection(
          "nextStepAvailable",
          function (nextStepAvailable) {
            if (nextStepAvailable) {
              scope.showHelp = true;
            }
          });

        function transformSteps() {
          var transformedSteps = [];
          if (scope.steps) {
            for (var i = 0; i < scope.steps.length; ++i) {
              var step = scope.steps[i];
              if (!transformedSteps[step.order] && !step.isSubStep) {
                if (step.isAuxStep) {
                  transformedSteps[step.isAuxStep].auxStep = step;
                } else {
                  transformedSteps[step.order] = step;

                }
              } else {
                if (!transformedSteps[step.order]) {
                  transformedSteps[step.order] = {};
                }
                if (!transformedSteps[step.order].subSteps) {
                  transformedSteps[step.order].subSteps = [];
                }
                transformedSteps[step.order].subSteps.push(step);
              }
            }
          }
          return transformedSteps;
        }
      }

    };
  }
})();
