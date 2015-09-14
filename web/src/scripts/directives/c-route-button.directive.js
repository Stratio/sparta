(function () {
'use strict';

/*BUTTON TO CHANGE ROUTE OR URL*/

angular
  .module('webApp')
  .directive('cRouteButton', cRouteButton);

cRouteButton.$inject = ['$state', '$location'];

function cRouteButton($state, $location) {
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
    templateUrl: "templates/components/c-route-button.tpl.html",
    link: function (scope, element) {
      element.bind('click',function () {
        if (scope.params == undefined) scope.params = {};
        if (scope.route) {
          $state.go(scope.route, scope.params)
        } else if (scope.url) {
          $location.path(scope.url).search(scope.params);
        }
      })
    }
  }
};
})();
