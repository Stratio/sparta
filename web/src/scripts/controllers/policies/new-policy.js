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

  /*POLICY CREATION CONTROLLER*/
  angular
    .module('webApp')
    .controller('NewPolicyCtrl', NewPolicyCtrl);

  NewPolicyCtrl.$inject = ['PolicyModelFactory', 'PolicyFactory', 'ModalService', '$state'];
  function NewPolicyCtrl(PolicyModelFactory, PolicyFactory, ModalService, $state) {
    var vm = this;

    vm.changeStepNavigationVisibility = changeStepNavigationVisibility;
    vm.confirmPolicy = confirmPolicy;
    vm.closeErrorMessage = closeErrorMessage;

    init();

    function init() {
      vm.policy = PolicyModelFactory.getCurrentPolicy();
      if (vm.policy && PolicyModelFactory.getProcessStatus().currentStep == 0) {
        vm.steps = PolicyModelFactory.getTemplate().steps;
        vm.status = PolicyModelFactory.getProcessStatus();
        vm.successfullySentPolicy = false;
        vm.error = null;
        vm.showStepNavigation = true;
        vm.editionMode  = false;
      }
      else {
        $state.go('dashboard.policies');
      }
    }

    function closeErrorMessage() {
      vm.error = null;
    }

    function changeStepNavigationVisibility() {
      vm.showStepNavigation = !vm.showStepNavigation;
    }

    function confirmPolicy() {
      var templateUrl = "templates/modal/confirm-modal.tpl.html";
      var controller = "ConfirmModalCtrl";
      var resolve = {
        title: function () {
          return "_POLICY_._WINDOW_._CONFIRM_._TITLE_";
        },
        message: function () {
          return "";
        }
      };
      var modalInstance = ModalService.openModal(controller, templateUrl, resolve, "", "lg");

      return modalInstance.result.then(function () {
        var finalJSON = PolicyModelFactory.getFinalJSON();
        PolicyFactory.createPolicy(finalJSON).then(function () {
          PolicyModelFactory.resetPolicy();
          $state.go("dashboard.policies");
        }, function (error) {
          if (error) {
            if (error.data.message) {
              vm.error = error.data.message;
            }
            else
             vm.error = error.data;
          }
        });
      });
    }
  }
})();
