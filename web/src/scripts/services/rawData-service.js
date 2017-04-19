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
    .service('RawDataService', RawDataService);

  RawDataService.$inject = ['PolicyModelFactory', 'ModalService', 'RawDataModelFactory', 'rawDataConstants', '$q'];

  function RawDataService(PolicyModelFactory, ModalService, RawDataModelFactory, rawDataConstants, $q) {
    var vm = this;
    var rawDataCreationStatus = {};
    var rawDataUpdateStatus = {};
    var rawDataContainer = null;
    var rawDataContainerType = "";
    var showHelpForSql = false;
    var policy = null;

    vm.showConfirmRemoveRawData = showConfirmRemoveRawData;
    vm.addRawData = addRawData;
    vm.saveRawData = saveRawData;
    vm.removeRawData = removeRawData;
    vm.isNewRawData = isNewRawData;
    vm.setRawDataContainer = setRawDataContainer;
    vm.getRawDataContainer = getRawDataContainer;
    vm.getRawDataContainerType = getRawDataContainerType;
    vm.isActiveRawDataCreationPanel = isActiveRawDataCreationPanel;
    vm.isActiveRawDataUpdatePanel = isActiveRawDataUpdatePanel;
    vm.activateRawDataCreationPanel = activateRawDataCreationPanel;
    vm.disableRawDataCreationPanel = disableRawDataCreationPanel;
    vm.getRawDataCreationStatus = getRawDataCreationStatus;
    vm.getSqlHelpSourceItems = getSqlHelpSourceItems;
    vm.changeVisibilityOfHelpForSql = changeVisibilityOfHelpForSql;
    vm.isEnabledHelpForSql = isEnabledHelpForSql;
    vm.changeOpenedRawData = changeOpenedRawData;
    vm.cancelRawDataCreation = cancelRawDataCreation;

    init();

    function init() {
      policy = PolicyModelFactory.getCurrentPolicy();
      rawDataCreationStatus.enabled = false;
    }

    function setRawDataContainer(_rawDataContainer, _rawDataContainerType) {
      rawDataContainer = _rawDataContainer;
      rawDataContainerType = _rawDataContainerType;
    }

    function getRawDataContainer() {
      return rawDataContainer;
    }

    function getRawDataContainerType() {
      return rawDataContainerType;
    }

    function getRawDataCreationStatus() {
      return rawDataCreationStatus;
    }

    function getRawDataUpdateStatus(){
      return rawDataUpdateStatus;
    }
    
    function activateRawDataCreationPanel() {
      rawDataCreationStatus.enabled = true;
      RawDataModelFactory.resetRawData(rawDataContainer.length, rawDataContainerType);
    }

    function disableRawDataCreationPanel() {
      rawDataCreationStatus.enabled = false;
    }


    function showConfirmRemoveRawData() {
      var defer = $q.defer();
      var controller = "ConfirmModalCtrl";
      var templateUrl = "templates/modal/confirm-modal.tpl.html";
      var title = "_REMOVE_TRIGGER_CONFIRM_TITLE_";
      var message = "";
      var size = "lg";
      var resolve = {
        title: function () {
          return title
        }, message: function () {
          return message
        }
      };
      var modalInstance = ModalService.openModal(controller, templateUrl, resolve, '', size);

      modalInstance.result.then(function () {
        defer.resolve();
      }, function () {
        defer.reject();
      });
      return defer.promise;
    }

    function addRawData(rawDataForm) {
      rawDataForm.$submitted = true;
      if (rawDataForm.$valid && RawDataModelFactory.isValidRawData(rawDataContainer, RawDataModelFactory.getContext().position)) {
        rawDataForm.$submitted = false;
        rawDataContainer.push(angular.copy(RawDataModelFactory.getRawData()));
        disableRawDataCreationPanel();
      }
    }

    function saveRawData(rawDataForm) {
      rawDataForm.$submitted = true;
      if (rawDataForm.$valid && RawDataModelFactory.isValidRawData(rawDataContainer, RawDataModelFactory.getContext().position)) {
        rawDataForm.$submitted = false;
        rawDataContainer[RawDataModelFactory.getContext().position] = angular.copy(RawDataModelFactory.getRawData());
      }
    }

    function removeRawData() {
      var defer = $q.defer();
      var rawDataPosition = RawDataModelFactory.getContext().position;
      showConfirmRemoveRawData().then(function () {
        rawDataContainer.splice(rawDataPosition, 1);
        defer.resolve();
      }, function () {
        defer.reject()
      });
      return defer.promise;
    }

    function isNewRawData(index) {
      return index == rawDataContainer.length;
    }

    function isActiveRawDataCreationPanel() {
      return rawDataCreationStatus.enabled;
    }

    function isActiveRawDataUpdatePanel(){
      return rawDataUpdateStatus.enabled;
    }

    function getSqlHelpSourceItems() {
      var sqlSourceItems = [];
      var sourceContainer = [];
      if (rawDataContainerType == rawDataConstants.TRANSFORMATION) {
        sourceContainer = policy.transformations;
        var sourceSqlItem = {};
        sourceSqlItem.name = rawDataConstants.STREAM_TABLE_NAME;
        sourceSqlItem.fields = [];
        for (var i = 0; i < sourceContainer.length; ++i) {
          var currentItem = sourceContainer[i];
          sourceSqlItem.fields = sourceSqlItem.fields.concat(currentItem.outputFields);
        }
        sqlSourceItems.push(sourceSqlItem);
      }
      return sqlSourceItems;
    }

    function changeVisibilityOfHelpForSql(visibility) {
      showHelpForSql = visibility;
    }

    function isEnabledHelpForSql() {
      return showHelpForSql;
    }

    function changeOpenedRawData(selectedRawDataPosition) {
      if (rawDataContainer.length > 0 ) {
        if (selectedRawDataPosition >= 0 && selectedRawDataPosition < rawDataContainer.length) {
          var selectedRawData = rawDataContainer[selectedRawDataPosition];
          RawDataModelFactory.setRawData(selectedRawData, selectedRawDataPosition, rawDataContainerType);
          rawDataUpdateStatus.enabled =  true;
        } else {
          if (selectedRawDataPosition> -1)
          RawDataModelFactory.resetRawData(rawDataContainer.length, rawDataContainerType);
          rawDataUpdateStatus.enabled = false;
        }
      }
    }

    function cancelRawDataCreation() {
      disableRawDataCreationPanel();
      RawDataModelFactory.resetRawData(RawDataModelFactory.getContext().position, rawDataContainerType);
    }
  }
})();
