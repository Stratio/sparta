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
    .controller('DriversListCtrl', DriversListCtrl);

  DriversListCtrl.$inject = ['$scope', 'EntityFactory', 'ModalService', 'UtilsService', '$state'];

  function DriversListCtrl($scope, EntityFactory, ModalService, UtilsService, $state) {
    /*jshint validthis: true*/
    var vm = this;
    vm.deleteDriver = deleteDriver;
    vm.getAllDrivers = getAllDrivers;
    vm.createDriver = createDriver;
    vm.sortDrivers = sortDrivers;
    vm.tableReverse = false;
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
      getAllDrivers();
    }

    function getAllDrivers() {
      EntityFactory.getAllDrivers().then(function (drivers) {
        vm.driversData = drivers;
      });
    }

    function createDriver() {
      var controller = 'CreateEntityModalCtrl';
      var templateUrl = "templates/modal/entity-creation-modal.tpl.html";
      var resolve = {
        type: function () {
          return "DRIVER";
        },
        title: function () {
          return "_ENTITY_._CREATE_DRIVER_TITLE_";
        },
        info: function () {
          return "_DRIVER_INFO_";
        },
        text: function () {
          return "_DRIVER_TEXT_";
        },
      };
      var modalInstance = ModalService.openModal(controller, templateUrl, resolve, '', 'lg');

      return modalInstance.result.then(function () {
        getAllDrivers();
        vm.successMessage.text = '_DRIVER_CREATE_OK_';
      });
    }

    function deleteDriver(fileName) {
      return deleteDriverConfirm('lg', fileName);
    }

    function deleteDriverConfirm(size, fileName) {
      var controller = 'DeleteEntityModalCtrl';
      var templateUrl = "templates/modal/entity-delete-modal.tpl.html";
      var resolve = {
        item: function () {
          return fileName;
        },
        type: function () {
          return "DRIVER";
        },
        title: function () {
          return "_ENTITY_._DELETE_DRIVER_TITLE_";
        }
      };
      var modalInstance = ModalService.openModal(controller, templateUrl, resolve, '', size);

      return modalInstance.result.then(function (fileName) {
        var index = UtilsService.getArrayElementPosition(vm.driversData, 'fileName', fileName);
        vm.driversData.splice(index, 1);
        vm.successMessage.text = '_DRIVER_DELETE_OK_';
      });
    }

    function sortDrivers(fieldName) {
      if (fieldName == vm.sortField) {
        vm.tableReverse = !vm.tableReverse;
      } else {
        vm.tableReverse = false;
        vm.sortField = fieldName;
      }
    }

  }
})();
