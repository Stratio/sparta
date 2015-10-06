(function () {
  'use strict';

  angular
    .module('webApp')
    .service('AccordionStatusService', AccordionStatusService);


  function AccordionStatusService() {
    var vm = this;
    var accordionStatus = [];

    vm.resetAccordionStatus = resetAccordionStatus;
    vm.getAccordionStatus = getAccordionStatus;

    function resetAccordionStatus(length, truePosition) {
      if (length !== undefined && length != null && length >= 0) {
        for (var i = 0; i <= length; ++i) {
          if (i == truePosition)
            accordionStatus[i] = true;
          else
            accordionStatus[i] = false;
        }
        if (truePosition == undefined) {
          accordionStatus[length] = true;
        }
      }
    }

    function getAccordionStatus() {
      return accordionStatus;
    }
  }
})();
