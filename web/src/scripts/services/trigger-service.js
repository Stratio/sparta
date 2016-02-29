(function () {
  'use strict';

  angular
    .module('webApp')
    .service('TriggerService', TriggerService);

  TriggerService.$inject = ['PolicyModelFactory', 'ModalService', 'TriggerModelFactory', 'triggerConstants', '$q'];

  function TriggerService(PolicyModelFactory, ModalService, TriggerModelFactory, triggerConstants, $q) {
    var vm = this;
    var showTriggerCreationPanel = null;
    var triggerContainer = null;
    var triggerContainerType = "";

    vm.showConfirmRemoveTrigger = showConfirmRemoveTrigger;
    vm.addTrigger = addTrigger;
    vm.saveTrigger = saveTrigger;
    vm.removeTrigger = removeTrigger;
    vm.isNewTrigger = isNewTrigger;
    vm.setTriggerContainer = setTriggerContainer;
    vm.isActiveTriggerCreationPanel = isActiveTriggerCreationPanel;
    vm.activateTriggerCreationPanel = activateTriggerCreationPanel;
    vm.disableTriggerCreationPanel = disableTriggerCreationPanel;
    vm.getSqlSourceItems = getSqlSourceItems;

    init();

    function init() {
      vm.policy = PolicyModelFactory.getCurrentPolicy();
      showTriggerCreationPanel = false;
    }

    function setTriggerContainer(_triggerContainer, _triggerContainerType) {
      triggerContainer = _triggerContainer;
      triggerContainerType = _triggerContainerType;
    }

    function activateTriggerCreationPanel() {
      showTriggerCreationPanel = true;
    }

    function disableTriggerCreationPanel() {
      showTriggerCreationPanel = false;
    }


    function showConfirmRemoveTrigger() {
      var defer = $q.defer();
      var controller = "ConfirmModalCtrl";
      var templateUrl = "templates/modal/confirm-modal.tpl.html";
      var title = "_REMOVE_TRIGGER_CONFIRM_TITLE_";
      var message = "";
      var resolve = {
        title: function () {
          return title
        }, message: function () {
          return message
        }
      };
      var modalInstance = ModalService.openModal(controller, templateUrl, resolve);

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
      } else {
        TriggerModelFactory.setError();
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
      return showTriggerCreationPanel;
    }

    function generateSqlSourceItemsFieldsByContainerType(item) {
      var sqlSourceItemsFields = [];
      var fields = [];
      if (triggerContainerType == triggerConstants.TRANSFORMATION) {
        fields = item.outputFields;
      } else {
        fields = fields.concat(item.dimensions);
        fields =  fields.concat(item.operators);
      }
      for (var i = 0; i < fields.length; ++i) {
        var item = fields[i];
        sqlSourceItemsFields.push({name: item.name, type: item.type});
      }

      return sqlSourceItemsFields;
    }

    function getSqlSourceItems() {
      var sqlSourceItems = [];
      var sourceContainer = [];
      if (triggerContainerType == triggerConstants.TRANSFORMATION) {
        sourceContainer = vm.policy.transformations;
      }
      else {
        sourceContainer = vm.policy.cubes;
      }
      for (var i = 0; i < sourceContainer.length; ++i) {
        var currentItem = sourceContainer[i];
        var sourceSqlItem = {};
        sourceSqlItem.name =  triggerConstants.TRANSFORMATION + " " + i;
        sourceSqlItem.fields = generateSqlSourceItemsFieldsByContainerType(currentItem);

        sqlSourceItems.push(sourceSqlItem);
      }

      return sqlSourceItems;
    }
  }
})();
