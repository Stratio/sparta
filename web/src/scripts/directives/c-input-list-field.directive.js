(function () {
  'use strict';

  /*LINE WITH A FORM CONTROL AND A LIST OF INPUT FIELDS*/

  angular
    .module('webApp')
    .directive('cInputListField', cInputListField);


  function cInputListField() {
    var directive = {
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
        readonly: "=readonly",
        enableDelete: "=enableDelete"
      },
      replace: "true",
      templateUrl: 'templates/components/c-input-list-field.tpl.html',
      link: link
    };

    return directive;

    function link(scope) {
      scope.deleteInput = deleteInput;

      function deleteInput(index) {
        scope.inputs.splice(index,1);
      };
    }
  };
})();
