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
        labelControlText: "@",
        labelControlClass: "=",
        inputControlClass: "=",
        wrapperControlClass: "=",
        rightTextClass: "=",
        placeholder: '=',
        inputText: "=",
        rightText: "=",
        value: "@",
        model: "=",
        inputType: "=",
        pattern: "=",
        required: "="
      },
      replace: "true",
      templateUrl: 'templates/components/c-input-field.tpl.html'
    }
  };
})();
