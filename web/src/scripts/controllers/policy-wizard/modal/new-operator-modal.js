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

  /*NEW OPERATOR MODAL CONTROLLER */
  angular
      .module('webApp')
      .controller('NewOperatorModalCtrl', NewOperatorModalCtrl);

  NewOperatorModalCtrl.$inject = ['$uibModalInstance', 'operatorName', 'operatorType', 'operators', 'UtilsService',
    'template', 'inputFieldList', 'cubeConstants', 'TemplateFactory'];

  function NewOperatorModalCtrl($uibModalInstance, operatorName, operatorType, operators, UtilsService, template,
                                inputFieldList, cubeConstants, TemplateFactory) {
    /*jshint validthis: true*/
    var vm = this;

    vm.ok = ok;
    vm.cancel = cancel;
    vm.isCount = isCount;
    init();

    function init() {
      vm.operator = {};
      vm.operator.name = operatorName;
      vm.operator.configuration = {};
      vm.operator.type = operatorType;
      vm.configHelpLink = template.configurationHelpLink;
      vm.nameError = "";
     
      TemplateFactory.getOperatorTemplateByType(operatorType).then(function(template) {
        vm.template = template;
        if (operatorType != cubeConstants.COUNT) {
          var inputField = UtilsService.filterByAttribute(vm.template.properties, 'propertyId', 'configuration.inputField')[0];
          inputField.values = UtilsService.generateOptionListFromStringArray(inputFieldList);
        }
      });
    }

    ///////////////////////////////////////

    function isRepeated() {
      var position = UtilsService.findElementInJSONArray(operators, vm.operator, "name");
      var repeated = position != -1;
      if (repeated) {
        vm.nameError = "_POLICY_._CUBE_._OPERATOR_NAME_EXISTS_";
        document.querySelector('#nameForm').focus();
      }
      return repeated;
    }

    function isCount() {
      return vm.operator.type == cubeConstants.COUNT
    }

    function ok() {
      vm.nameError = "";
      if (vm.operator.configuration && vm.operator['configuration.inputField'] == '') {
        delete vm.operator['configuration.inputField']
      }
      if (vm.form.$valid) {
        if (!isRepeated()) {
          var parsedOperator = UtilsService.convertDottedPropertiesToJson(vm.operator);
          $uibModalInstance.close(parsedOperator);
        }
      }
    }

    function cancel() {
      $uibModalInstance.dismiss('cancel');
    }
  }

})();
