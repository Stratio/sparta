'use strict';

/*BUTTON TO CHANGE ROUTE OR URL*/

angular
  .module('webApp')
  .directive('cInputField', cInputField);


function cInputField() {
  return {
    restrict: 'E',
    scope: {
      formControlText: "=formControlText",
      formControlClass: "=formControlClass",
      inputText: "=inputText",
      rightText: "=rightText"
    },
    replace: "true",
    templateUrl: 'templates/components/c-input-field.tpl.html'
  }
};
