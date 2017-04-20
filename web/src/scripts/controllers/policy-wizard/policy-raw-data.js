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

  /*RAW DATA CONTROLLER*/
  angular
    .module('webApp')
    .controller('RawDataCtrl', RawDataCtrl);


  RawDataCtrl.$inject = ['TemplateFactory', 'PolicyModelFactory', 'ModalService', '$translate', 'WizardStatusService'];

  function RawDataCtrl(TemplateFactory, PolicyModelFactory, ModalService, $translate, WizardStatusService) {

    var vm = this;
    vm.policy = PolicyModelFactory.getCurrentPolicy();
    vm.saveRawData = saveRawData;
    vm.cancelRawDataCreation = cancelRawDataCreation;
    vm.currentRawData = vm.policy.rawData || {};
    vm.rawData = angular.copy(vm.currentRawData);
    vm.createMode = vm.rawData.dataField ? false : true;
    vm.removeRawData = removeRawData;
    vm.confirmDeleteRawData = confirmDeleteRawData;
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


    init();

    function init() {
      if(vm.policy.transformations.length){
        WizardStatusService.enableNextStep();
      }
      if (vm.createMode) {
        resetForm();
      }
      vm.template = TemplateFactory.getRawDataJsonTemplate().then(function (template) {
        vm.template = template;
      });
    }

    function saveRawData(rawDataForm) {

      rawDataForm.$submitted = true;
      if (rawDataForm.$valid) {
        vm.createMode = false;
        vm.policy.rawData = vm.rawData;
        vm.form.$setPristine();
        vm.successMessage.text = $translate.instant('_RAWDATA_SAVED_OK_');
      }
    }

    function cancelRawDataCreation() {
      vm.creationPanel = false;
      vm.rawData = {};
    }

    function confirmDeleteRawData() {
      var templateUrl = "templates/modal/confirm-modal.tpl.html";
      var controller = "ConfirmModalCtrl";
      var extraClass = null;
      var size = 'lg';
      var resolve = {
        title: function () {
          return "title"
        },
        message: function () {
          return ""
        }
      };
      var modalInstance = ModalService.openModal(controller, templateUrl, resolve, extraClass, size);
      return modalInstance.result.then(function () {
        removeRawData();
      });
    }

    function removeRawData() {
      vm.form.$submitted = false;
      vm.form.$dirty = false;
      vm.createMode = true; //creation mode
      vm.creationPanel = false; //hide creationPanel
      vm.policy.rawData = {}; //remove curren raw data
      resetForm(); //reset form
    }

    function resetForm() {
      vm.rawData = {
        "writer": {
          "outputs": [],
          "autoCalculatedFields": [],
          "partitionBy": "",
          "tableName": ""
        },
        "dataField": "",
        "timeField": ""
      };
    }
  }

})(window.angular);
