'use strict';

/*STEP DIRECTIVE*/
angular
  .module('webApp')
  .directive('stepsComponent', stepsComponent);

function stepsComponent() {
  return {
    restrict: 'E',
    scope: {
      steps: '=steps',
      current: '=currentStep'
    },
    templateUrl: 'templates/components/steps-component.html',
    link: function(scope){
      scope.chooseStep = function (index) {
        scope.current = index;
      };
    }
  };
};
