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
            vm.outputs = item;
            vm.outputs.policies = policiesAffected;

            setTexts(item.texts);
        };

        function setTexts(texts) {
          vm.modalTexts = {};
          vm.modalTexts.title = texts.title;
          vm.modalTexts.secondaryText1 = texts.secondaryText1;
          vm.modalTexts.secondaryText2 = texts.secondaryText2;
          if (vm.outputs.type === 'output') {
            vm.modalTexts.mainText = (vm.outputs.policies.length > 0)? texts.mainText : texts.mainTextOK;
          }
          else {
            vm.modalTexts.mainText = texts.mainText;
          }
        };

        function ok() {
            var fragmentToDelete = FragmentFactory.deleteFragment(vm.outputs.type, vm.outputs.id);

            fragmentToDelete.then(function (result) {
                $modalInstance.close(vm.outputs);

            },function (error) {
                vm.error = true;
                vm.errorText = "_INPUT_ERROR_" + error.data.i18nCode + "_";
            });
        };

        function cancel() {
            $modalInstance.dismiss('cancel');
        };
    };

})();
