(function() {
    'use strict';

    angular
        .module('webApp')
        .controller('InputsCtrl', InputsCtrl);

    InputsCtrl.$inject = ['FragmentFactory', 'PolicyFactory', 'TemplateFactory', '$filter', '$modal'];

    function InputsCtrl(FragmentFactory, PolicyFactory, TemplateFactory, $filter, $modal) {
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
       vm.inputTypes = [];

       init();

        /////////////////////////////////

        function init() {
            getInputs();
        };

        function getInputs() {
            var inputList = FragmentFactory.GetFragments("input");

            inputList.then(function (result) {
                vm.inputsData = result;
                vm.getInputTypes(result);
                console.log(vm.inputsData);
            });
        };

        function deleteInput(fragmentType, fragmentId, index) {
            console.log('--> Deleting input');
            console.log('> Getting Policies affected');
            var policiesToDelete = PolicyFactory.GetPolicyByFragmentId(fragmentType, fragmentId);

            policiesToDelete.then(function (result) {
                console.log(result);

                var policies = vm.getPolicyNames(result);
                var inputToDelete =
                {
                    'type':fragmentType,
                    'id': fragmentId,
                    'policies': policies,
                    'index': index
                };
                vm.deleteInputConfirm('lg', inputToDelete);
            },
            function (error) {
              console.log('#ERROR#');
              console.log(error);
            });
        };

        function duplicateInput(inputName) {
            var inputSelected = $filter('filter')(angular.copy(vm.inputsData), {name:inputName}, true)[0];

            var newName = autoIncrementName(inputSelected.name);
            inputSelected.name = newName;

            var newName = SetDuplicatetedInput('sm', inputSelected);
       };

        function createInput() {
           var inputFragmentTemplate = TemplateFactory.GetNewFragmentTemplate('inputs');

           inputFragmentTemplate.then(function (result) {
                console.log('*********Controller');
                console.log(result);

                var createInputData = {
                    'action': 'create',
                    'inputDataTemplate': result,
                    'texts': {
                        'title': '_INPUT_WINDOW_NEW_TITLE_',
                        'button': '_INPUT_WINDOW_NEW_BUTTON_',
                        'button_icon': 'icon-circle-plus'
                    }
                };

               vm.createInputModal(createInputData);
           });
        };

        function editInput(inputType, inputName, inputId, index) {
           var inputSelected = $filter('filter')(angular.copy(vm.inputsData), {name:inputName}, true)[0];

           var inputFragmentTemplate = TemplateFactory.GetNewFragmentTemplate('inputs');

           inputFragmentTemplate.then(function (result) {
                console.log('--> Editing input');
                console.log('> Getting Fragment Template');
                console.log(result);
                console.log('> Getting Policies affected');
                var policiesAffected = PolicyFactory.GetPolicyByFragmentId(inputType, inputId);
                var inputDataTemplate = result;

                policiesAffected.then(function (result) {
                    console.log(result);

                    var policies = vm.getPolicyNames(result);
                    var editInputData = {
                        'index': index,
                        'action': 'edit',
                        'inputSelected': inputSelected,
                        'inputDataTemplate': inputDataTemplate,
                        'policies': policies,
                        'texts': {
                            'title': '_INPUT_WINDOW_MODIFY_TITLE_',
                            'button': '_INPUT_WINDOW_MODIFY_BUTTON_',
                            'button_icon': 'icon-circle-check'
                        }
                    };

                    vm.editInputModal(editInputData);
                },
                function (error) {
                  console.log('#ERROR#');
                  console.log(error);
                });
           });
        };

        function getPolicyNames(policiesData) {
            var policies = [];

            for (var i=0; i<policiesData.length; i++){
                policies.push(policiesData[i].name);
            }

            return policies;
        };

        function getInputTypes(inputs) {
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
               templateUrl: 'templates/inputs/input-details.tpl.html',
               controller: 'NewFragmentModalCtrl as vm',
               size: 'lg',
               resolve: {
                   item: function () {
                       return newInputTemplateData;
                   }
               }
            });

            modalInstance.result.
               then(function (newInputData) {
                   console.log('*************** Controller back');
                   console.log(newInputData);

                   var newFragment = FragmentFactory.CreateFragment(newInputData.data);

                   newFragment
                       .then(function (result) {
                           console.log('*********Fragment created');
                           console.log(result);

                           vm.inputsData.push(result);
                           console.log(vm.inputsData);
                       },
                       function (error) {
                           console.log(error);
                           console.log('Modal dismissed at: ' + new Date())
                       });

               }, function () {
                   console.log('Modal dismissed at: ' + new Date())
               });
            };

        function editInputModal(editInputData) {
           var modalInstance = $modal.open({
               animation: true,
               templateUrl: 'templates/inputs/input-details.tpl.html',
               controller: 'NewFragmentModalCtrl as vm',
               size: 'lg',
               resolve: {
                   item: function () {
                       return editInputData;
                   }
               }
           });

           modalInstance.result.
               then(function (updatedInputData) {
                   console.log('*************** Controller back');
                   console.log(updatedInputData);

                   var updatedFragment = FragmentFactory.UpdateFragment(updatedInputData.data);

                   updatedFragment
                       .then(function (result) {
                           console.log('*********Fragment updated');
                           console.log(result);

                           vm.inputsData[updatedInputData.index] = result;
                           console.log(vm.inputsData);
                       },
                       function (error) {
                           console.log(error);
                           console.log('Modal dismissed at: ' + new Date())
                       });

               }, function () {
                   console.log('Modal dismissed at: ' + new Date())
               });
        };

        function deleteInputConfirm(size, input) {
            var modalInstance = $modal.open({
                animation: true,
                templateUrl: 'templates/components/st-delete-modal.tpl.html',
                controller: 'DeleteFragmentModalCtrl',
                size: size,
                resolve: {
                    item: function () {
                        return input;
                    }
                }
            });

            modalInstance.result
                .then(function (selectedItem) {
                    console.log(selectedItem);
                    var fragmentDeleted = FragmentFactory.DeleteFragment(selectedItem.type, selectedItem.id);

                    fragmentDeleted
                        .then(function (result) {
                            console.log('*********Fragment deleted');
                            vm.inputsData.splice(selectedItem.index, 1);

                        });
                },
                function () {
                    console.log('Modal dismissed at: ' + new Date())
                });
        };

        function SetDuplicatetedInput(size, inputName) {
            var modalInstance = $modal.open({
                animation: true,
                templateUrl: 'templates/components/st-duplicate-modal.tpl.html',
                controller: 'DuplicateFragmentModalCtrl as vm',
                size: 'lg',
                resolve: {
                    item: function () {
                        return inputName;
                    }
                }
            });

            modalInstance.result
                .then(function (selectedItem) {
                    console.log(selectedItem);
                    delete selectedItem['id'];

                    var newFragment = FragmentFactory.CreateFragment(selectedItem);

                    newFragment
                        .then(function (result) {
                            console.log('*********Fragment created');
                            console.log(result);

                            vm.inputsData.push(result);
                            console.log(vm.inputsData);
                        },
                        function (error) {
                            console.log(error);
                            console.log('Modal dismissed at: ' + new Date())
                        });

                },
                function () {
                    console.log('Modal dismissed at: ' + new Date())
                });
        };
    };
})();
