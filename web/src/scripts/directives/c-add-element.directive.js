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
        type: "=type"
      },
      replace: "true",
      templateUrl: 'templates/components/c-add-element.tpl.html',
      link: link
    };

    return directive;

    function link(scope) {
      scope.showInputField = showInputField;
      scope.hideInputField = hideInputField;
      scope.addInput = addInput;

      function showInputField() {
        scope.showInput = true;
      }

      function hideInputField() {
        scope.showInput = false;
      }

      function addInput(event, element) {
        if (event.keyCode == '13') {
          scope.model.push(element.inputToAdd);
          scope.hideInputField();
        }
      }

    }


  };
})();

