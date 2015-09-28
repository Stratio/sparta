(function () {
  'use strict';

  /*DELETE INPUT MODALS CONTROLLER */
  angular
    .module('webApp')
    .controller('ConfirmModalCtrl', ConfirmModalCtrl);

  ConfirmModalCtrl.$inject = ['$modalInstance', 'title', 'message'];

  function ConfirmModalCtrl($modalInstance, title, message) {
    /*jshint validthis: true*/
    var vm = this;

    vm.ok = ok;
    vm.cancel = cancel;
    vm.title = title;
    vm.message = message;

    ///////////////////////////////////////

    function ok() {
      $modalInstance.close();
    };

    function cancel() {
      $modalInstance.dismiss('cancel');
    };
  };

})();
