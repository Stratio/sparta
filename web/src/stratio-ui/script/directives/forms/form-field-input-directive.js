(function () {
    'use strict';

    angular
        .module('webApp')
        .directive('formFieldInput', formFieldInput);

    formFieldInput.$inject = ['$document'];
    function formFieldInput($document) {
        var directive = {
            link: link,
            templateUrl: 'stratio-ui/template/form/form_field_input.html',
            restrict: 'AE',
            replace: true,
            scope: {
                customError: '@',
                help: '@',
                label: '@',
                name: '@stName',
                placeholder: '@',
                type: '@',
                autofocus: '=',
                form: '=',
                match: '=',
                maxlength: '=',
                minlength: '=',
                max: '=',
                min: '=',
                step: '=',
                stTrim: '=',
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

            scope.$watch('customError', function(newValue, oldValue) {
                if (newValue) {
                    scope[scope.name][scope.name].$setValidity('custom', false);
                } else if (oldValue !== newValue) {
                    scope[scope.name][scope.name].$setValidity('custom', true);
                }
            });

            scope.$watch('model', function(newValue, oldValue){
                    scope.customError = '';
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
