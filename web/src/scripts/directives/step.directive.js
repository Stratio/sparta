'use strict';

/*STEP DIRECTIVE*/
angular
  .module('webApp')
  .directive('step', step);

function step() {
  return {
    restrict: 'E',
    scope: {
      index: '=index',
      name: '=name',
      icon: '=icon',
      current: '=currentStep'
    },
    templateUrl: 'templates/components/step.html',

    link: function (scope, elem, attr) {
      scope.isSelected = function () {
        return scope.index == scope.current;
    };
    }
  };


}
