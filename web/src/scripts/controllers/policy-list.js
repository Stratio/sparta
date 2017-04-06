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
    .controller('PolicyListCtrl', PolicyListCtrl);

  PolicyListCtrl.$inject = ['WizardStatusService', 'PolicyFactory', 'PolicyModelFactory', 'ModalService', 'UtilsService', '$state',
    '$translate', '$interval', '$scope', '$q', '$filter'];

  function PolicyListCtrl(WizardStatusService, PolicyFactory, PolicyModelFactory, ModalService, UtilsService, $state,
    $translate, $interval, $scope, $q, $filter) {
    /*jshint validthis: true*/
    var vm = this;

    var checkPoliciesStatus = null;

    vm.createPolicy = createPolicy;
    vm.deletePolicy = deletePolicy;
    vm.editPolicy = editPolicy;
    vm.deleteErrorMessage = deleteErrorMessage;
    vm.deleteSuccessMessage = deleteSuccessMessage;
    vm.downloadPolicy = downloadPolicy;
    vm.sortPolicies = sortPolicies;
    vm.createPolicyFromJSON = createPolicyFromJSON;

    vm.policiesData = [];
    vm.policiesJsonData = {};
    vm.errorMessage = { type: 'error', text: '', internalTrace: '' };
    vm.successMessage = { type: 'success', text: '', internalTrace: '' };
    vm.loading = true;
    vm.tableReverse = false;

    init();

    /////////////////////////////////

    function init() {
      getPolicies();
    }

    function deleteErrorMessage() {
      vm.errorMessage.text = '';
      vm.errorMessage.internalTrace = '';
    }

    function deleteSuccessMessage() {
      vm.successMessage.text = '';
      vm.successMessage.internalTrace = '';
    }

    function createPolicy() {
      PolicyModelFactory.resetPolicy();
      WizardStatusService.reset();
      //WizardStatusService.enableNextStep();
      WizardStatusService.nextStep();
      $state.go('wizard.newPolicy', 'create');
    
    }

    function editPolicy(route, policyId) {
      //get the policy status before edit
        WizardStatusService.reset();
        $state.go(route, { "id": policyId });
    }

    function deletePolicy(policyId) {
        deletePolicyConfirm('lg', policyId);
    }

    function createPolicyFromJSON(){
      var controller = 'CreatePolicyJSONModalCtrl';
      var templateUrl = "templates/policies/st-create-policy-json-modal.tpl.html";
      var resolve = {};
      var modalInstance = ModalService.openModal(controller, templateUrl, resolve, '', 'lg');

      modalInstance.result.then(function () {
        vm.successMessage.text = '_POLICY_CREATE_OK_';
        getPolicies(); 
      });
    }

    function deletePolicyConfirm(size, policyId) {
      var controller = 'DeletePolicyModalCtrl';
      var templateUrl = "templates/policies/st-delete-policy-modal.tpl.html";
      var resolve = {
        item: function () {
          return policyId;
        }
      };
      var modalInstance = ModalService.openModal(controller, templateUrl, resolve, '', size);

      modalInstance.result.then(function (policyId) {
        var index = UtilsService.getArrayElementPosition(vm.policiesData, 'id', policyId);
        vm.policiesData.splice(index, 1);
        vm.successMessage.text = '_POLICY_DELETE_OK_';
      });
    }

    function getPolicies() {
      var policies = PolicyFactory.getAllPolicies();
      policies.then(function (result) {
        vm.sortField = 'name';
        vm.policiesData = parsePolicies(result);
        vm.loading = false;
      }, function (error) {
        vm.loading = false;
        $interval.cancel(checkPoliciesStatus);
        vm.successMessage.text = '_ERROR_._' + error.data.i18nCode + '_';
      });
    }

    function parsePolicies(policies){
      angular.forEach(policies, function(policy){
        policy.triggers = convertObjectArrayToString(policy.streamTriggers, 'name');
        policy.cubesStr = convertObjectArrayToString(policy.cubes, 'name');
        policy.transformType = convertObjectArrayToString(policy.transformations, 'type');
      });

      return policies;
    }

    function convertObjectArrayToString(array, property){
      var valueString = '';
      var l = array.length;
      for(var i=0; i<l; i++){
        valueString += array[i][property];
        if(i < l-1) valueString += ', ';
      }
      return valueString;
    }

    function downloadPolicy(policyId) {
      PolicyFactory.downloadPolicy(policyId).then(function (policyFile) {
        var data = "text/json;charset=utf-8," + encodeURIComponent(JSON.stringify(policyFile));
        var a = document.createElement('a');
        a.href = 'data:' + data;
        a.download = policyFile.name + ".json";
        document.body.appendChild(a);
        a.click();
        a.remove();
      })
    }

    function sortPolicies(fieldName) {
      if (fieldName == vm.sortField) {
        vm.tableReverse = !vm.tableReverse;
      } else {
        vm.tableReverse = false;
        vm.sortField = fieldName;
      }
    }

    /*Stop $interval when changing the view*/
    $scope.$on("$destroy", function () {
      if (checkPoliciesStatus) {
        $interval.cancel(checkPoliciesStatus);
      }
    });
  }
})();
