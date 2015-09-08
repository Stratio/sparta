(function() {
  'use strict';

    /*DELETE INPUT MODALS CONTROLLER */
    angular
        .module('webApp')
        .controller('ConfirmPolicyModalCtrl', ConfirmPolicyModalCtrl);

    ConfirmPolicyModalCtrl.$inject = ['$modalInstance'];

    function ConfirmPolicyModalCtrl($modalInstance) {
        /*jshint validthis: true*/
        var vm = this;

        vm.ok = ok;
        vm.cancel = cancel;

        ///////////////////////////////////////

        function ok() {
            $modalInstance.close();
        };

        function cancel() {
            $modalInstance.dismiss('cancel');
        };
    };

})();
