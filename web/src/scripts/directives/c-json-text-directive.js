(function () {
  'use strict';

  /*DIRECTIVE TO PASS STRING TO JSON*/

  angular
    .module('webApp')
    .directive('jsonText', function () {
      return {
        restrict: 'A', // only activate on element attribute
        require: 'ngModel', // get a hold of NgModelController
        link: function (scope, element, attrs, ngModelCtrl) {
          var lastValid;
          // push() if faster than unshift(), and avail. in IE8 and earlier (unshift isn't)
          ngModelCtrl.$parsers.push(fromUser);
          ngModelCtrl.$formatters.push(toUser);

          function fromUser(text) {
            // Beware: trim() is not available in old browsers
            if (!text || text.trim() === '') {
              if (scope.required == false) {
                ngModelCtrl.$setValidity('invalidJson', true);
              }
              return {};
            } else {
              try {
                lastValid = angular.fromJson(text);
                ngModelCtrl.$setValidity('invalidJson', true);
              } catch (e) {
                ngModelCtrl.$setValidity('invalidJson', false);
              }
              return lastValid;
            }
          }

          function toUser(object) {
            // better than JSON.stringify(), because it formats + filters $$hashKey etc.
            return angular.toJson(object, true);
          }
        }
      };
    });

})();
