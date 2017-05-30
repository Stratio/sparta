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

  angular
    .module('webApp')
    .controller('GeneralSettingsCtrl', GeneralSettingsCtrl);

  GeneralSettingsCtrl.$inject = ['$scope', 'EntityFactory', 'ModalService', 'UtilsService', '$state'];

  function GeneralSettingsCtrl($scope, EntityFactory, ModalService, UtilsService, $state) {
    /*jshint validthis: true*/
    var vm = this;
    vm.deleteBackup = deleteBackup;
    vm.getAllBackups = getAllBackups;
    vm.createBackup = createBackup;
    vm.sortBackups = sortBackups;
    vm.tableReverse = false;
    vm.generateBackup = generateBackup;
    vm.downloadBackup = downloadBackup;
    vm.uploadBackup = uploadBackup;
    vm.deleteAllBackups = deleteAllBackups;
    vm.executeBackup = executeBackup;
    vm.sortField = 'fileName';
    vm.errorMessage = {
      type: 'error',
      text: '',
      internalTrace: ''
    };
    vm.successMessage = {
      type: 'success',
      text: '',
      internalTrace: ''
    };

    init();

    /////////////////////////////////

    function init() {
      getAllBackups();
    }

    function getAllBackups() {
      EntityFactory.getAllBackups().then(function (backups) {
        vm.backups = backups;
      });
    }

    function createBackup() {
      var controller = 'CreateEntityModalCtrl';
      var templateUrl = "templates/modal/entity-creation-modal.tpl.html";
      var resolve = {
        type: function () {
          return "BACKUP";
        },
        title: function () {
          return "_ENTITY_._CREATE_BACKUP_TITLE_";
        },
        info: function () {
          return "_BACKUP_INFO_";
        },
        text: function () {
          return "_BACKUP_TEXT_";
        },
      };
      var modalInstance = ModalService.openModal(controller, templateUrl, resolve, '', 'lg');

      modalInstance.result.then(function () {
        getAllBackups();
        vm.successMessage.text = '_BACKUP_CREATE_OK_';
      });
    }

    function generateBackup() {
      EntityFactory.buildBackup().then(function () {
        getAllBackups();
      });
    }

    function downloadBackup(fileName) {
      EntityFactory.downloadBackup(fileName).then(function (policyFile) {
        var data = "text/json;charset=utf-8," + encodeURIComponent(JSON.stringify(policyFile));
        var a = document.createElement('a');
        a.href = 'data:' + data;
        a.download = fileName;
        document.body.appendChild(a);
        a.click();
        a.remove();
      });
    }

    function deleteBackup(fileName) {
      deleteBackupConfirm('lg', fileName);
    }

    function deleteBackupConfirm(size, fileName) {
      var controller = 'DeleteEntityModalCtrl';
      var templateUrl = "templates/modal/entity-delete-modal.tpl.html";
      var resolve = {
        item: function () {
          return fileName;
        },
        type: function () {
          return "BACKUP";
        },
        title: function () {
          return "_ENTITY_._DELETE_BACKUP_TITLE_";
        }
      };
      var modalInstance = ModalService.openModal(controller, templateUrl, resolve, '', size);

      modalInstance.result.then(function (fileName) {
        var index = UtilsService.getArrayElementPosition(vm.backups, 'fileName', fileName);
        vm.backups.splice(index, 1);
        vm.successMessage.text = '_BACKUP_DELETE_OK_';
      });
    }

    function sortBackups(fieldName) {
      if (fieldName == vm.sortField) {
        vm.tableReverse = !vm.tableReverse;
      } else {
        vm.tableReverse = false;
        vm.sortField = fieldName;
      }
    }

    function uploadBackup(file) {
      EntityFactory.uploadBackup(file).then(function () {
        getAllBackups();
      });

    }

    function deleteAllBackups() {
      deleteAllBackupsConfirm('lg');
    }

    function deleteAllBackupsConfirm(size) {

      var controller = 'DeleteEntityModalCtrl';
      var templateUrl = "templates/modal/entity-delete-modal.tpl.html";
      var resolve = {
        item: function () {
          return "";
        },
        type: function () {
          return "ALL_BACKUPS";
        },
        title: function () {
          return "_ENTITY_._DELETE_ALL_BACKUPS_TITLE_";
        }
      };
      var modalInstance = ModalService.openModal(controller, templateUrl, resolve, '', size);

      modalInstance.result.then(function () {
        vm.successMessage.text = "_ALL_BACKUPS_DELETE_OK_";
        getAllBackups();
      });
    }

    function executeBackup(fileName){
      var controller = 'ExecuteBackupModalCtrl';
      var templateUrl = "templates/modal/execute-backup-modal.tpl.html";
      var resolve = {
        item: function () {
          return fileName;
        }
      };
      var modalInstance = ModalService.openModal(controller, templateUrl, resolve, '', 'lg');

      modalInstance.result.then(function () {
        getAllBackups();
        vm.successMessage.text = '_BACKUP_EXECUTION_OK_';
      });
    }

  }
})();
