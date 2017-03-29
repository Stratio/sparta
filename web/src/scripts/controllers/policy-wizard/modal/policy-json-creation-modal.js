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

  /*DELETE POLICIES MODAL CONTROLLER */
  angular
    .module('webApp')
    .controller('CreatePolicyJSONModalCtrl', CreatePolicyJSONModalCtrl);

  CreatePolicyJSONModalCtrl.$inject = ['$uibModalInstance', 'PolicyFactory', 'TemplateFactory', '$q'];

  function CreatePolicyJSONModalCtrl($uibModalInstance, PolicyFactory, TemplateFactory, $q) {
    /*jshint validthis: true*/
    var vm = this;
    vm.cancel = cancel;
    vm.policy = {};
    vm.createPolicy = createPolicy;

    init();

    ///////////////////////////////////////

    function init() {
      return TemplateFactory.getPolicyJsonTemplate().then(function (template) {
        vm.template = template;
        console.log(template);
      });
    }

    function cancel() {
      $uibModalInstance.dismiss('cancel');
    }

    function createPolicy(){
      validateForm();
    }


    function validateForm() {
      vm.form.$setSubmitted();
      var parsedJSON;
      if (vm.form.$valid) {
        try{
          parsedJSON = JSON.parse(vm.policy.json);
        } catch(e){
          console.log(vm.form);
          alert("Error");
        }
        vm.error = false;
        /*Check if the name of the policy already exists*/
        return PolicyFactory.existsPolicy(vm.policy.name, vm.policy.id).then(function (found) {
          vm.error = found;
          /* Policy name doesn't exist */
          if (!found) {
            
          }
          /* Policy name exists */
          else {
            vm.errorText = "_ERROR_._200_";
            document.querySelector('#dataSourcenameForm').focus();
          }
        });
      }
      else {
        /*Focus on the first invalid input*/
        document.querySelector('input.ng-invalid').focus();
      }
    }
  }

})();
