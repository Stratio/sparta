/*
 * Copyright (C) 2015 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
(function() {
  'use strict';

    /*DUPLICATE INPUT MODAL CONTROLLER */
    angular
        .module('webApp')
        .controller('DuplicateFragmentModalCtrl', DuplicateFragmentModalCtrl);

    DuplicateFragmentModalCtrl.$inject = ['$uibModalInstance', 'fragmentTemplate', 'FragmentFactory', 'FragmentService'];

    function DuplicateFragmentModalCtrl($uibModalInstance, fragmentTemplate, FragmentFactory, FragmentService) {
        /*jshint validthis: true*/
        var vm = this;

        vm.ok = ok;
        vm.cancel = cancel;
        vm.error = false;
        vm.errorText = '';

        init();

        ///////////////////////////////////////

        function init () {
            setTexts(fragmentTemplate.texts);
            vm.fragmentData = fragmentTemplate.fragmentData;
        }

        function setTexts(texts) {
          vm.modalTexts = {};
          vm.modalTexts.title = texts.title;
        }

        function ok() {
            if (vm.form.$valid){
              if (FragmentService.isValidFragmentName(vm.fragmentData, fragmentTemplate)){
                createFragment();
              }else {
                vm.error = true;
                vm.errorText = "_ERROR_._100_";
              }
              
            }
        }

        function createFragment() {
            delete vm.fragmentData['id'];
            var newFragment = FragmentFactory.createFragment(vm.fragmentData);

            newFragment.then(function (result) {
                $uibModalInstance.close(result);

            },function (error) {
                vm.error = true;
                vm.errorText = "_ERROR_._" + error.data.i18nCode + "_";
            });
        }

        function cancel() {
            $uibModalInstance.dismiss('cancel');
        }
    }
})();
