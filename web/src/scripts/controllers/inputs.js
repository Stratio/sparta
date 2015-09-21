(function() {
    'use strict';

    angular
      .module('webApp')
      .controller('InputsCtrl', InputsCtrl);

    InputsCtrl.$inject = ['FragmentFactory', '$filter', '$modal'];

    function InputsCtrl(FragmentFactory, $filter, $modal) {
        /*jshint validthis: true*/
       var vm = this;

       vm.getInputs = getInputs;
       vm.deleteInput = deleteInput;
       vm.getInputTypes = getInputTypes;
       vm.createInput = createInput;
       vm.editInput = editInput;
       vm.createInputModal = createInputModal;
       vm.editInputModal = editInputModal;
       vm.duplicateInput = duplicateInput;
       vm.deleteInputConfirm = deleteInputConfirm;
       vm.getPolicyNames = getPolicyNames;
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
          var inputsList = getFragmentsNames(vm.inputsData);

          var createInputData = {
            'fragmentType': 'input',
            'fragmentNamesList' : inputsList,
            'texts': {
              'title': '_INPUT_WINDOW_NEW_TITLE_',
              'button': '_INPUT_WINDOW_NEW_BUTTON_',
              'button_icon': 'icon-circle-plus'
            }
          };

          vm.createInputModal(createInputData);
        };

        function editInput(inputType, inputName, inputId, index) {
          var inputSelected = $filter('filter')(angular.copy(vm.inputsData), {'id':inputId}, true)[0];
          var inputsList = getFragmentsNames(vm.inputsData);

          var editInputData = {
              'originalName': inputName,
              'fragmentType': 'input',
              'index': index,
              'fragmentSelected': inputSelected,
              'fragmentNamesList' : inputsList,
              'texts': {
                  'title': '_INPUT_WINDOW_MODIFY_TITLE_',
                  'button': '_INPUT_WINDOW_MODIFY_BUTTON_',
                  'button_icon': 'icon-circle-check',
                  'secondaryText2': '_INPUT_WINDOW_EDIT_MESSAGE2_',
                  'policyRunningMain': '_INPUT_CANNOT_BE_DELETED_',
                  'policyRunningSecondary': '_INTPUT_WINDOW_POLICY_RUNNING_MESSAGE_',
                  'policyRunningSecondary2': '_INTPUT_WINDOW_POLICY_RUNNING_MESSAGE2_'
              }
          };

          vm.editInputModal(editInputData);
        };

        function deleteInput(fragmentType, fragmentId, index) {
                  var inputToDelete =
          {
            'type':fragmentType,
            'id': fragmentId,
            'index': index,
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
          vm.deleteInputConfirm('lg', inputToDelete);
        };

        function duplicateInput(inputId) {
            var inputSelected = $filter('filter')(angular.copy(vm.inputsData), {'id':inputId}, true)[0];

            var newName = autoIncrementName(inputSelected.name);
            inputSelected.name = newName;

            var inputsList = getFragmentsNames(vm.inputsData);

            var duplicateInputData = {
              'fragmentData': inputSelected,
              'fragmentNamesList': inputsList,
              'texts': {
                'title': '_INPUT_WINDOW_DUPLICATE_TITLE_'
              }
            };

            setDuplicatetedInput('sm', duplicateInputData);
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
              fragmentTemplates: function (TemplateFactory) {
                return TemplateFactory.getNewFragmentTemplate(newInputTemplateData.fragmentType);
              }
            }
          });

          modalInstance.result.then(function (newInputData) {
            vm.inputsData.push(newInputData);
            vm.getInputTypes(vm.inputsData);
          }, function () {
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
                   fragmentTemplates: function (TemplateFactory) {
                      return TemplateFactory.getNewFragmentTemplate(editInputData.fragmentSelected.fragmentType);
                   },
                   policiesAffected: function (PolicyFactory) {
                      return PolicyFactory.getPolicyByFragmentId(editInputData.fragmentSelected.fragmentType, editInputData.fragmentSelected.id);
                   }
               }
           });

          modalInstance.result.then(function (updatedInputData) {
            vm.inputsData[updatedInputData.index] = updatedInputData.data;
            vm.getInputTypes(vm.inputsData);

              },function () {
                     });
        };

        function deleteInputConfirm(size, input) {
          var modalInstance = $modal.open({
            animation: true,
            templateUrl: 'templates/components/st-delete-modal.tpl.html',
            controller: 'DeleteFragmentModalCtrl as vm',
            size: size,
            resolve: {
                item: function () {
                    return input;
                },
                policiesAffected: function (PolicyFactory) {
                  return PolicyFactory.getPolicyByFragmentId(input.type, input.id);
                }
            }
          });

          modalInstance.result.then(function (selectedItem) {
            vm.inputsData.splice(selectedItem.index, 1);
            vm.getInputTypes(vm.inputsData);
          },function () {
          });
        };

        function setDuplicatetedInput(size, InputData) {
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

          modalInstance.result.then(function (newInput) {
            vm.inputsData.push(newInput);
            vm.getInputTypes(vm.inputsData);
          },function () {
          });
        };
    };
})();
