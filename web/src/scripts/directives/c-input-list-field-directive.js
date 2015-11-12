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
        labelControlText: "=",
        labelControlClass: '=',
        inputControlClass: '=',
        formControlClass: "=",
        placeholder: '=',
        inputText: "=",
        rightText: "=",
        model: "=",
        inputType: "=",
        pattern: "=",
        inputs: "=",
        readonly: "=",
        enableDelete: "=",
        required: "=",
        qa: "@",
        help: '@',
        helpQa: '@'
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
      }
    }
  }
})();
