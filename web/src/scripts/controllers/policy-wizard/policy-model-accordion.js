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

  PolicyModelAccordionCtrl.$inject = ['WizardStatusService', 'PolicyModelFactory', 'ModelFactory', 'ModelService',
    '$scope'];

  function PolicyModelAccordionCtrl(WizardStatusService, PolicyModelFactory, ModelFactory, ModelService, $scope) {
    var vm = this;

    vm.init = init;
    vm.isActiveModelCreationPanel = ModelService.isActiveModelCreationPanel;
    vm.modelCreationStatus = ModelService.getModelCreationStatus();
    vm.changeOpenedModel = changeOpenedModel;
    vm.activateModelCreationPanel = activateModelCreationPanel;
  
    vm.init();

    function init() {
      vm.outputsWidth = "m";
      vm.template = PolicyModelFactory.getTemplate();
      vm.policy = PolicyModelFactory.getCurrentPolicy();
      vm.modelAccordionStatus = [];
      if (vm.policy.transformations.length == 0) {
        ModelService.changeModelCreationPanelVisibility(true);
        activateModelCreationPanel();
      }
    }

    function activateModelCreationPanel() {
      vm.modelAccordionStatus[vm.modelAccordionStatus.length - 1] = true;
      ModelService.activateModelCreationPanel();
      ModelService.resetModel(vm.template);
    }

    function changeOpenedModel(selectedModelPosition) {
      if (vm.policy.transformations.length > 0 && selectedModelPosition >= 0 && selectedModelPosition < vm.policy.transformations.length) {
        var selectedModel = vm.policy.transformations[selectedModelPosition];
        ModelFactory.setModel(selectedModel, selectedModelPosition);
      } else {
          ModelService.resetModel(vm.template);
      }
      ModelFactory.updateModelInputs(vm.policy.transformations);
    }

    $scope.$on("forceValidateForm", function () {
      if (vm.policy.transformations.length == 0) {
        PolicyModelFactory.setError("_ERROR_._TRANSFORMATION_STEP_", "error");
      } else {
        if (vm.isActiveModelCreationPanel()) {
          PolicyModelFactory.setError("_ERROR_._CHANGES_WITHOUT_SAVING_", "error");
        }
      }
      if (vm.isActiveModelCreationPanel()) {
        vm.modelAccordionStatus[vm.modelAccordionStatus.length - 1] = true;
      }
    });

    $scope.$watchCollection(
      "vm.modelCreationStatus",
      function (modelCreationStatus) {
        if (!modelCreationStatus.enabled && vm.policy.transformations.length > 0) {
          WizardStatusService.enableNextStep();
        } else {
          WizardStatusService.disableNextStep();
        }
      }
    );

    $scope.$watchCollection(
      "vm.policy.transformations",
      function (transformations) {
        if (transformations.length > 0) {
          WizardStatusService.enableNextStep();
        } else {
          WizardStatusService.disableNextStep();
        }
      }
    )
  }
})();
