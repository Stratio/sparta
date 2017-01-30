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
(function() {
  'use strict';

  angular
      .module('webApp')
      .service('WizardStatusService', WizardStatusService);

  WizardStatusService.$inject = [];

  function WizardStatusService() {
    var vm = this;
    var status = {};

    vm.reset = reset;
    vm.getStatus = getStatus;
    vm.previousStep = previousStep;
    vm.previousStep = previousStep;
    vm.nextStep = nextStep;
    vm.enableNextStep = enableNextStep;
    vm.disableNextStep = disableNextStep;
    init();

    function init() {
      status.nextStepAvailable = false;
      status.currentStep = -1;
    }

    function reset() {
      init();
    }

    function getStatus() {
      return status;
    }

    function previousStep() {
      status.currentStep--;
    }

    function nextStep() {
      status.currentStep++;
    }

    function enableNextStep() {
      status.nextStepAvailable = true;
    }

    function disableNextStep() {
      status.nextStepAvailable = false;
    }

  }
})();
