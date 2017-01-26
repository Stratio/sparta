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

    /*EDIT FRAGMENT MODAL CONTROLLER*/
    angular
        .module('webApp')
        .controller('EditFragmentModalCtrl', EditFragmentModalCtrl);

    EditFragmentModalCtrl.$inject = ['$uibModalInstance', 'item', 'FragmentFactory', '$filter', 'fragmentTemplates', 'policiesAffected'];

    function EditFragmentModalCtrl($uibModalInstance, item, FragmentFactory, $filter, fragmentTemplates, policiesAffected) {
        /*jshint validthis: true*/
        var vm = this;

        vm.setProperties = setProperties;
        vm.ok = ok;
        vm.cancel = cancel;
        vm.setFragmentData = setFragmentData;
        vm.createTypeModels = createTypeModels;
        vm.dataSource = {};
        vm.dataSource.element = {};
        vm.templateFragmentsData = [];
        vm.properties = [];
        vm.error = false;
        vm.errorText = '';
        vm.fragmentTemplateData = {};
        vm.policiesRunning = [];

        init();

        /////////////////////////////////

    function init() {
      setPoliciesRunning(policiesAffected);
      vm.originalName = item.originalName;

      setTexts(item.texts);

      if (vm.policiesRunning.length === 0){
        vm.templateFragmentsData = fragmentTemplates;
        vm.dataSource = angular.copy(item.fragmentSelected);

        vm.createTypeModels(vm.templateFragmentsData);
        vm.selectedIndex = vm.index;
        vm.policiesAffected = policiesAffected;
      }
    }

    function setPoliciesRunning(policiesList) {
      for (var i=0; i < policiesList.length; i++) {
        if (policiesList[i].status !== 'NotStarted' && policiesList[i].status !== 'Stopped' && policiesList[i].status !== 'Failed') {
          var policy = {'name':policiesList[i].policy.name};
          vm.policiesRunning.push(policy);
        }
      }
    }

    function setTexts(texts) {
      vm.modalTexts = {};
      vm.modalTexts.title = texts.title;
      vm.modalTexts.button = texts.button;
      vm.modalTexts.icon = texts.button_icon;
      vm.modalTexts.secondaryText2 = texts.secondaryText2;
      vm.modalTexts.policyRunningMain = texts.policyRunningMain;
      vm.modalTexts.policyRunningSecondary = texts.policyRunningSecondary;
      vm.modalTexts.policyRunningSecondary2 = texts.policyRunningSecondary2;
    }

    function createTypeModels(fragmentData) {
      /*Creating one properties model for each input type*/
      for (var i = 0; i < fragmentData.length; i++) {
        var fragmentName = fragmentData[i].modelType;
        vm.properties[fragmentName] = {};

        /*Flag to check if there are any visible field*/
        vm.fragmentTemplateData[fragmentName] = $filter('filter')(fragmentData[i].properties, {'visible': []}, true);
        vm.properties[fragmentName]._visible = (vm.fragmentTemplateData[fragmentName].length > 0) ? true : false;
        for (var j = 0; j < fragmentData[i].properties.length; j++) {
          var fragmentProperty = fragmentData[i].properties[j];

          switch (fragmentProperty.propertyType) {
            case 'boolean':
              vm.properties[fragmentName][fragmentProperty.propertyId] = false;
              break;

            case 'list':
              vm.properties[fragmentName][fragmentProperty.propertyId] = [];
              var newFields = {};

              for (var m = 0; m < fragmentProperty.fields.length; m++) {
                var defaultValue = (fragmentProperty.fields[m].default) ? fragmentProperty.fields[m].default : '';
                defaultValue = (fragmentProperty.fields[m].propertyType === 'number') ? parseInt(defaultValue) : defaultValue;
                newFields[fragmentProperty.fields[m].propertyId] = defaultValue;
              }
              vm.properties[fragmentName][fragmentProperty.propertyId].push(newFields);
              break;

            default:
              var defaultValue = (fragmentProperty.default) ? fragmentProperty.default : '';
              defaultValue = (fragmentProperty.propertyType === 'number') ? parseInt(defaultValue) : defaultValue;
              vm.properties[fragmentName][fragmentProperty.propertyId] = defaultValue;
              break;
          }
        }

        /*Init properties*/
        if (fragmentName === vm.dataSource.element.type) {
          angular.forEach(vm.dataSource.element.configuration, function (value, key) {
            vm.properties[fragmentName][key] = value;
          });

          vm.dataSource.element.configuration = vm.properties[fragmentName];
          vm.index = i;
        }
      }
    }

    function setProperties(index, inputName) {
      vm.selectedIndex = index;
      vm.dataSource.element.configuration = (vm.properties[inputName].select) ? vm.properties[inputName][vm.properties[inputName].type] : vm.properties[inputName];
      vm.setFragmentData(index);
    }

    function setFragmentData(index) {
      /*Set fragment*/
      vm.dataSource.description = vm.templateFragmentsData[index].description.long;
      vm.dataSource.shortDescription = vm.templateFragmentsData[index].description.short;
      vm.dataSource.element.name = 'in-' + vm.dataSource.element.type;
    }

    function ok() {
      if (vm.form.$valid) {
        deleteNotVisibleProperties();
        checkFragmentName();
      }
    }

    function deleteNotVisibleProperties() {
      if (vm.dataSource.element.configuration._visible) {
        var fragmentType = vm.dataSource.element.type;

        for (var i = 0; i < vm.fragmentTemplateData[fragmentType].length; i++) {
          var propertyId = vm.fragmentTemplateData[fragmentType][i].propertyId;
          var originalProperty = vm.fragmentTemplateData[fragmentType][i].visible[0][0].propertyId;
          var originalPropertyValue = vm.fragmentTemplateData[fragmentType][i].visible[0][0].value;

          if (vm.dataSource.element.configuration[originalProperty] !== originalPropertyValue) {
            delete vm.dataSource.element.configuration[propertyId]
          }
        }
      }
      delete vm.dataSource.element.configuration['_visible'];
    }

    function checkFragmentName() {
      var inputNamesExisting = [];
      var newInputName = vm.dataSource.name.toLowerCase();
      inputNamesExisting = $filter('filter')(item.fragmentNamesList, {'name': newInputName}, true);

      if (inputNamesExisting.length > 0 && inputNamesExisting[0].name !== vm.originalName) {
        vm.error = true;
        vm.errorText = "_ERROR_._100_";
      }
      else {
        editFragment();
      }
    }

    function editFragment() {
      var updateFragment = FragmentFactory.updateFragment(vm.dataSource);

      updateFragment.then(function (result) {
        var callBackData = {};
        callBackData.originalFragment = item.fragmentSelected;
        callBackData.editedFragment = result;

        $uibModalInstance.close(callBackData);

      }, function (error) {
        vm.error = true;
        vm.errorText = "_ERROR_._" + error.data.i18nCode + "_";
      });
    }

    function cancel() {
      $uibModalInstance.dismiss('cancel');
    }
  }
})();
