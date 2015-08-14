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
        vm.getInputTypes = getInputTypes
        vm.createInput = createInput;
        vm.deleteInputConfirm = deleteInputConfirm
        vm.inputTypes = [];

        init();

        /////////////////////////////////

        function init() {
            getInputs();
        };

        function getInputs() {
            ApiTest.get().$promise.then(function (result) {
                vm.inputsData = result;
                vm.getInputTypes(result);
            });
        };

        function deleteInput(inputType, inputName) {
            var policiesToDelete = FragmentDataService.GetPolicyByFragmentName(inputType, inputName);

            policiesToDelete.then(function (result) {
                console.log('*********Controller');
                console.log(result);
                vm.deleteInputConfirm('lg', result);
                //vm.deleteModal = true;
            });
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
                templateUrl: 'views/myModalContent.html',
                controller: 'ModalInstanceCtrl',
                size: size,
                resolve: {
                    items: function () {
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

        function deleteInputConfirm(size, policies) {
            var modalInstance = $modal.open({
                animation: true,
                templateUrl: 'templates/components/st_delete_modal.tpl.html',
                controller: 'ModalInstanceCtrl',
                size: size,
                resolve: {
                    items: function () {
                        return policies;
                    }
                }
            });


            modalInstance.result.then(function (selectedItem) {
                /*$scope.selected = selectedItem;*/
                console.log(selectedItem);

            }, function () {
                console.log('Modal dismissed at: ' + new Date())
                /*$log.info('Modal dismissed at: ' + new Date());*/
            });

        };

    };


    angular.module('webApp').controller('ModalInstanceCtrl', function ($scope, $modalInstance, items) {
        console.log('*********Modal');
        console.log(items);
        $scope.items = items;

/*
        $scope.items = items;
        $scope.selected = {
            item: $scope.items[0]
        };
*/
        $scope.ok = function () {
            /*$modalInstance.close($scope.selected.item);*/
            $modalInstance.close('accepted');
        };

        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };

    });

})();