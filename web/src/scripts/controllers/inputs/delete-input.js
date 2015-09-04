(function() {
  'use strict';

    /*DELETE INPUT MODALS CONTROLLER */
    angular
        .module('webApp')
        .controller('DeleteFragmentModalCtrl', DeleteFragmentModalCtrl);

    DeleteFragmentModalCtrl.$inject = ['$modalInstance', 'item', 'PolicyFactory', 'FragmentFactory', 'policiesAffected'];

    function DeleteFragmentModalCtrl($modalInstance, item, PolicyFactory, FragmentFactory, policiesAffected) {
        /*jshint validthis: true*/
        var vm = this;

        vm.ok = ok;
        vm.cancel = cancel;
        vm.error = false;

        init();

        ///////////////////////////////////////

        function init () {
            console.log(policiesAffected);
            console.log('*********Modal');
            console.log(item);

            setTexts(item.texts);

            vm.outputs = item;
            vm.outputs.policies = policiesAffected;
        };

        function setTexts(texts) {
          vm.modalTexts = {};
          vm.modalTexts.title = texts.title;
        };

        function ok() {
            var fragmentToDelete = FragmentFactory.DeleteFragment(vm.outputs.type, vm.outputs.id);

            fragmentToDelete.then(function (result) {
                console.log('*********Fragment deleted');
                $modalInstance.close(vm.outputs);

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
