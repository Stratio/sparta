(function () {
  'use strict';

  angular
    .module('webApp')
    .service('AccordionStatusService', AccordionStatusService);


  function AccordionStatusService() {
    var vm = this;
    vm.accordionStatus = [];

    vm.ResetAccordionStatus = ResetAccordionStatus;
    vm.GetAccordionStatus = GetAccordionStatus;

    function ResetAccordionStatus(length, truePosition) {
      for (var i = 0; i < length; ++i) {
        vm.accordionStatus[i] = false;
      }
      vm.accordionStatus[truePosition] = true;
    }

    function GetAccordionStatus() {
      return vm.accordionStatus;
    }
  }
})();
