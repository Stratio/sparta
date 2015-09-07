(function () {
  'use strict';

  /*LINE WITH A FORM CONTROL AND A LIST OF INPUT FIELDS*/

  angular
    .module('webApp')
    .directive('cInputListField', cInputListField);


  function cInputListField() {
    return {
      restrict: 'E',
      scope: {
        labelControlText: "=labelControlText",
        labelControlClass: '=labelControlClass',
        inputControlClass: '=inputControlClass',
        formControlClass: "=formControlClass",
        placeholder: '=placeholder',
        inputText: "=inputText",
        rightText: "=rightText",
        model: "=model",
        inputType: "=inputType",
        pattern: "=pattern",
        inputs: "=inputs",
        readonly: "=readonly"
      },
      replace: "true",
      templateUrl: 'templates/components/c-input-list-field.tpl.html'
    }
  };
})();
