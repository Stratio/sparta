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
    .service('TriggerService', TriggerService);

  TriggerService.$inject = ['PolicyModelFactory', 'ModalService', 'TriggerModelFactory', 'triggerConstants', '$q'];

  function TriggerService(PolicyModelFactory, ModalService, TriggerModelFactory, triggerConstants, $q) {
    var vm = this;
    var triggerCreationStatus = {};
    var triggerContainer = null;
    var triggerContainerType = "";
    var showHelpForSql = false;

    vm.showConfirmRemoveTrigger = showConfirmRemoveTrigger;
    vm.addTrigger = addTrigger;
    vm.saveTrigger = saveTrigger;
    vm.removeTrigger = removeTrigger;
    vm.isNewTrigger = isNewTrigger;
    vm.setTriggerContainer = setTriggerContainer;
    vm.getTriggerContainer = getTriggerContainer;
    vm.getTriggerTemplate = getTriggerTemplate;
    vm.isActiveTriggerCreationPanel = isActiveTriggerCreationPanel;
    vm.activateTriggerCreationPanel = activateTriggerCreationPanel;
    vm.disableTriggerCreationPanel = disableTriggerCreationPanel;
    vm.getTriggerCreationStatus = getTriggerCreationStatus;
    vm.getSqlHelpSourceItems = getSqlHelpSourceItems;
    vm.changeVisibilityOfHelpForSql = changeVisibilityOfHelpForSql;
    vm.isEnabledHelpForSql = isEnabledHelpForSql;
    vm.changeOpenedTrigger = changeOpenedTrigger;
    vm.cancelTriggerCreation = cancelTriggerCreation;

    init();

    function init() {
      vm.policy = PolicyModelFactory.getCurrentPolicy();
      triggerCreationStatus.enabled = false;
    }

    function setTriggerContainer(_triggerContainer, _triggerContainerType) {
      triggerContainer = _triggerContainer;
      triggerContainerType = _triggerContainerType;
    }

    function getTriggerContainer() {
      return triggerContainer;
    }

    function getTriggerCreationStatus(){
      return triggerCreationStatus;
    }

    function getTriggerTemplate() {
      return PolicyModelFactory.getTemplate().trigger[triggerContainerType];
    }

    function activateTriggerCreationPanel() {
      triggerCreationStatus.enabled = true;
      TriggerModelFactory.resetTrigger(triggerContainer.length, triggerContainerType);
    }

    function disableTriggerCreationPanel() {
      triggerCreationStatus.enabled  = false;
    }


    function showConfirmRemoveTrigger() {
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

    function addTrigger() {
      var newTrigger = angular.copy(TriggerModelFactory.getTrigger());
      if (TriggerModelFactory.isValidTrigger(newTrigger, triggerContainer, TriggerModelFactory.getContext().position)) {
        triggerContainer.push(newTrigger);
      }
    }

    function saveTrigger(triggerForm) {
      triggerForm.$submitted = true;
      var trigger = angular.copy(TriggerModelFactory.getTrigger());
      if (TriggerModelFactory.isValidTrigger(trigger, triggerContainer, TriggerModelFactory.getContext().position)) {
        triggerForm.$submitted = false;
        triggerContainer[TriggerModelFactory.getContext().position] = trigger;
      } else {
        TriggerModelFactory.setError();
      }
    }

    function removeTrigger() {
      var defer = $q.defer();
      var triggerPosition = TriggerModelFactory.getContext().position;
      showConfirmRemoveTrigger().then(function () {
        triggerContainer.splice(triggerPosition, 1);
        defer.resolve();
      }, function () {
        defer.reject()
      });
      return defer.promise;
    }

    function isNewTrigger(index) {
      return index == triggerContainer.length;
    }

    function isActiveTriggerCreationPanel() {
      return triggerCreationStatus.enabled;
    }

    function getSqlHelpSourceItems() {
      var sqlSourceItems = [];
      var sourceContainer = [];
      if (triggerContainerType == triggerConstants.TRANSFORMATION) {
        sourceContainer = vm.policy.transformations;
        var sourceSqlItem = {};
        sourceSqlItem.name = triggerConstants.STREAM_TABLE_NAME;
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

    function changeOpenedTrigger(selectedTriggerPosition) {
      if (triggerContainer.length > 0 && selectedTriggerPosition >= 0 && selectedTriggerPosition < triggerContainer.length) {
        var selectedTrigger = triggerContainer[selectedTriggerPosition];
        TriggerModelFactory.setTrigger(selectedTrigger, selectedTriggerPosition, triggerContainerType);
      }
    }

    function cancelTriggerCreation() {
      disableTriggerCreationPanel();
      TriggerModelFactory.resetTrigger(TriggerModelFactory.getContext().position, triggerContainerType);
    }
  }
})();
