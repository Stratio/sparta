(function () {
  'use strict';

  /*ICON RESPONSIBLE TO ADD A NEW ELEMENT TO THE INTRODUCED MODEL*/

  angular
    .module('webApp')
    .directive('cAddElement', cAddElement);


  function cAddElement() {
    var directive = {
      restrict: 'E',
      scope: {
        iconClass: "=iconClass",
        model: "=model",
        type: "=type",
        disabled: "=disabled",
        qa: "@",
        pattern: "=",
        placeholder: "@",
        error: '=',
        elementsLength: '=',
        submittedForm: '=',
        label: '=',
        help: '=',
        limit: '='
      },
      replace: "true",
      templateUrl: 'templates/components/c-add-element.tpl.html',
      link: link
    };

    return directive;

    function link(scope) {
      scope.addInput = addInput;
      scope.errorLimit = false;

      function addInput(event) {
        if (scope.inputToAdd !== '' && scope.inputToAdd !== undefined && (event.keyCode == '13' || event.type === "click")) {
          var inputExists = false;
          scope.errorLimit = false;
          if (scope.limit === 0 || scope.elementsLength < scope.limit) {
            for (var i = 0; i < scope.model.length; i++) {
              if (scope.model[i].name === scope.inputToAdd) {
                inputExists = true;
              }
            }
            if (inputExists) {
              scope.submittedForm = true;
              scope.error = true;
            }
            else {
              scope.submittedForm = false;
              scope.error = false;
              scope.model.push({name:scope.inputToAdd});
              scope.inputToAdd = '';
            }
            event.preventDefault();
          }
          else {
            scope.errorLimit = true;
          }
        }
      }
    }
  }
})();
