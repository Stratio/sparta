(function() {
    'use strict';

    angular.module('webApp')
    .controller('NuevoCtrl',['ApiTest', '$filter', function (ApiTest, $filter) {

        this.init = function (){
            var vm = this;
            this.getInputs();
        };

        this.getInputs = function(){
            var vm = this;

            vm.inputs = ApiTest.get();
            vm.inputs.$promise.then(function (result) {
                vm.inputsData = result;
                vm.getInputTypes(vm.inputsData);
            });
        };

        this.getInputTypes = function(inputs){
            var vm = this;
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

    }]);
})();