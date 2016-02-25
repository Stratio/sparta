(function () {
  'use strict';

  angular
    .module('webApp')
    .service('TriggerService', TriggerService);

  TriggerService.$inject = ['PolicyModelFactory', 'ModalService', 'TriggerModelFactory', '$q'];

  function TriggerService(PolicyModelFactory, ModalService, TriggerModelFactory, $q) {
    var vm = this;
    var showTriggerCreationPanel = null;

    vm.showConfirmRemoveTrigger = showConfirmRemoveTrigger;
    vm.addTrigger = addTrigger;
    vm.saveTrigger = saveTrigger;
    vm.removeTrigger = removeTrigger;
    vm.isNewTrigger = isNewTrigger;
    vm.changeTriggerCreationPanelVisibility = changeTriggerCreationPanelVisibility;
    vm.isActiveTriggerCreationPanel = isActiveTriggerCreationPanel;
    vm.activateTriggerCreationPanel = activateTriggerCreationPanel;

    init();

    function init() {
      vm.policy = PolicyModelFactory.getCurrentPolicy();
      showTriggerCreationPanel = false;
    }

    function activateTriggerCreationPanel() {
      showTriggerCreationPanel = true;
    }

    function showConfirmRemoveTrigger() {
      var defer = $q.defer();
      var controller = "ConfirmModalCtrl";
      var templateUrl = "templates/modal/confirm-modal.tpl.html";
      var title = "_REMOVE_CUBE_CONFIRM_TITLE_";
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
      if (TriggerModelFactory.isValidTrigger(newTrigger, vm.policy.streamTriggers, TriggerModelFactory.getContext().position)) {
        vm.policy.streamTriggers.push(newTrigger);
      } else {
        TriggerModelFactory.setError();
      }
    }

    function saveTrigger(triggerForm) {
      triggerForm.$submitted = true;
      var trigger = angular.copy(TriggerModelFactory.getTrigger());
      if (TriggerModelFactory.isValidTrigger(trigger, vm.policy.streamTriggers, TriggerModelFactory.getContext().position)) {
        triggerForm.$submitted = false;
        vm.policy.streamTriggers[TriggerModelFactory.getContext().position] = trigger;
      } else {
        TriggerModelFactory.setError();
      }
    }

    function removeTrigger() {
      var defer = $q.defer();
      var triggerPosition = TriggerModelFactory.getContext().position;
      showConfirmRemoveTrigger().then(function () {
        vm.policy.streamTriggers.splice(triggerPosition, 1);
        if (vm.policy.streamTriggers.length == 0) {
          PolicyModelFactory.disableNextStep();
        }
        defer.resolve();
      }, function () {
        defer.reject()
      });
      return defer.promise;
    }

    function isNewTrigger(index) {
      return index == vm.policy.streamTriggers.length;
    }

    function changeTriggerCreationPanelVisibility(isVisible) {
      showTriggerCreationPanel = isVisible;
    }

    function isActiveTriggerCreationPanel() {
      return showTriggerCreationPanel;
    }
  }
})();
