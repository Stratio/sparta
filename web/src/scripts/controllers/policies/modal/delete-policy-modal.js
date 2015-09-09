(function() {
  'use strict';

    /*DELETE POLICIES MODAL CONTROLLER */
    angular
        .module('webApp')
        .controller('DeletePolicyModalCtrl', DeletePolicyModalCtrl);

    DeletePolicyModalCtrl.$inject = ['$modalInstance', 'item', 'PolicyFactory'];

    function DeletePolicyModalCtrl($modalInstance, item, PolicyFactory) {
        /*jshint validthis: true*/
        var vm = this;

        vm.ok = ok;
        vm.cancel = cancel;
        vm.error = false;

        init();

        ///////////////////////////////////////

        function init () {
            console.log('*********Modal');
            console.log(item);

            vm.policyData = item;
        };

        function ok() {
            var policyToDelete = PolicyFactory.DeletePolicy(vm.policyData.id);

            policyToDelete.then(function (result) {
                console.log('*********Policy deleted');
                $modalInstance.close(vm.policyData);

            },function (error) {
                console.log(error);
                vm.error = true;
                vm.errorText = "_INPUT_ERROR_" + error.data.i18nCode + "_";
            });
        };

        function cancel() {
            $modalInstance.dismiss('cancel');
        };
    };

})();
