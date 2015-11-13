(function() {
    'use strict';

    angular
      .module('webApp')
      .controller('InputsCtrl', InputsCtrl);

    InputsCtrl.$inject = ['FragmentFactory', '$filter', '$modal', 'UtilsService', 'TemplateFactory', 'PolicyFactory'];

    function InputsCtrl(FragmentFactory, $filter, $modal, UtilsService, TemplateFactory, PolicyFactory) {
        /*jshint validthis: true*/
       var vm = this;

       vm.deleteInput = deleteInput;
       vm.getInputTypes = getInputTypes;
       vm.createInput = createInput;
       vm.editInput = editInput;
       vm.duplicateInput = duplicateInput;
       vm.inputsData = undefined;
       vm.inputTypes = [];
       vm.error = false;
       vm.errorMessage = '';

       init();

        /////////////////////////////////

        function init() {
          getInputs();
        };

        function getInputs() {
          var inputList = FragmentFactory.getFragments('input');

          inputList.then(function (result) {
            vm.error = false;
            vm.inputsData = result;
            vm.getInputTypes(result);
          },function (error) {
            vm.error = true
            vm.errorMessage = "_INPUT_ERROR_" + error.data.i18nCode + "_";
          });

        };

        function createInput() {
          var inputsList = UtilsService.getNamesJSONArray(vm.inputsData);

          var createInputData = {
            'fragmentType': 'input',
            'fragmentNamesList' : inputsList,
            'texts': {
              'title': '_INPUT_WINDOW_NEW_TITLE_',
              'button': '_INPUT_WINDOW_NEW_BUTTON_',
              'button_icon': 'icon-circle-plus'
            }
          };

          return createInputModal(createInputData);
        };

        function editInput(input) {
          var inputsList = UtilsService.getNamesJSONArray(vm.inputsData);
          var editInputData = {
              'originalName': input.name,
              'fragmentType': 'input',
              'fragmentSelected': input,
              'fragmentNamesList' : inputsList,
              'texts': {
                  'title': '_INPUT_WINDOW_MODIFY_TITLE_',
                  'button': '_INPUT_WINDOW_MODIFY_BUTTON_',
                  'button_icon': 'icon-circle-check',
                  'secondaryText2': '_INPUT_WINDOW_EDIT_MESSAGE2_',
                  'policyRunningMain': '_INPUT_CANNOT_BE_MODIFIED_',
                  'policyRunningSecondary': '_INTPUT_WINDOW_POLICY_RUNNING_MESSAGE_',
                  'policyRunningSecondary2': '_INTPUT_WINDOW_POLICY_RUNNING_MESSAGE2_'
              }
          };

          return editInputModal(editInputData);
        };

        function deleteInput(fragmentType, fragmentId, elementType) {
          var inputToDelete =
          {
            'type':fragmentType,
            'id': fragmentId,
            'elementType': elementType,
            'texts': {
              'title': '_INPUT_WINDOW_DELETE_TITLE_',
              'mainText': '_ARE_YOU_COMPLETELY_SURE_',
              'secondaryText1': '_INPUT_WINDOW_DELETE_MESSAGE_',
              'secondaryText2': '_INPUT_WINDOW_DELETE_MESSAGE2_',
              'policyRunningMain': '_INPUT_CANNOT_BE_DELETED_',
              'policyRunningSecondary': '_INTPUT_WINDOW_POLICY_RUNNING_MESSAGE_',
              'policyRunningSecondary2': '_INTPUT_WINDOW_DELETE_POLICY_RUNNING_MESSAGE2_'
            }
          };
          return deleteInputConfirm(inputToDelete);
        };

        function duplicateInput(inputId) {
          var inputSelected = $filter('filter')(angular.copy(vm.inputsData), {'id':inputId}, true)[0];

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
          return setDuplicatetedInput(duplicateInputData);
        };

        function getInputTypes(inputs) {
            vm.inputTypes = [];
            for (var i=0; i<inputs.length; i++) {
                var newType = false;
                var type    = inputs[i].element.type;

                if (i === 0) {
                    vm.inputTypes.push({'type': type, 'count': 1});
                }
                else {
                    for (var j=0; j<vm.inputTypes.length; j++) {
                        if (vm.inputTypes[j].type === type) {
                            vm.inputTypes[j].count++;
                            newType = false;
                            break;
                        }
                        else if (vm.inputTypes[j].type !== type){
                            newType = true;
                        }
                    }
                    if (newType) {
                        vm.inputTypes.push({'type': type, 'count':1});
                    }
                }
            }
        };

        function createInputModal(newInputTemplateData) {
          var modalInstance = $modal.open({
            animation: true,
            templateUrl: 'templates/fragments/fragment-details.tpl.html',
            controller: 'NewFragmentModalCtrl as vm',
            size: 'lg',
            resolve: {
              item: function () {
                return newInputTemplateData;
              },
              fragmentTemplates: function () {
                return TemplateFactory.getNewFragmentTemplate(newInputTemplateData.fragmentType);
              }
            }
          });

          return modalInstance.result.then(function (newInputData) {
            vm.inputsData.push(newInputData);
            UtilsService.addFragmentCount(vm.inputTypes, newInputData.element.type);
          });
        };

        function editInputModal(editInputData) {
          var modalInstance = $modal.open({
            animation: true,
            templateUrl: 'templates/fragments/fragment-details.tpl.html',
            controller: 'EditFragmentModalCtrl as vm',
            size: 'lg',
            resolve: {
                item: function () {
                   return editInputData;
                },
                fragmentTemplates: function () {
                   return TemplateFactory.getNewFragmentTemplate(editInputData.fragmentSelected.fragmentType);
                },
                 policiesAffected: function () {
                  return PolicyFactory.getPolicyByFragmentId(editInputData.fragmentSelected.fragmentType, editInputData.fragmentSelected.id);
                }
            }
          });

          return modalInstance.result.then(function (editedInputData) {
            var originalFragment = editedInputData.originalFragment;
            var editedFragment = editedInputData.editedFragment;
            if (originalFragment.element.type !== editedFragment.element.type) {
              UtilsService.subtractFragmentCount(vm.inputTypes, originalFragment.element.type, vm.filters);
              UtilsService.addFragmentCount(vm.inputTypes, editedFragment.element.type);
            }

            for (var prop in editedInputData.originalFragment) delete editedInputData.originalFragment[prop];
            for (var prop in editedInputData.editedFragment) editedInputData.originalFragment[prop] =  editedInputData.editedFragment[prop];
          });
        };

        function deleteInputConfirm(input) {
          var modalInstance = $modal.open({
            animation: true,
            templateUrl: 'templates/components/st-delete-modal.tpl.html',
            controller: 'DeleteFragmentModalCtrl as vm',
            size: 'lg',
            resolve: {
              item: function () {
                  return input;
              },
              policiesAffected: function () {
                return PolicyFactory.getPolicyByFragmentId(input.type, input.id);
              }
            }
          });

          return modalInstance.result.then(function (fragmentDeletedData) {
            for (var i=0; i < vm.inputsData.length; i++) {
              if (vm.inputsData[i].id === fragmentDeletedData.id) {
                vm.inputsData.splice(i,1);
              }
            }
            UtilsService.subtractFragmentCount(vm.inputTypes, fragmentDeletedData.type, vm.filters);
          });
        };

        function setDuplicatetedInput(InputData) {
          var modalInstance = $modal.open({
            animation: true,
            templateUrl: 'templates/components/st-duplicate-modal.tpl.html',
            controller: 'DuplicateFragmentModalCtrl as vm',
            size: 'lg',
            resolve: {
              item: function () {
                  return InputData;
              }
            }
          });

          return modalInstance.result.then(function (newInputData) {
            vm.inputsData.push(newInputData);
            UtilsService.addFragmentCount(vm.inputTypes, newInputData.element.type);
          });
        };
    };
})();
