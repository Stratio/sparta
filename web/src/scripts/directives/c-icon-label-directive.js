(function () {
'use strict';

/*LINE WITH LABEL AND ICON*/

angular
  .module('webApp')
  .directive('cIconLabel', cIconLabel);


function cIconLabel() {
  return {
    restrict: 'E',
    scope: {
      wrapperControlClass: "=",
      iconClass: "=",
      text: "=",
      textClass: "=",
      iconUrl: "=",
      test: "@",
      required : "@",
      help: "=",
      helpQa: "="
    },
    replace: true,
    templateUrl: 'templates/components/c-icon-label.tpl.html'
  }
};
})();

