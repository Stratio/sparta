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
    .controller('PluginsListCtrl', PluginsListCtrl);

  PluginsListCtrl.$inject = ['$scope', 'EntityFactory', 'ModalService', 'UtilsService', '$state'];

  function PluginsListCtrl($scope, EntityFactory, ModalService, UtilsService, $state) {
    /*jshint validthis: true*/
    var vm = this;
    vm.deletePlugin = deletePlugin;
    vm.getAllPlugins = getAllPlugins;
    vm.createPlugin = createPlugin;
    vm.sortPlugins = sortPlugins;
    vm.tableReverse = false;
    vm.sortField = 'fileName';
    vm.selectedOption = 'PLUGINS';
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
    vm.menuOptions = [{
      text: '_MENU_DASHBOARD_PLUGINS_',
      isDisabled: false,
      name: 'PLUGINS'
    }, {
      text: '_MENU_DASHBOARD_DRIVERS_',
      isDisabled: false,
      name: 'DRIVERS'
    }];

    init();

    /////////////////////////////////

    function init() {
      vm.getAllPlugins();
    }

    function getAllPlugins() {
      EntityFactory.getAllPlugins().then(function (plugins) {
        vm.pluginsData = plugins;
      });
    }

    function createPlugin() {
      var controller = 'CreateEntityModalCtrl';
      var templateUrl = "templates/modal/entity-creation-modal.tpl.html";
      var resolve = {
        type: function () {
          return "PLUGIN";
        },
        title: function () {
          return "_ENTITY_._CREATE_PLUGIN_TITLE_";
        },
        info: function () {
          return "_PLUGIN_INFO_";
        },
        text: function () {
          return "_PLUGIN_TEXT_";
        }
      };
      var modalInstance = ModalService.openModal(controller, templateUrl, resolve, '', 'lg');

      return modalInstance.result.then(function () {
        getAllPlugins();
        vm.successMessage.text = '_PLUGIN_CREATE_OK_';
      });
    }

    function deletePlugin(fileName) {
      return deletePluginConfirm('lg', fileName);
    }

    function deletePluginConfirm(size, fileName) {
      var controller = 'DeleteEntityModalCtrl';
      var templateUrl = "templates/modal/entity-delete-modal.tpl.html";
      var resolve = {
        item: function () {
          return fileName;
        },
        type: function () {
          return "PLUGIN";
        },
        title: function () {
          return "_ENTITY_._DELETE_PLUGIN_TITLE_";
        }
      };
      var modalInstance = ModalService.openModal(controller, templateUrl, resolve, '', size);

      return modalInstance.result.then(function (fileName) {
        var index = UtilsService.getArrayElementPosition(vm.pluginsData, 'fileName', fileName);
        vm.pluginsData.splice(index, 1);
        vm.successMessage.text = '_PLUGIN_DELETE_OK_';
      });
    }

    function sortPlugins(fieldName) {
      if (fieldName == vm.sortField) {
        vm.tableReverse = !vm.tableReverse;
      } else {
        vm.tableReverse = false;
        vm.sortField = fieldName;
      }
    }


    $scope.$on("newTabOptionValue", function (event, value) {
      $state.go("dashboard.resources.drivers");
    });

  }
})();
