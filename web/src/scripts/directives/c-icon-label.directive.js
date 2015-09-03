'use strict';

/*BUTTON TO CHANGE ROUTE OR URL*/

angular
  .module('webApp')
  .directive('cIconLabel', cIconLabel);


function cIconLabel() {
  return {
    restrict: 'E',
    scope: {
      iconClass: "=iconClass",
      text: "=text",
      textClass: "=textClass"
    },
    replace: "true",
    templateUrl: 'templates/components/c-icon-label.tpl.html'
  }
};
