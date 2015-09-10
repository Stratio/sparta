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
      scope.addInput = addInput;

      function addInput(event) {
        if (scope.inputToAdd !== '' && scope.inputToAdd !== undefined && (event.keyCode == '13' || event.type === "click")) {
          scope.model.push(scope.inputToAdd);
          scope.inputToAdd = '';
          event.preventDefault();
        }
      }
    }
  };
})();

