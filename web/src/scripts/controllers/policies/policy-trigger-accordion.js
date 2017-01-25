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
(function () {
  'use strict';

  /*POLICY MODELS CONTROLLER*/
  angular
    .module('webApp')
    .controller('PolicyTriggerAccordionCtrl', PolicyTriggerAccordionCtrl);

  PolicyTriggerAccordionCtrl.$inject = ['WizardStatusService', 'PolicyModelFactory', 'ModelFactory', 'ModelService',
    'TriggerService', 'triggerConstants', '$scope'];

  function PolicyTriggerAccordionCtrl(WizardStatusService, PolicyModelFactory, ModelFactory, ModelService,
                                    TriggerService, triggerConstants, $scope) {
    var vm = this;

    vm.init = init;
    vm.changeOpenedTrigger = TriggerService.changeOpenedTrigger;
    vm.isActiveTriggerCreationPanel = TriggerService.isActiveTriggerCreationPanel;
    vm.triggerCreationStatus = TriggerService.getTriggerCreationStatus();
    vm.activateTriggerCreationPanel = activateTriggerCreationPanel;

    vm.init();

    function init() {
      vm.outputsWidth = "m";
      vm.template = PolicyModelFactory.getTemplate();
      vm.policy = PolicyModelFactory.getCurrentPolicy();
      TriggerService.setTriggerContainer(vm.policy.streamTriggers, triggerConstants.TRANSFORMATION);
      vm.triggerContainer = vm.policy.streamTriggers;
      vm.triggerAccordionStatus = [];
      TriggerService.changeVisibilityOfHelpForSql(true);
    }

    function activateTriggerCreationPanel() {
      vm.triggerAccordionStatus[vm.triggerAccordionStatus.length - 1] = true;
      TriggerService.activateTriggerCreationPanel();
    }

    $scope.$on("forceValidateForm", function () {
      if (vm.isActiveTriggerCreationPanel()) {
        PolicyModelFactory.setError("_ERROR_._CHANGES_WITHOUT_SAVING_", "error");
        vm.triggerAccordionStatus[vm.triggerAccordionStatus.length - 1] = true;
      }
    });

    $scope.$watchCollection(
      "vm.triggerCreationStatus",
      function (triggerCreationStatus) {
        if (!triggerCreationStatus.enabled) {
          WizardStatusService.enableNextStep();
        } else {
          WizardStatusService.disableNextStep();
        }
      }
    );
  }
})();
