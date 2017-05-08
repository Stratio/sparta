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
      .controller('ExecutionsListCtrl', ExecutionsListCtrl);

  ExecutionsListCtrl.$inject = ['WizardStatusService', 'PolicyFactory', 'PolicyModelFactory', 'ModalService', '$state',
    '$translate', '$interval', '$scope', '$q', '$filter'];

  function ExecutionsListCtrl(WizardStatusService, PolicyFactory, PolicyModelFactory, ModalService, $state,
                          $translate, $interval, $scope, $q, $filter) {
    /*jshint validthis: true*/
    var vm = this;

    var checkPoliciesStatus = null;

    vm.runPolicy = runPolicy;
    vm.stopPolicy = stopPolicy;
    vm.sortPolicies = sortPolicies;
    vm.deleteCheckpoint = deleteCheckpoint;

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

    function runPolicy(policyId, policyStatus, policyName) {
      if (policyStatus.toLowerCase() === 'notstarted' || policyStatus.toLowerCase() === 'failed' ||
          policyStatus.toLowerCase() === 'stopped' || policyStatus.toLowerCase() === 'stopping' ||
          policyStatus.toLowerCase() === 'killed') {
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
      if (policyStatus.toLowerCase() !== 'notstarted' && policyStatus.toLowerCase() !== 'stopped' &&
       policyStatus.toLowerCase() !== 'stopping' && policyStatus.toLowerCase() !== 'finished' &&
       policyStatus.toLowerCase() !== 'killed') {


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
