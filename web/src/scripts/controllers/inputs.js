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

  angular
    .module('webApp')
    .controller('InputsCtrl', InputsCtrl);

  InputsCtrl.$inject = ['FragmentFactory', '$filter', '$uibModal', 'UtilsService', 'TemplateFactory', 'PolicyFactory'];

  function InputsCtrl(FragmentFactory, $filter, $uibModal, UtilsService, TemplateFactory, PolicyFactory) {
    /*jshint validthis: true*/
    var vm = this;

    vm.deleteInput = deleteInput;
    vm.getInputTypes = getInputTypes;
    vm.createInput = createInput;
    vm.editInput = editInput;
    vm.duplicateInput = duplicateInput;
    vm.deleteErrorMessage = deleteErrorMessage;
    vm.inputsData = undefined;
    vm.inputTypes = [];
    vm.errorMessage =  {type: 'error',text: '', internalTrace: ''};

    init();

    /////////////////////////////////

    function init() {
      getInputs();
    }

    function deleteErrorMessage() {
      vm.errorMessage.text = '';
    }

    function getInputs() {
      var inputList = FragmentFactory.getFragments('input');

      inputList.then(function (result) {
        vm.errorMessage.text = '';
        vm.inputsData = result;
        vm.getInputTypes(result);
      }, function (error) {
        vm.errorMessage.text = "_ERROR_._" + error.data.i18nCode + "_";
      });

    }

    function createInput() {
      var inputsList = UtilsService.getNamesJSONArray(vm.inputsData);

      var createInputData = {
        'fragmentType': 'input',
        'fragmentNamesList': inputsList,
        'texts': {
          'title': '_INPUT_WINDOW_NEW_TITLE_',
          'button': '_INPUT_WINDOW_NEW_BUTTON_',
          'button_icon': 'icon-circle-plus'
        }
      };

      return createInputModal(createInputData);
    }

    function editInput(input) {
      var inputsList = UtilsService.getNamesJSONArray(vm.inputsData);
      var editInputData = {
        'originalName': input.name,
        'fragmentType': 'input',
        'fragmentSelected': input,
        'fragmentNamesList': inputsList,
        'texts': {
          'title': '_INPUT_WINDOW_MODIFY_TITLE_',
          'button': '_INPUT_WINDOW_MODIFY_BUTTON_',
          'button_icon': 'icon-circle-check',
          'secondaryText': '_INPUT_WINDOW_EDIT_MESSAGE_'
        }
      };

      return editInputModal(editInputData);
    }

    function deleteInput(fragmentType, fragmentId, elementType) {
      var inputToDelete =
      {
        'type': fragmentType,
        'id': fragmentId,
        'elementType': elementType,
        'texts': {
          'title': '_INPUT_WINDOW_DELETE_TITLE_',
          'mainText': '_ARE_YOU_SURE_',
          'secondaryText': '_INPUT_WINDOW_DELETE_MESSAGE_'
        }
      };
      return deleteInputConfirm(inputToDelete);
    }

    function duplicateInput(inputId) {
      var inputSelected = $filter('filter')(angular.copy(vm.inputsData), {'id': inputId}, true)[0];

      var newName = UtilsService.autoIncrementName(inputSelected.name);
      inputSelected.name = newName;

      var inputsList = UtilsService.getNamesJSONArray(vm.inputsData);

      var duplicateInputData = {
        'fragmentData': inputSelected,
        'fragmentNamesList': inputsList,
        'texts': {
          'title': '_INPUT_WINDOW_DUPLICATE_TITLE_'
        }
      };
      return setDuplicatedInput(duplicateInputData);
    }

    function getInputTypes(inputs) {
      vm.inputTypes = [];
      for (var i = 0; i < inputs.length; i++) {
        var newType = false;
        var type = inputs[i].element.type;

        if (i === 0) {
          vm.inputTypes.push({'type': type, 'count': 1});
        }
        else {
          for (var j = 0; j < vm.inputTypes.length; j++) {
            if (vm.inputTypes[j].type === type) {
              vm.inputTypes[j].count++;
              newType = false;
              break;
            }
            else if (vm.inputTypes[j].type !== type) {
              newType = true;
            }
          }
          if (newType) {
            vm.inputTypes.push({'type': type, 'count': 1});
          }
        }
      }
    }

    function createInputModal(creationFragmentData) {
      var modalInstance = $uibModal.open({
        animation: true,
        templateUrl: 'templates/fragments/fragment-details.tpl.html',
        controller: 'NewFragmentModalCtrl as vm',
        size: 'lg',
        resolve: {
          creationFragmentData: function () {
            return creationFragmentData;
          },
          fragmentTemplate: function () {
            return TemplateFactory.getNewFragmentTemplate(creationFragmentData.fragmentType);
          }
        }
      });

      return modalInstance.result.then(function (newInputData) {
        vm.inputsData.push(newInputData);
        UtilsService.addFragmentCount(vm.inputTypes, newInputData.element.type);
      });
    }

    function editInputModal(editInputData) {
      var modalInstance = $uibModal.open({
        animation: true,
        templateUrl: 'templates/fragments/fragment-details.tpl.html',
        controller: 'EditFragmentModalCtrl as vm',
        size: 'lg',
        resolve: {
          creationFragmentData: function () {
            return editInputData;
          },
          fragmentTemplate: function () {
            return TemplateFactory.getNewFragmentTemplate(editInputData.fragmentSelected.fragmentType);
          }
        }
      });

      return modalInstance.result.then(function (editedInputData) {
        var originalFragment = editedInputData.originalFragment;
        var editedFragment = editedInputData.editedFragment;
        if (originalFragment.element.type !== editedFragment.element.type) {
          UtilsService.subtractFragmentCount(vm.inputTypes, originalFragment.element.type);
          UtilsService.addFragmentCount(vm.inputTypes, editedFragment.element.type);
        }

        for (var prop in editedInputData.originalFragment) delete editedInputData.originalFragment[prop];
        for (var prop in editedInputData.editedFragment) editedInputData.originalFragment[prop] = editedInputData.editedFragment[prop];
      });
    }

    function deleteInputConfirm(input) {
      var modalInstance = $uibModal.open({
        animation: true,
        templateUrl: 'templates/components/st-delete-modal.tpl.html',
        controller: 'DeleteFragmentModalCtrl as vm',
        size: 'lg',
        resolve: {
          fragmentTemplate: function () {
            return input;
          }
        }
      });

      return modalInstance.result.then(function (fragmentDeletedData) {
        for (var i = 0; i < vm.inputsData.length; i++) {
          if (vm.inputsData[i].id === fragmentDeletedData.id) {
            vm.inputsData.splice(i, 1);
          }
        }
        UtilsService.subtractFragmentCount(vm.inputTypes, fragmentDeletedData.type, vm.filters);
      });
    }

    function setDuplicatedInput(InputData) {
      var modalInstance = $uibModal.open({
        animation: true,
        templateUrl: 'templates/components/st-duplicate-modal.tpl.html',
        controller: 'DuplicateFragmentModalCtrl as vm',
        size: 'lg',
        resolve: {
          fragmentTemplate: function () {
            return InputData;
          }
        }
      });

      return modalInstance.result.then(function (newInputData) {
        vm.inputsData.push(newInputData);
        UtilsService.addFragmentCount(vm.inputTypes, newInputData.element.type);
      });
    }
  }
})();
