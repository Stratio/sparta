(function() {
    'use strict';

    angular
        .module('webApp')
        .controller('NuevoCtrl', NuevoCtrl);

    NuevoCtrl.$inject = ['ApiTest', 'FragmentDataService', '$filter', '$modal'];

    function NuevoCtrl(ApiTest, FragmentDataService, $filter, $modal) {
        /*jshint validthis: true*/
        var vm = this;

        vm.getInputs = getInputs;
        vm.deleteInput = deleteInput;
        vm.getInputTypes = getInputTypes;
        vm.createInput = createInput;
        vm.deleteInputConfirm = deleteInputConfirm;
        vm.getPoliciesNames = getPoliciesNames;
        vm.setInputsId = setInputsId;
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
                console.log(vm.inputsData);
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

        function createInput(size) {
            var modalInstance = $modal.open({
                animation: true,
                templateUrl: 'templates/inputs/input-details.tpl.html',
                controller: 'ModalInstanceCtrl',
                size: size,
                resolve: {
                    item: function () {
                        /*return $scope.items;*/
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
                            /*Check deleting the input in the array*/
                            vm.inputsData.splice(selectedItem.index, 1);

                        });
                },
                function () {
                    console.log('Modal dismissed at: ' + new Date())
                });
        };
    };


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
        
        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };
    };
})();