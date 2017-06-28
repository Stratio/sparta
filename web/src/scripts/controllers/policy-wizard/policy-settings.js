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

  /*POLICY CREATION MODAL CONTROLLER */
  angular
    .module('webApp')
    .controller('PolicySettingsCtrl', PolicySettingsCtrl);

  PolicySettingsCtrl.$inject = ['PolicyModelFactory', 'PolicyFactory', 'TemplateFactory', 'WizardStatusService', '$state', '$scope'];

  function PolicySettingsCtrl(PolicyModelFactory, PolicyFactory, TemplateFactory, WizardStatusService, $state, $scope) {
    /*jshint validthis: true*/
    var vm = this;
    vm.validateForm = validateForm;

    init();

    ///////////////////////////////////////

    $scope.$on('validatePolicySettings', function (event, data) {
      validateForm(data.callback, data.step);
    });


    function init() {

        WizardStatusService.enableNextStep();
      

      return TemplateFactory.getPolicyTemplate().then(function (template) {
        setSettingsCategories(template.advancedSettings);
        PolicyModelFactory.setTemplate(template);
        vm.currentPolicy = PolicyModelFactory.getCurrentPolicy();
        vm.policy = vm.currentPolicy;
        vm.template = template;
        vm.helpLink = template.helpLinks[0];

      });
    }

    function setSettingsCategories(advancedSettings){
      var categories = [];
      angular.forEach(advancedSettings, function(settings){
        categories.push({
          text: "_SETTINGS_" + settings.name.toString().toUpperCase() + "_",
          isDisabled: false,
          name: settings.name
        });
      });
      vm.categories = categories;
      vm.selectedCategory = categories[0].name;
    }

    function validateForm(next, step) {
      vm.form.$setSubmitted();
      if (vm.form.$valid) {
        vm.error = false;
        /*Check if the name of the policy already exists*/
        return PolicyFactory.existsPolicy(vm.policy.name, vm.policy.id).then(function (found) {
          vm.error = found;
          /* Policy name doesn't exist */
          if (!found) {
            vm.nextStepEnabled = true;
            angular.extend(vm.currentPolicy, vm.policy);
            next(step);
          }
          /* Policy name exists */
          else {
            vm.errorText = "_ERROR_._200_";
            document.querySelector('#dataSourcenameForm').focus();
          }
        });
      }
      else {
        var error = false;
        angular.forEach(vm.categories, function(category){
          vm.form[category.name].$setSubmitted();
          if(vm.form[category.name].$invalid){
              vm.selectedCategory = category.name;
              error = true;
          };
        });

        if(!error){
           vm.selectedCategory = vm.categories[0].name;
        }
        document.querySelector('input.ng-invalid').focus();
      }
    }

  }

})();
