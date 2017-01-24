/*
 * Copyright (C) 2015 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
(function() {
  'use strict';

    /*DELETE INPUT MODALS CONTROLLER */
    angular
        .module('webApp')
        .controller('DeleteFragmentModalCtrl', DeleteFragmentModalCtrl);

    DeleteFragmentModalCtrl.$inject = ['$uibModalInstance', 'item', 'PolicyFactory', 'FragmentFactory', 'policiesAffected'];

    function DeleteFragmentModalCtrl($uibModalInstance, item, PolicyFactory, FragmentFactory, policiesAffected) {
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
        }

        function setPoliciesRunning(policiesList) {
          for (var i=0; i < policiesList.length; i++) {
            if (policiesList[i].status !== 'NotStarted' && policiesList[i].status !== 'Stopped' && policiesList[i].status !== 'Failed') {
              var policy = {'name':policiesList[i].policy.name}
              vm.policiesRunning.push(policy);
            }
          }
        }

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
        }

        function ok() {
            var fragmentToDelete = FragmentFactory.deleteFragment(vm.outputs.type, vm.outputs.id);

            fragmentToDelete.then(function (result) {
                $uibModalInstance.close({"id": vm.outputs.id, 'type': vm.outputs.elementType});

            },function (error) {
                vm.error = true;
                vm.errorText = "_ERROR_._" + error.data.i18nCode + "_";
            });
        }

        function cancel() {
            $uibModalInstance.dismiss('cancel');
        }
    }

})();
