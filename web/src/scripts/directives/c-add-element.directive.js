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
        placeholder: "@"
      },
      replace: "true",
      templateUrl: 'templates/components/c-add-element.tpl.html',
      link: link
    };

    return directive;

    function link(scope) {
      scope.addInput = addInput;
      scope.error = false;

      function addInput(event) {
        if (scope.inputToAdd !== '' && scope.inputToAdd !== undefined && (event.keyCode == '13' || event.type === "click")) {
          var inputExists = false;
          for (var i = 0; i < scope.model.length; i++) {
            if (scope.model[i] === scope.inputToAdd) {
              inputExists = true;
            }
          }

          if (inputExists) {
            scope.error = true;
          }
          else {
            scope.error = false;
            scope.model.push(scope.inputToAdd);
            scope.inputToAdd = '';
          }
          event.preventDefault();
        }
      }
    }
  };
})();
