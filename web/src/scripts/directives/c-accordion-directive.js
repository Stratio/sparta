(function () {
  'use strict';

  /* Accordion directive */

  angular
    .module('webApp')
    .directive('cAccordion', cAccordion);

  cAccordion.$inject = ['AccordionStatusService'];

  function cAccordion(AccordionStatusService) {
    var index = 0;

    var directive = {
      restrict: 'E',
      scope: {
        items: "=",
        itemTemplate: "=",
        onChangeOpenedElement: "&",
        itemQaTag: "=",
        showNewItemPanel: "=",
        accordionStatus:"=",
        itemAttributeInHeader:"@",
        newItemHeader: "@",
        additionalModifierClass:"@"
      },
      replace: "true",
      templateUrl: 'templates/components/c-accordion.tpl.html',
      link: link
    };

    return directive;

    function link(scope) {
      AccordionStatusService.resetAccordionStatus(scope.accordionStatus, scope.items.length, scope.items.length);

      scope.generateIndex = function () {
        return index++;
      };

      scope.onClickItem = function () {
        scope.onChangeOpenedElement();
      };

      scope.closeAll = function(){

      };

      scope.$watchCollection(
        "items",
        function (newItems, oldItems) {
          // reset if item is added to items
          if (newItems.length != oldItems.length) {
            AccordionStatusService.resetAccordionStatus(scope.accordionStatus, scope.items.length);
          }
        });

      scope.$watchCollection(
        "accordionStatus",
        function () {

          if (scope.accordionStatus) {
            var selectedItemPosition = scope.accordionStatus.indexOf(true);
            scope.onChangeOpenedElement({selectedItemPosition:selectedItemPosition});
          }
        });
    }
  }
})();
