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
      name: '=name',
      icon: '=icon',
      current: '=currentStep'
    },
    replace: 'true',
    templateUrl: 'templates/components/c-step.tpl.html',
    link: function (scope, elem, attr) {
      scope.isSelected = function () {
        return scope.index == scope.current;
      };
    }
  };


}
