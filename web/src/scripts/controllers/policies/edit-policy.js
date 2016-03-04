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

  /*POLICY EDITION CONTROLLER*/
  angular
    .module('webApp')
    .controller('EditPolicyCtrl', EditPolicyCtrl);

  EditPolicyCtrl.$inject = ['TemplateFactory', 'PolicyModelFactory', 'PolicyFactory', 'ModalService', '$state', '$stateParams'];
  function EditPolicyCtrl(TemplateFactory, PolicyModelFactory, PolicyFactory, ModalService, $state, $stateParams) {
    var vm = this;

    vm.changeStepNavigationVisibility = changeStepNavigationVisibility;
    vm.confirmPolicy = confirmPolicy;
    vm.closeErrorMessage = closeErrorMessage;

    init();

    function init() {
      return TemplateFactory.getPolicyTemplate().then(function (template) {
        PolicyModelFactory.setTemplate(template);

        var id = $stateParams.id;
        vm.steps = PolicyModelFactory.getTemplate().steps;
        vm.status = PolicyModelFactory.getProcessStatus();
        vm.successfullySentPolicy = false;
        vm.showStepNavigation = true;
        vm.error = null;
        vm.editionMode  = true;
        PolicyFactory.getPolicyById(id).then(
          function (policyJSON) {
            PolicyModelFactory.setPolicy(policyJSON);
            vm.policy = PolicyModelFactory.getCurrentPolicy();
            //PolicyModelFactory.nextStep();
          }, function () {
            $state.go('dashboard.policies');
          });
      });
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
          return "_POLICY_._WINDOW_._EDIT_._TITLE_";
        },
        message: function () {
          return "";
        }
      };
      var modalInstance = ModalService.openModal(controller, templateUrl, resolve, '', 'lg');

      return modalInstance.result.then(function () {
        var finalJSON = PolicyModelFactory.getFinalJSON();

        PolicyFactory.savePolicy(finalJSON).then(function () {
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
