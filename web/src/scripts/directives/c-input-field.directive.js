(function () {
'use strict';

/*LINE WITH A FORM CONTROL AND INPUT FIELD*/

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
      rightText: "=rightText",
      value: "=value",
      model: "=model",
      inputType: "=inputType",
      pattern: "=pattern"
    },
    replace: "true",
    templateUrl: 'templates/components/c-input-field.tpl.html'
  }
};
})();
