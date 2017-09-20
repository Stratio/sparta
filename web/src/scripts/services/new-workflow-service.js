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
    .service('NewWorkflowService', NewWorkflowService);

  NewWorkflowService.$inject = ['ModalService', '$q'];

  function NewWorkflowService(ModalService, $q) {
    var vm = this;

    this.showCreationModeModal = function () {
      var deferred = $q.defer();

      var controller = 'PolicyCreationModalCtrl';
      var templateUrl = "templates/policies/st-create-policy-modal.tpl.html";
      var resolve = {};
      var modalInstance = ModalService.openModal(controller, templateUrl, resolve, 'new-workflow-modal', 'md');

      modalInstance.result.then(function (result) {
        if (result === 'JSON') {
          showCreationJSONModal().then(function(){
            deferred.resolve(true);
          });
        } else {
            deferred.resolve(false);
        }
      });

      return deferred.promise;
    }

    function showCreationJSONModal() {
      var controller = 'CreatePolicyJSONModalCtrl';
      var templateUrl = "templates/policies/st-create-policy-json-modal.tpl.html";
      var resolve = {};
      var modalInstance = ModalService.openModal(controller, templateUrl, resolve, '', 'lg');

      return modalInstance.result;
    }
  }


})();
