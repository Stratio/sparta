(function () {
  'use strict';

  /*DIRECTIVE TO PASS STRING TO JSON*/

  angular
    .module('webApp')
    .directive('jsonText', function() {
      return {
        restrict: 'A', // only activate on element attribute
        require: 'ngModel', // get a hold of NgModelController
        link: function(scope, element, attrs, ngModelCtrl) {

          var lastValid;

          // push() if faster than unshift(), and avail. in IE8 and earlier (unshift isn't)
          ngModelCtrl.$parsers.push(fromUser);
          ngModelCtrl.$formatters.push(toUser);

          // clear any invalid changes on blur
          element.bind('blur', function() {
            try{
              var newValue = angular.fromJson(element[0].value);
              lastValid = lastValid || newValue;

              ngModelCtrl.$setViewValue(toUser(newValue));

              // TODO avoid this causing the focus of the input to be lost..
              ngModelCtrl.$render();
            }
           catch (e)
          {
          }

          });

          function fromUser(text) {
            // Beware: trim() is not available in old browsers

            if (!text || text.trim() === '') {
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
