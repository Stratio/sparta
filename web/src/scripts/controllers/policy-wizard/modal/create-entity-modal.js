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
(function () {
  'use strict';

  /*Create POLICIES MODAL CONTROLLER */
  angular
    .module('webApp')
    .controller('CreateEntityModalCtrl', CreateEntityModalCtrl);

  CreateEntityModalCtrl.$inject = ['$uibModalInstance', 'type', 'title', 'info', 'text', 'EntityFactory'];

  function CreateEntityModalCtrl($uibModalInstance, type, title, info, text, EntityFactory) {
    /*jshint validthis: true*/
    var vm = this;

    vm.validateForm = validateForm;
    vm.cancel = cancel;
    vm.info = info;
    vm.error = false;
    vm.text = text;

    init();

    ///////////////////////////////////////

    function init() {
      vm.title = title;
    }

    function validateForm() {
      var file = vm.file;
      if(!file){
        return;
      }
      var promise;
      if (type === "PLUGIN") {
        promise = EntityFactory.createPlugin(vm.file);
      } else {
        promise = EntityFactory.createDriver(vm.file);
      }
      return promise.then(function () {
        $uibModalInstance.close(vm.fileName);

      }, function (error) {
        vm.error = true;
        vm.errorText = "_ERROR_._" + error.data.i18nCode + "_";
      });
    }

    function cancel() {
      $uibModalInstance.dismiss('cancel');
    }
  }

})();
