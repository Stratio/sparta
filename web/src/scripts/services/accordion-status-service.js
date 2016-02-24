(function () {
  'use strict';

  angular
    .module('webApp')
    .service('AccordionStatusService', AccordionStatusService);


  function AccordionStatusService() {
    var vm = this;

    vm.resetAccordionStatus = resetAccordionStatus;

    function resetAccordionStatus(accordionStatus, length, truePosition) {
      if (length !== undefined && length != null && length >= 0) {
        for (var i = 0; i <= length; ++i) {
          if (i == truePosition)
            accordionStatus[i] = true;
          else
            accordionStatus[i] = false;
        }

        var currentLength = accordionStatus.length;
        while(accordionStatus.length > length+1){
          accordionStatus.pop();
          currentLength--;
        }
      }
      return accordionStatus;
    }
  }
})();
