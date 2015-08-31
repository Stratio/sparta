(function() {
  'use strict';

    /*DELETE INPUT MODALS CONTROLLER */
    angular
        .module('webApp')
        .controller('DeleteFragmentModalCtrl', DeleteFragmentModalCtrl);

    DeleteFragmentModalCtrl.$inject = ['$modalInstance', 'item'];

    function DeleteFragmentModalCtrl($modalInstance, item) {
        /*jshint validthis: true*/
        var vm = this;

        vm.ok = ok;
        vm.cancel = cancel;

        init();

        ///////////////////////////////////////

        function init () {
            console.log('*********Modal');
            console.log(item);
            vm.inputs = item;
        };

        function ok() {
            $modalInstance.close(vm.inputs);
        };

        function cancel() {
            $modalInstance.dismiss('cancel');
        };
    };

})();
