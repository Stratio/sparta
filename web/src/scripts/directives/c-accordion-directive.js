/*
 * Copyright (C) 2015 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
        itemQaTag: "@",
        hideElementFromModel: "=",
        showNewItemPanel: "=",
        accordionStatus: "=",
        itemAttributeInHeader: "@",
        newItemHeader: "@",
        additionalModifierClass: "@",
        width: '@'
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

      scope.$watchCollection(
        "items",
        function (newItems, oldItems) {
          // reset if item is added to items
          if (newItems.length != oldItems.length || newItems != oldItems) {
            AccordionStatusService.resetAccordionStatus(scope.accordionStatus, scope.items.length);
          }
        });

      scope.$watchCollection(
        "accordionStatus",
        function () {
          if (scope.accordionStatus) {
            var selectedItemPosition = scope.accordionStatus.indexOf(true);
            scope.onChangeOpenedElement({selectedItemPosition: selectedItemPosition});
          }
        });

    }
  }
})();
