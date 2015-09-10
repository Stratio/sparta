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
    template: '	<button class="{{class}}">' +
                 '<span class="icon icon-circle-plus"></span>' +
                  '<span> {{text}} </span> ' +
              '</button>',
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
