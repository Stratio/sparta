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
    .controller('PolicyModelAccordionCtrl', PolicyModelAccordionCtrl);

  PolicyModelAccordionCtrl.$inject = ['PolicyModelFactory', 'ModelFactory', 'ModelService','TriggerModelFactory', 'TriggerService', 'triggerConstants'];

  function PolicyModelAccordionCtrl(PolicyModelFactory, ModelFactory, ModelService,TriggerModelFactory, TriggerService, triggerConstants) {
    var vm = this;

    vm.init = init;
    vm.changeOpenedModel = changeOpenedModel;
    vm.changeOpenedTrigger = changeOpenedTrigger;
    vm.isActiveModelCreationPanel = ModelService.isActiveModelCreationPanel;
    vm.activateModelCreationPanel = activateModelCreationPanel;
    vm.isActiveTriggerCreationPanel = TriggerService.isActiveTriggerCreationPanel;
    vm.activateTriggerCreationPanel = activateTriggerCreationPanel;
    vm.init();

    function init() {
      vm.outputsWidth = "m";
      vm.template = PolicyModelFactory.getTemplate();
      vm.policy = PolicyModelFactory.getCurrentPolicy();
      TriggerService.setTriggerContainer(vm.policy.streamTriggers, triggerConstants.TRANSFORMATION);
      vm.triggerContainer = vm.policy.streamTriggers;
      vm.helpLink = vm.template.helpLinks.models;
      vm.error = "";
      vm.modelAccordionStatus = [];
      vm.triggerAccordionStatus = [];
      TriggerService.changeVisibilityOfHelpForSql(true);

      if (vm.policy.transformations.length > 0) {
        PolicyModelFactory.enableNextStep();
      } else {
        ModelService.changeModelCreationPanelVisibility(true);
      }
    }

    function activateModelCreationPanel(){
      ModelService.activateModelCreationPanel();
      TriggerService.disableTriggerCreationPanel();
    }

    function activateTriggerCreationPanel(){
      TriggerService.activateTriggerCreationPanel();
      ModelService.disableModelCreationPanel();
    }

    function changeOpenedModel(selectedModelPosition) {
      if (vm.policy.transformations.length > 0 && selectedModelPosition >= 0 && selectedModelPosition < vm.policy.transformations.length) {
        var selectedModel = vm.policy.transformations[selectedModelPosition];
        ModelFactory.setModel(selectedModel, selectedModelPosition);
      } else {
        var modelNumber = vm.policy.transformations.length;
        var order = 0;

        if (modelNumber > 0) {
          order = vm.policy.transformations[modelNumber - 1].order + 1
        }
        ModelFactory.resetModel(vm.template.model, order, vm.policy.transformations.length);
      }
      ModelFactory.updateModelInputs(vm.policy.transformations);
    }

    function changeOpenedTrigger(selectedTriggerPosition) {
      if (vm.policy.streamTriggers.length > 0 && selectedTriggerPosition >= 0 && selectedTriggerPosition < vm.policy.streamTriggers.length) {
        var selectedTrigger = vm.policy.streamTriggers[selectedTriggerPosition];
        TriggerModelFactory.setTrigger(selectedTrigger, selectedTriggerPosition);
      } else {
        TriggerModelFactory.resetTrigger(vm.policy.streamTriggers.length);
      }
    }
  }
})();
