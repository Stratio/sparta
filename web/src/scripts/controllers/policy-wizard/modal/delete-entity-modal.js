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

  /*DELETE POLICIES MODAL CONTROLLER */
  angular
    .module('webApp')
    .controller('DeleteEntityModalCtrl', DeleteEntityModalCtrl);

  DeleteEntityModalCtrl.$inject = ['$uibModalInstance', 'item', 'title', 'type', 'EntityFactory'];

  function DeleteEntityModalCtrl($uibModalInstance, item, title, type, EntityFactory) {
    /*jshint validthis: true*/
    var vm = this;

    vm.ok = ok;
    vm.cancel = cancel;
    vm.error = false;
    vm.title = title;

    init();

    ///////////////////////////////////////

    function init() {
      vm.fileName = item;
    }

    function ok() {
      var promise;

      switch (type) {
        case 'PLUGIN':
          promise = EntityFactory.deletePlugin(vm.fileName);
          break;
        case 'DRIVER':
          promise = EntityFactory.deleteDriver(vm.fileName);
          break;
        case 'BACKUP':
          promise = EntityFactory.deleteBackup(vm.fileName);
          break;
        case 'ALL_BACKUPS':
          promise = EntityFactory. deleteAllBackups();
          break;
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
