(function () {
    'use strict';

    angular
        .module('webApp')
        .directive('formFieldList', formFieldList);

    formFieldList.$inject = ['$document'];
    function formFieldList($document) {
        var directive = {
            link: link,
            templateUrl: 'stratio-ui/template/form/form_field_list.html',
            restrict: 'AE',
            replace: true,
            scope: {
                name: '@stName',
                field: '=',
                form: '=',
                model: '='
            }
        }
        return directive;

        function link(scope, element, attrs) {
            scope.name = "";
            scope.showHelp = false;

            scope.addItem = function() {
                var item = {};

                for (var i=0; i<scope.field.fields.length; i++) {
                    var value = scope.field.fields[i].propertyType == "number" ? Number(scope.field.fields[i].default) : scope.field.fields[i].default;
                    item[scope.field.fields[i].propertyId] = value;
                }
                scope.model[scope.field.propertyId].push(item);
            };

            scope.removeItem = function(index) {
                scope.model[scope.field.propertyId].splice(index, 1);
            };

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
