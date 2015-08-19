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
        vm.modifyInput = modifyInput;
        vm.createInputModal = createInputModal;
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

        function createInput() {
            var inputFragmentTemplate = TemplateDataService.GetNewFragmentTemplate('input');

            inputFragmentTemplate.then(function (result) {
                console.log('*********Controller');
                console.log(result);

                vm.createInputModal(result);
            });
        };

        function modifyInput() {
            var inputFragmentTemplate = TemplateDataService.GetNewFragmentTemplate('input');

            inputFragmentTemplate.then(function (result) {
                console.log('*********Controller');
                console.log(result);

                //vm.createInputModal(result);
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

                    var newFragment = FragmentDataService.InsertFragment(newInputData);

                    newFragment
                        .then(function (result) {
                            console.log('*********Fragment created');
                            console.log(result);

                            var newId = vm.inputsData[vm.inputsData.length-1].id + 1;
                            newInputData.id = newId;

                            vm.inputsData.push(newInputData);
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

    /*NEW FRAGMENT MODAL CONTROLLER*/
    angular
        .module('webApp')
        .controller('NewFragmentModalCtrl', NewFragmentModalCtrl);

    NewFragmentModalCtrl.$inject = ['$modalInstance', 'item'];

    function NewFragmentModalCtrl($modalInstance, item) {
        /*jshint validthis: true*/
        var vm = this;

        vm.setProperties = setProperties;
        vm.ok = ok;
        vm.cancel = cancel;
        vm.templateInputsData = [];
        vm.initFragmentObecjt = initFragmentObecjt;
        vm.setFragmentData = setFragmentData;
        vm.dataSource = {};
        init();

        /////////////////////////////////

        function init() {
            console.log('*********Modal');
            console.log(item);

            vm.templateInputsData = item;
            vm.initFragmentObecjt(vm.templateInputsData);

            vm.selectedIndex = 0;
        };

        function initFragmentObecjt(fragmentData) {
            /*Init fragment*/
            vm.dataSource.fragmentType = 'input';
            vm.dataSource.name = '';

            /*Init fragment.element*/
            vm.dataSource.element = {};
            vm.dataSource.element.type = fragmentData[0].name;
            vm.dataSource.element.name = 'in-' + vm.dataSource.element.type;

            /*Init fragment.element.configuration*/
            vm.dataSource.element.configuration = {};

            vm.properties = [];

            /*Create one properties model for each input type*/
            for (var i=0; i<fragmentData.length; i++){
                vm.properties[fragmentData[i].name]={};
                for (var j=0; j<fragmentData[i].properties.length; j++) {
                    if (fragmentData[i].properties[j].propertyId !== 'name' && fragmentData[i].properties[j].propertyId !== 'type'){
                        vm.properties[fragmentData[i].name][fragmentData[i].properties[j].propertyId]='';
                    }
                }
                /*Init properties*/
                if (i === 0) {
                    vm.dataSource.element.configuration = vm.properties[fragmentData[i].name];
                }
            }

            vm.setFragmentData(0);
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
            console.log(vm.dataSource);
            $modalInstance.close(vm.dataSource);
        };

        function cancel() {
            $modalInstance.dismiss('cancel');
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