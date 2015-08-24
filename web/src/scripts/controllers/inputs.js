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
       vm.setInputsId = setInputsId;
       vm.inputTypes = [];
       vm.inputId = 1;

       init();

        /////////////////////////////////

        function init() {
            getInputs();
        };

        function getInputs() {
            var inputList = FragmentFactory.GetFragments("input");

            inputList.then(function (result) {
                vm.inputsData = vm.setInputsId(result);
                vm.getInputTypes(result);
            });
        };

        function deleteInput(fragmentType, fragmentName, index) {
            console.log('--> Deleting input');
            console.log('> Getting Policies affected');
            var policiesToDelete = PolicyFactory.GetPolicyByFragmentName(fragmentType, fragmentName);

            policiesToDelete.then(function (result) {
                console.log(result);

                var policies = vm.getPolicyNames(result);
                var inputToDelete =
                {
                    'type':fragmentType,
                    'name': fragmentName,
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
           var inputFragmentTemplate = TemplateFactory.GetNewFragmentTemplate('input');

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

        function editInput(inputType, inputName, index) {
           var inputSelected = $filter('filter')(angular.copy(vm.inputsData), {name:inputName}, true)[0];
           var id = inputSelected.id;

           delete inputSelected["id"];

           var inputFragmentTemplate = TemplateFactory.GetNewFragmentTemplate('input');

           inputFragmentTemplate.then(function (result) {
                console.log('--> Editing input');
                console.log('> Getting Fragment Template');
                console.log(result);
                console.log('> Getting Policies affected');
                var policiesAffected = PolicyFactory.GetPolicyByFragmentName(inputType, inputName);
                var inputDataTemplate = result;

                policiesAffected.then(function (result) {
                    console.log(result);

                    var policies = vm.getPolicyNames(result);
                    var editInputData = {
                        'index': index,
                        'id': id,
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

        function setInputsId(inputsData) {
            for (var i=0; i < inputsData.length; i++) {
                inputsData[i].id = vm.inputId;
                vm.inputId++;
            }
            return inputsData;
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

                           var newId = vm.inputsData[vm.inputsData.length-1].id + 1;
                           result.id = newId;

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

                           result.id = updatedInputData.id;

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
                templateUrl: 'templates/components/st_delete_modal.tpl.html',
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
                    var fragmentDeleted = FragmentFactory.DeleteFragment(selectedItem.type, selectedItem.name);

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
                templateUrl: 'templates/components/st_duplicate_modal.tpl.html',
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
                    delete selectedItem["id"];
                    console.log(selectedItem);

                    var newFragment = FragmentFactory.CreateFragment(selectedItem);

                    newFragment
                        .then(function (result) {
                            console.log('*********Fragment created');
                            console.log(result);

                            var newId = vm.inputsData[vm.inputsData.length-1].id + 1;
                            selectedItem.id = newId;

                            vm.inputsData.push(selectedItem);
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

    /*NEW & EDIT FRAGMENT MODAL CONTROLLER*/
    angular
        .module('webApp')
        .controller('NewFragmentModalCtrl', NewFragmentModalCtrl);

    NewFragmentModalCtrl.$inject = ['$modalInstance', 'item', 'FragmentFactory'];

    function NewFragmentModalCtrl($modalInstance, item, FragmentFactory) {
        /*jshint validthis: true*/
        var vm = this;

        vm.setProperties = setProperties;
        vm.ok = ok;
        vm.cancel = cancel;
        vm.initFragmentObject = initFragmentObject;
        vm.setFragmentData = setFragmentData;
        vm.createTypeModels = createTypeModels;
        vm.dataSource = {};
        vm.dataSource.element = {};
        vm.templateInputsData = [];
        vm.properties = [];
        vm.error = false;

        init();

        /////////////////////////////////

        function init() {
            console.log('--> NewFragmentModalCtrl');
            console.log('> Data received');
            console.log(item);

            setTexts(item.texts);

            vm.action = item.action;

            if (vm.action === 'edit') {
                vm.templateInputsData = item.inputDataTemplate;
                vm.dataSource = item.inputSelected;
                vm.createTypeModels(vm.templateInputsData);
                vm.selectedIndex = vm.index;
                vm.policiesAffected = item.policies;
            }
            else {
                vm.templateInputsData = item.inputDataTemplate;
                vm.initFragmentObject(vm.templateInputsData);
                vm.createTypeModels(vm.templateInputsData);
                vm.selectedIndex = 0;
            }
        };

        function setTexts(texts) {
            vm.modalTexts = {};
            vm.modalTexts.title = texts.title;
            vm.modalTexts.button = texts.button;
            vm.modalTexts.icon = texts.button_icon;
        }

        function initFragmentObject(fragmentData) {
            /*Init fragment*/
            vm.dataSource.fragmentType = 'input';
            vm.dataSource.name = '';

            /*Init fragment.element*/
            vm.dataSource.element.type = fragmentData[0].name;
            vm.dataSource.element.name = 'in-' + vm.dataSource.element.type;

            /*Init fragment.element.configuration*/
            vm.dataSource.element.configuration = {};

            vm.setFragmentData(0);
        };

        function createTypeModels(fragmentData) {
            /*Create one properties model for each input type*/
            for (var i=0; i<fragmentData.length; i++){
                vm.properties[fragmentData[i].name] = {};

                for (var j=0; j<fragmentData[i].properties.length; j++) {
                    if (fragmentData[i].properties[j].propertyId !== 'name' && fragmentData[i].properties[j].propertyId !== 'type'){
                        vm.properties[fragmentData[i].name][fragmentData[i].properties[j].propertyId] = '';
                    }
                }

                /*Init properties*/
                if(i === 0 && vm.action !== 'edit') {
                    vm.dataSource.element.configuration = vm.properties[fragmentData[i].name];
                }

                else if(vm.action === 'edit' && fragmentData[i].name === vm.dataSource.element.type) {
                    vm.properties[fragmentData[i].name] = vm.dataSource.element.configuration;
                    vm.dataSource.element.configuration = vm.properties[fragmentData[i].name];
                    vm.index = i;
                }
            }
        };

        function setFragmentData(index) {
            /*Set fragment*/
            vm.dataSource.description = vm.templateInputsData[index].description.long;
            vm.dataSource.shortDescription = vm.templateInputsData[index].description.short;
            vm.dataSource.icon = vm.templateInputsData[index].icon.url;
            vm.dataSource.element.name = 'in-' + vm.dataSource.element.type;
        };

        function setProperties(index, inputName) {
            vm.selectedIndex = index;
            vm.dataSource.element.configuration = vm.properties[inputName];
            vm.setFragmentData(index);
        };

        function ok() {
            if (vm.form.$valid){
                checkInputName(vm.dataSource.fragmentType, vm.dataSource.name);
            }
        };

        function checkInputName(inputType, inputName) {
            var newFragment = FragmentFactory.GetFragmentByName(inputType, inputName);

            newFragment
            .then(function (result) {
                vm.error = true;
            },
            function (error) {
                var callBackData = {
                    'index': item.index,
                    'id': item.id,
                    'data': vm.dataSource,
                };
               $modalInstance.close(callBackData);
            });
        };

        function cancel() {
            $modalInstance.dismiss('cancel');
        };
    };

    /*DELETE INPUT MODALS CONTROLLER */
    angular
        .module('webApp')
        .controller('DeleteFragmentModalCtrl', DeleteFragmentModalCtrl);

    DeleteFragmentModalCtrl.$inject = ['$scope', '$modalInstance', 'item'];

    function DeleteFragmentModalCtrl($scope, $modalInstance, item) {
        console.log('*********Modal');
        console.log(item);

        $scope.inputs = item;

        $scope.ok = function () {
            $modalInstance.close($scope.inputs);
        };

        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };
    };


    /*DUPLICATE INPUT MODAL CONTROLLER */
    angular
        .module('webApp')
        .controller('DuplicateFragmentModalCtrl', DuplicateFragmentModalCtrl);

    DuplicateFragmentModalCtrl.$inject = ['$modalInstance', 'item', 'FragmentFactory'];

    function DuplicateFragmentModalCtrl($modalInstance, item, FragmentFactory) {
        /*jshint validthis: true*/
        var vm = this;

        vm.ok = ok;
        vm.cancel = cancel;
        vm.error = false;

        init();

        ///////////////////////////////////////

        function init () {
            console.log('*********Modal');
            console.log(item);
            vm.inputData = item;
        };

        function ok() {
            if (vm.form.$valid){
                checkInputName(vm.inputData.fragmentType, vm.inputData.name);
            }
        };

        function checkInputName(inputType, inputName) {
            var newFragment = FragmentFactory.GetFragmentByName(inputType, inputName);

            newFragment
            .then(function (result) {
                vm.error = true;
            },
            function (error) {
                $modalInstance.close(vm.inputData);
            });
        };

        function cancel() {
            $modalInstance.dismiss('cancel');
        };
    };
})();
