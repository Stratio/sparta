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

  PolicyModelAccordionCtrl.$inject = ['WizardStatusService', 'PolicyModelFactory', 'ModelFactory', 'ModelService', '$scope', 'ModalService', '$translate'
  ];

  function PolicyModelAccordionCtrl(WizardStatusService, PolicyModelFactory, ModelFactory, ModelService, $scope, ModalService, $translate) {
    var vm = this;

    vm.init = init;
    vm.isActiveModelCreationPanel = ModelService.isActiveModelCreationPanel;
    vm.modelCreationStatus = ModelService.getModelCreationStatus();
    vm.changeOpenedModel = changeOpenedModel;
    vm.activateModelCreationPanel = activateModelCreationPanel;
    vm.saveData = saveData;
    vm.createMode = true; //creation mode
    vm.confirmDeleteSaveData = confirmDeleteSaveData;
    vm.successMessage = {
      type: 'success',
      text: '',
      internalTrace: ''
    };
    vm.selectedOption = 'TRANSFORMATIONS';
    vm.menuOptions = [{
      text: '_MENU_TABS_TRANSFORMATIONS_',
      isDisabled: false,
      name: 'TRANSFORMATIONS'
    }, {
      text: '_MENU_TABS_SAVE_OPTIONS_',
      isDisabled: false,
      name: 'SAVE_OPTIONS'
    }];


    vm.init();

    function init() {
      resetSaveData();
      vm.outputsWidth = "m";
      vm.template = PolicyModelFactory.getTemplate();
      vm.outputsTemplate = vm.template.model.outputs.writer;
      vm.policy = PolicyModelFactory.getCurrentPolicy();
      vm.modelAccordionStatus = [];
      if (vm.policy.transformations.writer && vm.policy.transformations.writer.tableName) {
         vm.createMode = false; //creation mode
        vm.saveOptions.writer = angular.copy(vm.policy.transformations.writer);
      }
      if (vm.policy.transformations.transformationsPipe.length == 0) {
        ModelService.changeModelCreationPanelVisibility(true);
        activateModelCreationPanel();
      }
    }

    function activateModelCreationPanel() {
      vm.modelAccordionStatus[vm.modelAccordionStatus.length - 1] = true;
      ModelService.activateModelCreationPanel();
      ModelService.resetModel(vm.template);
    }

    function cancelModelUpdatePanel() {
      for (var i = 0; i < vm.modelAccordionStatus.length; i++) {
        vm.modelAccordionStatus[i] = false;
      }
    }

    function changeOpenedModel(selectedModelPosition) {
      if (vm.policy.transformations.transformationsPipe.length > 0 && selectedModelPosition >= 0 && selectedModelPosition < vm.policy.transformations.transformationsPipe.length) {
        var selectedModel = vm.policy.transformations.transformationsPipe[selectedModelPosition];
        var selectedModelCopy = angular.copy(selectedModel);
        ModelFactory.setModel(selectedModelCopy, selectedModelPosition);
      } else {
        ModelService.resetModel(vm.template);
      }
      ModelFactory.updateModelInputs(vm.policy.transformations.transformationsPipe);
    }

    function saveData(form) {
      form.$submitted = true;
      if (form.$valid) {
        vm.createMode = false;
        vm.policy.transformations.writer = vm.saveOptions.writer;
        vm.form.$setPristine();
        vm.successMessage.text = $translate.instant('_OPTIONS_SAVED_OK_');
      }
    }

    function confirmDeleteSaveData() {
      var templateUrl = "templates/modal/confirm-modal.tpl.html";
      var controller = "ConfirmModalCtrl";
      var extraClass = null;
      var size = 'lg';
      var resolve = {
        title: function () {
          return "_SAVE_OPTIONS_WINDOW_DELETE_TITLE_"
        },
        message: function () {
          return ""
        }
      };
      var modalInstance = ModalService.openModal(controller, templateUrl, resolve, extraClass, size);
      return modalInstance.result.then(function () {
        removeSaveData();
      });
    }


    function removeSaveData() {
      vm.form.$submitted = false;
      vm.form.$dirty = false;
      vm.createMode = true; //creation mode
      vm.creationPanel = false; //hide creationPanel
      vm.policy.transformations.writer = {}; //remove curren raw data
      resetSaveData(); //reset form
    }

    function resetSaveData() {
      vm.saveOptions = {
        writer: {
          outputs: []
        }
      }
    }

    $scope.$on("forceValidateForm", function () {
      if (vm.policy.transformations.transformationsPipe.length == 0) {
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
        if (!modelCreationStatus.enabled && vm.policy.transformations.transformationsPipe.length > 0) {
          WizardStatusService.enableNextStep();
        } else {
          WizardStatusService.disableNextStep();
        }
      }
    );

    $scope.$watchCollection(
      "vm.policy.transformations.transformationsPipe",
      function (transformations) {
        if (transformations.length > 0) {
          WizardStatusService.enableNextStep();
        } else {
          WizardStatusService.disableNextStep();
        }
      }
    );
  }
})();
