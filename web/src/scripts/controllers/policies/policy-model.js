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

  /*POLICY MODEL CONTROLLER*/
  angular
      .module('webApp')
      .controller('PolicyModelCtrl', PolicyModelCtrl);

  PolicyModelCtrl.$inject = ['ModelFactory', 'PolicyModelFactory', 'ModelService', 'modelConstants', 'UtilsService', '$scope'];

  function PolicyModelCtrl(ModelFactory, PolicyModelFactory, ModelService, modelConstants, UtilsService, $scope) {
    var vm = this;

    vm.init = init;
    vm.addModel = addModel;
    vm.removeModel = removeModel;
    vm.onChangeType = onChangeType;
    vm.cancelModelCreation = cancelModelCreation;
    vm.modelInputs = ModelFactory.getModelInputs();
    vm.isLastModel = ModelService.isLastModel;
    vm.isNewModel = ModelService.isNewModel;
    vm.invalidOutputField = undefined;

    vm.init();

    function init() {
      vm.template = PolicyModelFactory.getTemplate();
      vm.policy = PolicyModelFactory.getCurrentPolicy();
      vm.model = ModelFactory.getModel();
      vm.modelError = '';
      if (vm.model) {
        vm.modelError = ModelFactory.getError();
        vm.modelContext = ModelFactory.getContext();
        vm.modelTypes = vm.template.model.types;
        vm.configPlaceholder = vm.template.configPlaceholder;
        vm.outputPattern = vm.template.outputPattern;
        vm.outputFieldTypes = vm.template.model.defaultOutputFieldTypes;
      }
    }

    function getOutputList() {
      if (vm.template.model[vm.model.type] && vm.template.model[vm.model.type].outputFieldTypes) {
        return vm.template.model[vm.model.type].outputFieldTypes;
      }
      return vm.template.model.defaultOutputFieldTypes;

    }

    function onChangeType() {
      vm.model.outputFields = [];
      vm.model.inputField = "";
      vm.outputFieldTypes = vm.template.model.defaultOutputFieldTypes;
      vm.model.configuration = {};
      switch (vm.model.type) {
        case modelConstants.MORPHLINES:
          vm.model.configuration = vm.template.model[modelConstants.MORPHLINES].defaultConfiguration;
          break;
        case modelConstants.FILTER:
        delete vm.model.inputField;
          vm.model.configuration = vm.template.model[modelConstants.FILTER].defaultConfiguration;
          break;
        case modelConstants.JSON:
          vm.model.configuration = vm.template.model[modelConstants.JSON].defaultConfiguration;
          break;
        case modelConstants.XML:
          vm.model.configuration = vm.template.model[modelConstants.XML].defaultConfiguration;
          break;
        case modelConstants.CSV:
          vm.model.configuration = vm.template.model[modelConstants.CSV].defaultConfiguration;
          break;
        case modelConstants.GEO:
          delete vm.model.inputField;
          vm.model.configuration = vm.template.model[modelConstants.GEO].defaultConfiguration;
          vm.outputFieldTypes = vm.template.model[modelConstants.GEO].outputFieldTypes;
          break;
        default:
          break;
      }
    }

    function addModel() {
      vm.form.$submitted = true;
      if (vm.form.$valid && areValidOutputFields()) {
        if (vm.model.type == modelConstants.DATETIME && vm.model.configuration.granularityTime){
          vm.model.configuration.granularity = vm.model.configuration.granularityNumber ?
           vm.model.configuration.granularityNumber + vm.model.configuration.granularityTime:
            vm.model.configuration.granularityTime;
        }
        vm.form.$submitted = false;
        ModelService.addModel();
        ModelService.changeModelCreationPanelVisibility(false);
      } else {
        ModelFactory.setError("_ERROR_._GENERIC_FORM_");
      }
    }

    function removeModel() {
      return ModelService.removeModel().then(function() {
        var order = 0;
        var modelNumber = vm.policy.transformations.length;
        if (modelNumber > 0) {
          order = vm.policy.transformations[modelNumber - 1].order + 1
        }
        ModelFactory.resetModel(vm.template.model, order, modelNumber);
        ModelFactory.updateModelInputs(vm.policy.transformations);
      });
    }

    function cancelModelCreation() {
      ModelService.disableModelCreationPanel();
      ModelFactory.resetModel(vm.template.model, vm.model.order, vm.modelContext.position);
    }

    function areValidOutputFields() {
      vm.invalidOutputField = undefined;
      if (vm.model.outputFields.length == 0 && vm.model.type != modelConstants.FILTER) {
        return false;
      }
      var policyCurrentFields = ModelFactory.getPreviousOutputFields(vm.policy.transformations,
          ModelFactory.getContext().position);
      for (var i = 0; i < vm.model.outputFields.length; ++i) {
        var outputField = vm.model.outputFields[i];
        if (UtilsService.findElementInJSONArray(policyCurrentFields, outputField, 'name') != -1) {
          vm.invalidOutputField = outputField;
          return false;
        }
      }
      return true;
    }

    $scope.$on("forceValidateForm", function() {
      vm.form.$submitted = true;
    });

    $scope.$watch(
        "vm.model.type",
        function(previousType, newType) {
          if (previousType != newType) {
            vm.outputFieldTypes = getOutputList();
          }
        })
  }
})
();
