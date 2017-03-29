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

  /*POLICY WIZARD HEADER CONTROLLER*/
  angular
    .module('webApp')
    .controller('PolicyWizardHeaderCtrl', PolicyWizardHeaderCtrl);

  PolicyWizardHeaderCtrl.$inject = ['WizardStatusService', 'PolicyModelFactory', 'ModalService', '$scope', '$state'];
  function PolicyWizardHeaderCtrl(WizardStatusService, PolicyModelFactory, ModalService, $scope, $state) {
    var header = this;

    var policyTemplate = null;
    header.policy = PolicyModelFactory.getCurrentPolicy();
    header.wizardStatus = WizardStatusService.getStatus();
    header.leaveEditor = leaveEditor;

    header.showPolicyData = showPolicyData;

    function showPolicyData() {
      var controller = 'PolicyCreationModalCtrl';
      var templateUrl = "templates/modal/policy-creation-modal.tpl.html";
      var resolve = {
        title: function () {
          return "_POLICY_._MODAL_SETTINGS_TITLE_";
        }
      };
      ModalService.openModal(controller, templateUrl, resolve, '', 'lg');
    }

    function leaveEditor() {
      var templateUrl = "templates/modal/confirm-modal.tpl.html";
      var controller = "ConfirmModalCtrl";
      var resolve = {
        title: function () {
          return "_POLICY_._WINDOW_._EXIT_._TITLE_";
        },
        message: function () {
          return "_POLICY_._EXIT_CONFIRMATION_";
        }
      };
      var modalInstance = ModalService.openModal(controller, templateUrl, resolve, '', 'lg');

      return modalInstance.result.then(function () {
        $state.go('dashboard.policies');
      });
    }

    $scope.$watchCollection(
      "header.wizardStatus",
      function (newStatus) {
        policyTemplate = PolicyModelFactory.getTemplate();
        if (Object.keys(policyTemplate).length > 0) {
          if (newStatus && newStatus.currentStep >= 0 && newStatus.currentStep < policyTemplate.helpLinks.length - 1) {

            header.helpLink = policyTemplate.helpLinks[newStatus.currentStep];
          } else {
            header.helpLink = null;
          }
        }
      }
    );
  }
})();
