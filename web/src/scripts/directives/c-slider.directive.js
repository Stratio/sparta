'use strict';

/*BUTTON TO CHANGE ROUTE OR URL*/

angular
  .module('webApp')
  .directive('cSlider', cSlider);


function cSlider() {
  return {
    restrict: 'E',
    scope: {
      minText: "=minText",
      maxText: "=maxText",
      minvalue: "=minValue",
      maxvalue: "=maxValue",
      steps: "=steps",
      value: "=value"
    },
    replace: "true",
    templateUrl: 'templates/components/c-slider.tpl.html',
  }
};
