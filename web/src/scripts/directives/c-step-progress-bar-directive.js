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
        scope.chooseStep = function (index) {
          if ((index == scope.current + 1 && scope.nextStepAvailable) || (index < scope.current) || visited[index]) {
            visited[scope.current] = true;
            scope.current = index;
            scope.nextStepAvailable = false;
          }
        };
      }
    };
  }
})();
