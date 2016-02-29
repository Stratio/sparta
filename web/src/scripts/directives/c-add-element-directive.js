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
        submittedForm: '=',
        label: '=',
        help: '=',
        limit: '=',
        isObject: "=",
        attribute: "@",
        required:"="
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
          if (scope.limit === 0 || scope.model.length < scope.limit) {
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

              if (scope.isObject) {
                var objectToAdd = {};
                objectToAdd[scope.attribute] =  scope.inputToAdd;
                scope.model.push(objectToAdd);
              } else
                scope.model.push(scope.inputToAdd);
              scope.inputToAdd = '';
            }
            event.preventDefault();
          }
          else {
            scope.submittedForm = true;
            scope.errorLimit = true;
          }
        }
      }
    }
  }
})();
