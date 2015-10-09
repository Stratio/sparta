(function () {
'use strict';

/*LINE WITH LABEL AND ICON*/

angular
  .module('webApp')
  .directive('cIconLink', cIconLink);


function cIconLink() {
  return {
    restrict: 'E',
    scope: {
      iconClass: "=iconClass",
      text: "=text",
      textClass: "=textClass",
      linkUrl:"=linkUrl",
      linkClass: "=linkClass",
      qa: "=qa"
    },
    replace: true,
    templateUrl: 'templates/components/c-icon-link.tpl.html'
  }
};
})();

