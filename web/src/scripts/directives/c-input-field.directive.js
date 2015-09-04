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
      labelControlText: "=labelControlText",
      labelControlClass: '=labelControlClass',
      inputControlClass: '=inputControlClass',
      formControlClass: "=formControlClass",
      rightTextClass: "=rightTextClass",
      placeholder: '=placeholder',
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
