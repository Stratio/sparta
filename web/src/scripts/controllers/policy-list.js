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
      .controller('PolicyListCtrl', PolicyListCtrl);

  PolicyListCtrl.$inject = ['WizardStatusService', 'PolicyFactory', 'PolicyModelFactory', 'ModalService', '$state',
    '$translate', '$interval', '$scope', '$q', '$filter'];

  function PolicyListCtrl(WizardStatusService, PolicyFactory, PolicyModelFactory, ModalService, $state,
                          $translate, $interval, $scope, $q, $filter) {
    /*jshint validthis: true*/
    var vm = this;

    var checkPoliciesStatus = null;

    vm.createPolicy = createPolicy;
    vm.deletePolicy = deletePolicy;
    vm.runPolicy = runPolicy;
    vm.stopPolicy = stopPolicy;
    vm.editPolicy = editPolicy;
    vm.deleteCheckpoint = deleteCheckpoint;
    vm.deleteErrorMessage = deleteErrorMessage;
    vm.deleteSuccessMessage = deleteSuccessMessage;
    vm.downloadPolicy = downloadPolicy;
    vm.sortPolicies = sortPolicies;

    vm.policiesData = [];
    vm.policiesJsonData = {};
    vm.errorMessage = {type: 'error', text: '', internalTrace: ''};
    vm.successMessage = {type: 'success', text: '', internalTrace: ''};
    vm.loading = true;
    vm.tableReverse = false;
    vm.showInfoModal = showInfoModal;

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
      var controller = 'PolicyCreationModalCtrl';
      var templateUrl = "templates/modal/policy-creation-modal.tpl.html";
      var resolve = {
        title: function() {
          return "_POLICY_._MODAL_CREATION_TITLE_";
        }
      };
      var modalInstance = ModalService.openModal(controller, templateUrl, resolve, null, 'lg');
      return modalInstance.result.then(function() {
        WizardStatusService.nextStep();
        $state.go('wizard.newPolicy');
      });
    }

    function editPolicy(route, policyId, policyStatus) {
      vm.errorMessage.text = "";
      WizardStatusService.reset();
      if (policyStatus.toLowerCase() === 'notstarted' || policyStatus.toLowerCase() === 'failed' ||
          policyStatus.toLowerCase() === 'stopped' || policyStatus.toLowerCase() === 'stopping') {
        $state.go(route, {"id": policyId});
      }
      else {
        vm.errorMessage.text = '_POLICY_ERROR_EDIT_POLICY_';
      }
    }

    function deletePolicy(policyId, policyStatus, index) {
      if (policyStatus.toLowerCase() === 'notstarted' || policyStatus.toLowerCase() === 'failed' ||
          policyStatus.toLowerCase() === 'stopped' || policyStatus.toLowerCase() === 'stopping') {
        var policyToDelete =
        {
          'id': policyId,
          'index': index
        };
        deletePolicyConfirm('lg', policyToDelete);
      }
      else {
        vm.errorMessage.text = '_POLICY_ERROR_DELETE_POLICY_';
      }
    }

    function runPolicy(policyId, policyStatus, policyName) {
      if (policyStatus.toLowerCase() === 'notstarted' || policyStatus.toLowerCase() === 'failed' ||
          policyStatus.toLowerCase() === 'stopped' || policyStatus.toLowerCase() === 'stopping') {
        var policyRunning = PolicyFactory.runPolicy(policyId);

        policyRunning.then(function() {
          vm.successMessage.text = $translate.instant('_RUN_POLICY_OK_', {policyName: policyName});
        }, function(error) {
          vm.errorMessage.text = "_ERROR_._" + error.data.i18nCode + "_";
          vm.errorMessage.internalTrace = 'Error: ' + error.data.message;
        });
      }
      else {
        vm.errorMessage.text = $translate.instant('_RUN_POLICY_KO_', {policyName: policyName});
      }
    }

    function stopPolicy(policyId, policyStatus, policyName) {
      if (policyStatus.toLowerCase() !== 'notstarted' && policyStatus.toLowerCase() !== 'stopped' && policyStatus.toLowerCase() !== 'stopping') {

        var stopPolicy =
        {
          "id": policyId,
          "status": "Stopping"
        };

        var policyStopping = PolicyFactory.stopPolicy(stopPolicy);

        policyStopping.then(function() {
          vm.successMessage.text = $translate.instant('_STOP_POLICY_OK_', {policyName: policyName});
        }, function(error) {
          vm.errorMessage.text = '_ERROR_._' + error.data.i18nCode + '_';
          vm.errorMessage.internalTrace = 'Error: ' + error.data.message;
        });
      }
      else {
        vm.errorMessage.text = $translate.instant('_STOP_POLICY_KO_', {policyName: policyName});
      }
    }


    function deleteCheckpoint(policyName){
      var deletePolicyCheckpoint = PolicyFactory.deletePolicyCheckpoint(policyName);
      deletePolicyCheckpoint.then(function(response){
        vm.successMessage.text = $translate.instant('_DELETE_CHECKPOINT_POLICY_OK_', {policyName: policyName});
      });
    }

    function deletePolicyConfirm(size, policy) {
      var controller = 'DeletePolicyModalCtrl';
      var templateUrl = "templates/policies/st-delete-policy-modal.tpl.html";
      var resolve = {
        item: function() {
          return policy;
        }
      };
      var modalInstance = ModalService.openModal(controller, templateUrl, resolve, '', size);

      modalInstance.result.then(function(selectedPolicy) {
        vm.policiesData.splice(selectedPolicy.index, 1);
        vm.successMessage.text = '_POLICY_DELETE_OK_';
      });
    }

    function updatePoliciesStatus() {
      var defer = $q.defer();
      var policiesStatus = PolicyFactory.getPoliciesStatus();
      policiesStatus.then(function(result) {
        var policiesWithStatus = result;
        if (policiesWithStatus) {
          for (var i = 0; i < policiesWithStatus.length; i++) {
            var policyData = $filter('filter')(vm.policiesData, {'id': policiesWithStatus[i].id}, true)[0];
            if (policyData) {
              policyData.submissionId = policiesWithStatus[i].submissionId;
              policyData.statusInfo = policiesWithStatus[i].statusInfo;
              policyData.lastError = policiesWithStatus[i].lastError;
              policyData.status = policiesWithStatus[i].status;
              policyData.clusterUI = policiesWithStatus[i].resourceManagerUrl;
              policyData.lastExecutionMode = policiesWithStatus[i].lastExecutionMode;
            } else {
              vm.policiesData.push(policiesWithStatus[i]);
            }
          }
        }
        defer.resolve();
      }, function() {
        defer.reject();
      });
      return defer.promise;
    }

    function getPolicies() {
      var policiesStatus = PolicyFactory.getPoliciesStatus();
      policiesStatus.then(function (result) {
        vm.sortField = 'name';
        vm.policiesData = result;
        vm.loading = false;
        checkPoliciesStatus = $interval(function() {
          updatePoliciesStatus().then(null, function() {
            $interval.cancel(checkPoliciesStatus);
          });
        }, 5000);
      }, function(error) {
        vm.loading = false;
        $interval.cancel(checkPoliciesStatus);
        vm.successMessage.text = '_ERROR_._' + error.data.i18nCode + '_';
      });
    }

    function downloadPolicy(policyId) {
      PolicyFactory.downloadPolicy(policyId).then(function(policyFile) {
        var data = "text/json;charset=utf-8," + encodeURIComponent(JSON.stringify(policyFile));
        var a = document.createElement('a');
        a.href = 'data:' + data;
        a.download = policyFile.name + ".json";
        document.body.appendChild(a);
        a.click();
        a.remove();
      })
    }

    function sortPolicies(fieldName){
      if(fieldName == vm.sortField){
        vm.tableReverse = !vm.tableReverse;
      } else {
        vm.tableReverse = false;
        vm.sortField = fieldName;
      }
    }

    function showInfoModal(policy) {
      var controller = 'PolicyInfoModalCtrl';
      var templateUrl = "templates/modal/policy-info-modal.tpl.html";
      var resolve = {
        policyName: function() {
          return policy.name;
        },
        policyDescription: function() {
          return policy.description;
        },
        status: function() {
          return policy.status;
        },
        statusInfo: function() {
          return policy.statusInfo;
        },
        submissionId: function() {
          return policy.submissionId;
        },
        deployMode: function() {
          return policy.lastExecutionMode;
        },
        error: function() {
          return policy.lastError;
        }
      };
      ModalService.openModal(controller, templateUrl, resolve, '', 'lg');
    }

    /*Stop $interval when changing the view*/
    $scope.$on("$destroy", function() {
      if (checkPoliciesStatus) {
        $interval.cancel(checkPoliciesStatus);
      }
    });
  }
})();
