(function() {
  'use strict';

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
            var newFragment = FragmentFactory.GetFragmentById(inputType, inputName);

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
