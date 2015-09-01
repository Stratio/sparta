(function() {
  'use strict';

    /*DELETE INPUT MODALS CONTROLLER */
    angular
        .module('webApp')
        .controller('DeleteFragmentModalCtrl', DeleteFragmentModalCtrl);

    DeleteFragmentModalCtrl.$inject = ['$modalInstance', 'item', 'PolicyFactory', 'FragmentFactory'];

    function DeleteFragmentModalCtrl($modalInstance, item, PolicyFactory, FragmentFactory) {
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

            vm.inputs = item;

            getPoliciesAffected(vm.inputs);
        };

        function getPoliciesAffected(fragmentData) {
            console.log('> Getting Policies affected');
            var policiesAffected = PolicyFactory.GetPolicyByFragmentId(fragmentData.type, fragmentData.id);

            policiesAffected.then(function (result) {
                console.log(result);
                vm.inputs.policies = getPolicyNames(result);
            },
            function (error) {
                console.log('#ERROR#');
                console.log(error);
            });
        };

        function ok() {
            var fragmentToDelete = FragmentFactory.DeleteFragment(vm.inputs.type, vm.inputs.id);

            fragmentToDelete.then(function (result) {
                console.log('*********Fragment deleted');
                $modalInstance.close(vm.inputs);

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
