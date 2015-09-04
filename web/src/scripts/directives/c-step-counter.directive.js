(function () {
'use strict';

/*STEP DIRECTIVE*/
angular
  .module('webApp')
  .directive('cStepCounter', stepsComponent);

function stepsComponent() {
  return {
    restrict: 'E',
    scope: {
      steps: '=steps',
      current: '=currentStep'
    },
    replace: 'true',
    templateUrl: 'templates/components/c-step-counter.tpl.html',
    link: function(scope){
      scope.chooseStep = function (index) {
        scope.current = index;
      };
    }
  };
};
})();
