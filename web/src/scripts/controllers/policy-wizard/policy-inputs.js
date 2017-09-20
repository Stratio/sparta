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

  /*POLICY INPUTS CONTROLLER*/
  angular
    .module('webApp')
    .controller('PolicyInputCtrl', PolicyInputCtrl);

  PolicyInputCtrl.$inject = ['WizardStatusService', 'FragmentFactory', 'PolicyModelFactory', '$scope'];

  function PolicyInputCtrl(WizardStatusService, FragmentFactory, PolicyModelFactory, $scope) {
    var vm = this;
    vm.setInput = setInput;
    vm.isSelectedInput = isSelectedInput;
    vm.previousStep = previousStep;
    vm.validateForm = validateForm;
    vm.inputList = [];
    init();

    function init() {
      WizardStatusService.disableNextStep();
      vm.policy = PolicyModelFactory.getCurrentPolicy();
      vm.template = PolicyModelFactory.getTemplate();
      if (vm.policy && Object.keys(vm.template).length > 0) {
        vm.helpLink = vm.template.helpLinks.inputs;
        var inputList = FragmentFactory.getFragments("input");
        return inputList.then(function (result) {
          vm.inputList = result;
          if ( Object.keys( vm.policy.input).length ) {
            WizardStatusService.enableNextStep();
          }
        });
      }
    }

    function setInput(index) {
      if (index >= 0 && index < vm.inputList.length) {
        vm.policy.input = vm.inputList[index];
        WizardStatusService.enableNextStep();
      }
    }

    function isSelectedInput(name) {
      if (vm.policy.input)
        return name == vm.policy.input.name;
      else
        return false;
    }

    function previousStep() {
      WizardStatusService.previousStep();
    }

    function validateForm() {
      if (vm.policy.input.name) {
        vm.error = false;
        WizardStatusService.nextStep();
      }
      else {
        PolicyModelFactory.setError("_ERROR_._POLICY_INPUTS_", "error");
      }
    }

    $scope.$on("forceValidateForm", function () {
      validateForm();
    });
  }
})();
