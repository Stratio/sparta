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
      linkUrl:"",
      linkClass: "=linkClass"
    },
    replace: "true",
    templateUrl: 'templates/components/c-icon-link.tpl.html'
  }
};
})();

