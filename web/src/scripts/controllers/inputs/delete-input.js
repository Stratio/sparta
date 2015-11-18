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
        vm.policiesRunning = [];

        init();

        ///////////////////////////////////////

        function init () {
            setPoliciesRunning(policiesAffected);

            vm.outputs = item;
            vm.outputs.policies = policiesAffected;

            setTexts(item.texts);
        };

        function setPoliciesRunning(policiesList) {
          for (var i=0; i < policiesList.length; i++) {
            if (policiesList[i].status !== 'NotStarted' && policiesList[i].status !== 'Stopped' && policiesList[i].status !== 'Failed') {
              var policy = {'name':policiesList[i].policy.name}
              vm.policiesRunning.push(policy);
            }
          }
        };

        function setTexts(texts) {
          vm.modalTexts = {};
          vm.modalTexts.title = texts.title;
          vm.modalTexts.secondaryText1 = texts.secondaryText1;
          vm.modalTexts.secondaryText2 = texts.secondaryText2;
          vm.modalTexts.policyRunningMain = texts.policyRunningMain;
          vm.modalTexts.policyRunningSecondary = texts.policyRunningSecondary;
          vm.modalTexts.policyRunningSecondary2 = texts.policyRunningSecondary2;

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
                $modalInstance.close({"id": vm.outputs.id, 'type': vm.outputs.elementType});

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
