(function() {
  'use strict';

    /*DUPLICATE INPUT MODAL CONTROLLER */
    angular
        .module('webApp')
        .controller('DuplicateFragmentModalCtrl', DuplicateFragmentModalCtrl);

    DuplicateFragmentModalCtrl.$inject = ['$modalInstance', 'item', 'FragmentFactory', '$filter'];

    function DuplicateFragmentModalCtrl($modalInstance, item, FragmentFactory, $filter) {
        /*jshint validthis: true*/
        var vm = this;

        vm.ok = ok;
        vm.cancel = cancel;
        vm.error = false;
        vm.errorText = '';

        init();

        ///////////////////////////////////////

        function init () {
            console.log('*********Modal');
            console.log(item);
            setTexts(item.texts);
            vm.fragmentData = item.fragmentData;
            console.log(vm.fragmentData);
        };

        function setTexts(texts) {
          vm.modalTexts = {};
          vm.modalTexts.title = texts.title;
        };

        function ok() {
            if (vm.form.$valid){
                checkFragmnetname();
            }
        };

        function checkFragmnetname() {
          var fragmentNamesExisting = [];
          console.log(vm.fragmentData);
          var newFragmentName = vm.fragmentData.name.toLowerCase();
          fragmentNamesExisting = $filter('filter')(item.fragmentNamesList, {'name': newFragmentName}, true);

          if (fragmentNamesExisting.length > 0) {
            vm.error = true;
            vm.errorText = "_INPUT_ERROR_100_";
          }
          else {
            createfragment();
          }
        };

        function createfragment() {
            delete vm.fragmentData['id'];
            var newFragment = FragmentFactory.CreateFragment(vm.fragmentData);

            newFragment.then(function (result) {
                console.log('*********Fragment duplicated');
                console.log(result);
                $modalInstance.close(result);

            },function (error) {
                vm.error = true;
                vm.errorText = "_INPUT_ERROR_" + error.data.i18nCode + "_";
            });
        };

        function cancel() {
            $modalInstance.dismiss('cancel');
        };
    };
})();
