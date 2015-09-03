'use strict';

/*BUTTON TO CHANGE ROUTE OR URL*/

angular
  .module('webApp')
  .directive('routeButton', routeButton);

routeButton.$inject = ['$state', '$location'];

function routeButton($state, $location) {
  return {
    restrict: 'E',
    scope: {
      class: '=buttonClass',
      url: '=url',
      route: '=route',
      text: '=text',
      params: '=params'
    },
    replace: "true",
    template: '	<button class="c-button c-button--call-to-action-1" ng-click = "changePage()">' +
    '<span class="icon icon-circle-plus"></span>' +
    '<span> {{text}} </span> </button>',
    link: function (scope) {
      scope.changePage = function () {
        if (scope.params == undefined) scope.params = {};
        if (scope.route) {
          $state.go(scope.route, scope.params)
        } else if (scope.url) {
          $location.path(scope.url).search(scope.params);
        }
      }
    }
  }
};
