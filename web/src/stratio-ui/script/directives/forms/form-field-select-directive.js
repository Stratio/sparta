(function () {
  'use strict';

  angular
    .module('webApp')
    .directive('formFieldSelect', formFieldSelect);

  formFieldSelect.$inject = ['$document', '$timeout'];
  function formFieldSelect($document, $timeout) {
    var directive = {
      link: link,
      templateUrl: 'stratio-ui/template/form/form_field_select.html',
      restrict: 'AE',
      replace: true,
      scope: {
        field: '@',
        help: '@',
        label: '@',
        name: '@stName',
        placeholder: '@',
        trackBy: '@',
        selectClass: '@',
        autofocus: '=',
        defaultValue: '=',
        form: '=',
        model: '=',
        options: '=',
        required: '=',
        disabled: '=',
        changeAction: '&',
        qa: '@'
      }
    };
    return directive;

    function link(scope, element, attrs) {
      scope.help = "";
      scope.label = "";
      scope.name = "";
      scope.placeholder = "";

      scope.isFocused = false;
      scope.showHelp = false;

      if (scope.defaultValue) {
        scope.model = scope.defaultValue;
      }

      scope.$watch('autofocus', function (newValue, oldValue) {
        if (newValue) {
          var tags = element.find('input');
          if (tags.length > 0) {
            tags[0].focus();
          }
        }
      });

      scope.changeActionHandler = function () {
        $timeout(function () {
          scope.changeAction();
        }, 0);
      };

      scope.keyupHandler = function ($event) {
        if (scope.model) {
          $timeout(function () {
            angular.element($event.target).triggerHandler('change');
          }, 0, false);
        }
      };

      scope.toggleHelp = function (event) {
        if (scope.showHelp) {
          scope.showHelp = false;
          $document.unbind('click', externalClickHandler);
        } else {
          scope.showHelp = true;
          $document.bind('click', externalClickHandler);
        }
      };

      function externalClickHandler(event) {
        if (event.target.id == "help-" + scope.name)
          return;
        $document.unbind('click', externalClickHandler);
        scope.showHelp = false;
        scope.$apply();
      }
    }
  }
})();
