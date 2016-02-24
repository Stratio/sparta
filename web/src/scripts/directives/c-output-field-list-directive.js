(function () {
  'use strict';

  /*LINE WITH A FORM CONTROL AND A LIST OF INPUT FIELDS*/

  angular
    .module('webApp')
    .directive('cOutputFieldList', cOutputFieldList);


  function cOutputFieldList() {
    var directive = {
      restrict: 'E',
      scope: {
        label: '=',
        editionMode: '=',
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
        outputTypes: "=",
        qa: "@",
        help: '@',
        helpQa: '@'
      },
      replace: "true",
      templateUrl: 'templates/components/c-output-field-list.tpl.html',
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
