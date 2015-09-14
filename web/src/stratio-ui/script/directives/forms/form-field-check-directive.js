(function () {
    'use strict';

    angular
        .module('webApp')
        .directive('formFieldCheck', formFieldCheck);

    formFieldCheck.$inject = ['$document'];
    function formFieldCheck($document) {
        var directive = {
            link: link,
            templateUrl: 'stratio-ui/template/form/form_field_check.html',
            restrict: 'AE',
            replace: true,
            scope: {
                help: '@',
                label: '@',
                name: '@stName',
                type: '@',
                autofocus: '=',
                form: '=',
                model: '=',
                pattern: '=',
                required: '=',
                disabled: '=',
                qa: '@'
            }
        };
        return directive;

        function link(scope, element, attrs) {
            scope.customError = "";
            scope.help = "";
            scope.label = "";
            scope.name = "";
            scope.placeholder = "";
            scope.type = "text";

            scope.isFocused = false;
            scope.showHelp = false;

            scope.$watch('autofocus', function(newValue, oldValue) {
                if (newValue) {
                    var tags = element.find('input');
                    if (tags.length>0) {
                        tags[0].focus();
                    }
                }
            });

            scope.toggleHelp = function(event) {
                if (scope.showHelp) {
                    scope.showHelp = false;
                    $document.unbind('click', externalClickHandler);
                } else {
                    scope.showHelp = true;
                    $document.bind('click', externalClickHandler);
                }
            };

            function externalClickHandler(event) {
                if (event.target.id == "help-"+scope.name)
                    return;
                $document.unbind('click', externalClickHandler);
                scope.showHelp = false;
                scope.$apply();
            }
        }
    }
})();
