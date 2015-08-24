(function() {
    'use strict';

    angular.module('webApp')
    .directive("tooltip", function () {
      return {
        link: function (scope, element, attrs) {

            $(element).on("mouseover", function () {
                $(this).append("<span>"+ attrs.tooltip +"</span>");
            });

            $(element).on("mouseout", function () {
                $(this).find("span").remove();
            });

            scope.$on("$destroy", function () {
                $(element).off("mouseover");
                $(element).off("mouseout");
            });
        }
      };
    });

})();