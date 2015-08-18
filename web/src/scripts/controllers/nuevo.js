(function() {
    'use strict';

    angular
        .module('webApp')
        .controller('NuevoCtrl', NuevoCtrl);

    NuevoCtrl.$inject = ['ApiTest', 'FragmentDataService', 'TemplateDataService', '$filter', '$modal'];

    function NuevoCtrl(ApiTest, FragmentDataService, TemplateDataService, $filter, $modal) {
        /*jshint validthis: true*/
        var vm = this;

        vm.getInputs = getInputs;
        vm.deleteInput = deleteInput;
        vm.getInputTypes = getInputTypes;
        vm.createInput = createInput;
        vm.createInputModal = createInputModal;
        vm.deleteInputConfirm = deleteInputConfirm;
        vm.getPoliciesNames = getPoliciesNames;
        vm.setInputsId = setInputsId;
        vm.duplicateInput = duplicateInput;
        vm.inputTypes = [];
        vm.inputId = 1;

        init();

        /////////////////////////////////

        function init() {
            getInputs();
        };

        function getInputs() {
            ApiTest.get().$promise.then(function (result) {
                vm.inputsData = vm.setInputsId(result);
                vm.getInputTypes(result);
            });
        };

        function deleteInput(fragmentType, fragmentName, index) {
            console.log(index);
            var policiesToDelete = FragmentDataService.GetPolicyByFragmentName(fragmentType, fragmentName);

            policiesToDelete.then(function (result) {
                console.log('*********Controller');
                console.log(result);

                var policies = vm.getPoliciesNames(result);
                var inputToDelete =
                {
                    'type':fragmentType,
                    'name': fragmentName,
                    'policies': policies,
                    'index': index
                };
                vm.deleteInputConfirm('lg', inputToDelete);
            });
        };

        function duplicateInput(inputName) {
            var inputSelected = $filter('filter')(angular.copy(vm.inputsData), {name:inputName}, true)[0];

            var newName = autoIncrementName(inputSelected.name);
            inputSelected.name = newName;

            var newName = SetDuplicatetedInput('sm', inputSelected);
       };

        function createInput() {
            var inputFragmentTemplate = TemplateDataService.GetNewFragmentTemplate('input');

            inputFragmentTemplate.then(function (result) {
                console.log('*********Controller');
                console.log(result);

                vm.createInputModal(result);
            });
        };

        function setInputsId(inputsData) {
            for (var i=0; i < inputsData.length; i++) {
                inputsData[i].id = vm.inputId;
                vm.inputId++;
            }
            return inputsData;
        };

        function getPoliciesNames(policiesData) {
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
                controller: 'ModalInstanceCtrl',
                size: 'lg',
                resolve: {
                    item: function () {
                        return newInputTemplateData;
                    }
                }
            });
/*
            modalInstance.result.then(function (selectedItem) {
                $scope.selected = selectedItem;
            }, function () {
                console.log('Modal dismissed at: ' + new Date())
                $log.info('Modal dismissed at: ' + new Date());
            });
*/
        };

        function deleteInputConfirm(size, input) {
            var modalInstance = $modal.open({
                animation: true,
                templateUrl: 'templates/components/st_delete_modal.tpl.html',
                controller: 'ModalInstanceCtrl',
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
                    var fragmentDeleted = FragmentDataService.DeleteFragment(selectedItem.type, selectedItem.name);

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
                controller: 'NewFragmentCtrl',
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

                    var newFragment = FragmentDataService.InsertFragment(selectedItem);

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

    /*NEW INPUT & DELETE INPUT MODALS CONTROLLER */
    angular
        .module('webApp')
        .controller('ModalInstanceCtrl', ModalInstanceCtrl);

    ModalInstanceCtrl.$inject = ['$scope', '$modalInstance', 'item'];

    function ModalInstanceCtrl($scope, $modalInstance, item) {
        console.log('*********Modal');
        console.log(item);

        $scope.inputs = item;

        $scope.ok = function () {
            $modalInstance.close($scope.inputs);
        };

<<<<<<< HEAD
        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };
    };


    /*DUPLICATE INPUT MODAL CONTROLLER */
    angular
        .module('webApp')
        .controller('NewFragmentCtrl', NewFragmentCtrl);

    NewFragmentCtrl.$inject = ['$scope', '$modalInstance', 'item'];

    function NewFragmentCtrl($scope, $modalInstance, item) {
        console.log('*********Modal');
        console.log(item);

        $scope.inputData = item;

        $scope.ok = function () {
            $modalInstance.close(item);
        };

=======
>>>>>>> feature/new-input: getting template data from API
        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };
    };
})();