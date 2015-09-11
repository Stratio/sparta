(function () {
    'use strict';

    angular
        .module('webApp')
        .directive('stModal', stModal);

    stModal.$inject = ['$rootScope'];

    function stModal($rootScope) {
        var directive = {
            link: link,
            replace: true,
            restrict: 'AE',
            templateUrl: 'templates/tools/st-modal.tpl.html',
            transclude: true,
            scope: {
                actionLabel: '@',
                actionTitle: '@',
                secondaryActionLabel: '@',
                adjustToContent: '=',
                hasNext: '=',
                open: '=',
                showIcon: '=',
                showFooter: '=',
                startLoading: '=',
                action: '&',
                actionCancel: '&',
                secondaryAction: '&'
            }
        };
        return directive;

        function link(scope, element, attrs) {
            var actionClick = false;
            scope.isLoading = false;
            scope.actionLabel = "continue";
            scope.actionTitle = "Confirmation required";
            scope.$watch('open', openHandler);

            scope.actionHandler = function() {
                hideModalbroadcast();
                scope.isLoading = true;
                scope.action();
                actionClick = true;
            };

            scope.secondaryActionHandler = function() {
                hideModalbroadcast();
                scope.isLoading = true;
                scope.secondaryAction();
            };

            scope.actionCancelHandler = function() {
                hideModalbroadcast();
                scope.isLoading = false;
                scope.open = false;
                scope.actionCancel();
            };

            scope.showLoading = function(){
              if(scope.startLoading === undefined){
                return scope.isLoading;
              } else {
                return scope.isLoading && scope.startLoading;
              }
            };

            scope.deletepolicies = function() {
                scope.open = false;
            };

            function openHandler(newValue, oldValue) {
                if (!newValue && !oldValue)
                    return;
                if((scope.hasNext === undefined) || (scope.hasNext && !actionClick)){
                  $rootScope.$broadcast('ST_MODAL_VIEW', newValue);
                }
                actionClick = false;
                scope.isLoading = false;
            }
            function hideModalbroadcast(){
                 $rootScope.$broadcast('ST_MODAL_VIEW', false);
            }
        }
    }

})();
