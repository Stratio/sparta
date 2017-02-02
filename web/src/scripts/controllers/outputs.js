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

    angular
      .module('webApp')
      .controller('OutputsCtrl', OutputsCtrl);

    OutputsCtrl.$inject = ['FragmentFactory', '$filter', '$uibModal', 'UtilsService'];

    function OutputsCtrl(FragmentFactory, $filter, $uibModal, UtilsService) {
      /*jshint validthis: true*/
      var vm = this;

      vm.createOutput = createOutput;
      vm.editOutput = editOutput;
      vm.deleteOutput = deleteOutput;
      vm.duplicateOutput = duplicateOutput;
      vm.deleteErrorMessage = deleteErrorMessage;
      vm.outputsData = undefined;
      vm.outputTypes = [];
      vm.errorMessage =  {type: 'error',text: '', internalTrace: ''};

      init();

      /////////////////////////////////

      function init() {
        getOutputs();
      }

      function deleteErrorMessage() {
        vm.errorMessage.text = '';
      }

      function getOutputs() {
        var outputList = FragmentFactory.getFragments('output');

        outputList.then(function (result) {
          vm.outputsData = result;
          getOutputTypes(result);

        },function (error) {
          vm.errorMessage.text = "_ERROR_._" + error.data.i18nCode + "_";
        });

      }

      function getOutputTypes(outputs) {
        vm.outputTypes = [];
        for (var i=0; i<outputs.length; i++) {
            var newType = false;
            var type    = outputs[i].element.type;

            if (i === 0) {
                vm.outputTypes.push({'type': type, 'count': 1});
            }
            else {
                for (var j=0; j<vm.outputTypes.length; j++) {
                    if (vm.outputTypes[j].type === type) {
                        vm.outputTypes[j].count++;
                        newType = false;
                        break;
                    }
                    else if (vm.outputTypes[j].type !== type){
                        newType = true;
                    }
                }
                if (newType) {
                    vm.outputTypes.push({'type': type, 'count':1});
                }
            }
        }
      }

      function createOutput() {
        var outputsList = UtilsService.getNamesJSONArray(vm.outputsData);

        var createOutputData = {
          'fragmentType': 'output',
          'fragmentNamesList' : outputsList,
          'texts': {
            'title': '_OUTPUT_WINDOW_NEW_TITLE_',
            'button': '_OUTPUT_WINDOW_NEW_BUTTON_',
            'button_icon': 'icon-circle-plus'
          }
        };

        createOutputModal(createOutputData);
      }

      function editOutput(output) {
        var outputsList = UtilsService.getNamesJSONArray(vm.outputsData);
        var editOutputData = {
            'originalName': output.name,
            'fragmentType': 'output',
            'fragmentSelected': output,
            'fragmentNamesList' : outputsList,
            'texts': {
                'title': '_OUTPUT_WINDOW_MODIFY_TITLE_',
                'button': '_OUTPUT_WINDOW_MODIFY_BUTTON_',
                'button_icon': 'icon-circle-check',
                'secondaryText': '_OUTPUT_WINDOW_EDIT_MESSAGE_'
            }
        };

        editOutputModal(editOutputData);
      }

      function deleteOutput(fragmentType, fragmentId, elementType) {
               var outputToDelete =
        {
          'type':fragmentType,
          'id': fragmentId,
            'elementType': elementType,
          'texts': {
            'title': '_OUTPUT_WINDOW_DELETE_TITLE_',
            'mainText': '_ARE_YOU_SURE_',
            'secondaryText': '_OUTPUT_WINDOW_DELETE_MESSAGE_'
          }
        };
        deleteOutputConfirm('lg', outputToDelete);
      }

      function duplicateOutput(outputId) {
        var outputSelected = $filter('filter')(angular.copy(vm.outputsData), {'id':outputId}, true)[0];

        var newName = UtilsService.autoIncrementName(outputSelected.name);
        outputSelected.name = newName;

        var outputsList = UtilsService.getNamesJSONArray(vm.outputsData);

        var duplicateOutputData = {
          'fragmentData': outputSelected,
          'fragmentNamesList': outputsList,
          'texts': {
            'title': '_OUTPUT_WINDOW_DUPLICATE_TITLE_'
          }
        };

        setDuplicatedOutput('sm', duplicateOutputData);
      }

      function createOutputModal(newOutputTemplateData) {
        var modalInstance = $uibModal.open({
          animation: true,
          templateUrl: 'templates/fragments/fragment-details.tpl.html',
          controller: 'NewFragmentModalCtrl as vm',
          size: 'lg',
          resolve: {
            item: function () {
              return newOutputTemplateData;
            },
            fragmentTemplates: function (TemplateFactory) {
              return TemplateFactory.getNewFragmentTemplate(newOutputTemplateData.fragmentType);
            }
          }
        });

        modalInstance.result.then(function (newOutputData) {
          vm.outputsData.push(newOutputData);
          UtilsService.addFragmentCount(vm.outputTypes, newOutputData.element.type);
        });
      }

      function editOutputModal(editOutputData) {
        var modalInstance = $uibModal.open({
            animation: true,
            templateUrl: 'templates/fragments/fragment-details.tpl.html',
            controller: 'EditFragmentModalCtrl as vm',
            size: 'lg',
            resolve: {
              item: function () {
                return editOutputData;
              },
              fragmentTemplates: function (TemplateFactory) {
                return TemplateFactory.getNewFragmentTemplate(editOutputData.fragmentSelected.fragmentType);
              }
            }
      });

        modalInstance.result.then(function (editedOutputData) {
          var originalFragment = editedOutputData.originalFragment;
          var editedFragment = editedOutputData.editedFragment;
          if (originalFragment.element.type !== editedFragment.element.type) {
            UtilsService.subtractFragmentCount(vm.outputTypes, originalFragment.element.type, vm.filters);
            UtilsService.addFragmentCount(vm.outputTypes, editedFragment.element.type);
          }

          for (var prop in editedOutputData.originalFragment) delete editedOutputData.originalFragment[prop];
          for (var prop in editedOutputData.editedFragment) editedOutputData.originalFragment[prop] =  editedOutputData.editedFragment[prop];
        });
      }

      function deleteOutputConfirm(size, output) {
        var modalInstance = $uibModal.open({
          animation: true,
          templateUrl: 'templates/components/st-delete-modal.tpl.html',
          controller: 'DeleteFragmentModalCtrl as vm',
          size: size,
          resolve: {
              item: function () {
                  return output;
              }
          }
        });

        modalInstance.result.then(function (fragmentDeletedData) {
          for (var i=0; i < vm.outputsData.length; i++) {
            if (vm.outputsData[i].id === fragmentDeletedData.id) {
              vm.outputsData.splice(i,1);
            }
          }
          UtilsService.subtractFragmentCount(vm.outputTypes, fragmentDeletedData.type, vm.filters);
        });
      }

      function setDuplicatedOutput(size, OutputData) {
        var modalInstance = $uibModal.open({
          animation: true,
          templateUrl: 'templates/components/st-duplicate-modal.tpl.html',
          controller: 'DuplicateFragmentModalCtrl as vm',
          size: 'lg',
          resolve: {
              item: function () {
                  return OutputData;
              }
          }
        });

        modalInstance.result.then(function (newOutput) {
          vm.outputsData.push(newOutput);
          UtilsService.addFragmentCount(vm.outputTypes, newOutput.element.type);
        });
      }

    }
})();
