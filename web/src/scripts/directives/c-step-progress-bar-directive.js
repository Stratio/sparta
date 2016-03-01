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
        nextStepAvailable: '='
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
          if ((index == scope.current + 1 && scope.nextStepAvailable) || (index < scope.current) || scope.visited[index]) {
            scope.visited[scope.current] = true;
            scope.current = index;
            scope.nextStepAvailable = false;
          }
          scope.showHelp = true;
        };

        scope.$watchCollection(
          "nextStepAvailable",
          function (nextStepAvailable) {
              if (nextStepAvailable){
                scope.showHelp = true;
              }
          });
      }
    };
  }
})();
