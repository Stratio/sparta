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
    link: function(scope){
      scope.chooseStep = function (index) {
        scope.current = index;
        scope.nextStepAvailable = false;
      };
    }
  };
}
})();
