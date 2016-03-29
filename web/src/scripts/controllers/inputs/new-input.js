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

  /*NEW FRAGMENT MODAL CONTROLLER*/
  angular
    .module('webApp')
    .controller('NewFragmentModalCtrl', NewFragmentModalCtrl);

  NewFragmentModalCtrl.$inject = ['$modalInstance', 'item', 'fragmentTemplates', 'FragmentFactory', '$filter'];

  function NewFragmentModalCtrl($modalInstance, item, fragmentTemplates, FragmentFactory, $filter) {
    /*jshint validthis: true*/
    var vm = this;

    vm.setProperties = setProperties;
    vm.ok = ok;
    vm.cancel = cancel;
    vm.initFragmentObject = initFragmentObject;
    vm.setFragmentData = setFragmentData;
    vm.createTypeModels = createTypeModels;
    vm.dataSource = {};
    vm.dataSource.element = {};
    vm.templateFragmentsData = [];
    vm.properties = [];
    vm.error = false;
    vm.errorText = '';
    vm.fragmentType = '';
    vm.fragmentTemplateData = {};
    vm.policiesRunning = [];

    init();

    /////////////////////////////////

    function init() {
      vm.fragmentType = item.fragmentType;

      setTexts(item.texts);

      vm.templateFragmentsData = fragmentTemplates;
      vm.initFragmentObject(vm.templateFragmentsData);
      vm.createTypeModels(vm.templateFragmentsData);
      vm.selectedIndex = 0;
    }

    function setTexts(texts) {
      vm.modalTexts = {};
      vm.modalTexts.title = texts.title;
      vm.modalTexts.button = texts.button;
      vm.modalTexts.icon = texts.button_icon;
    }

    function initFragmentObject(fragmentData) {
      /*Init fragment*/
      vm.dataSource.fragmentType = vm.fragmentType;
      vm.dataSource.name = '';

      /*Init fragment.element*/
      vm.dataSource.element.type = fragmentData[0].modelType;
      vm.dataSource.element.name = 'in-' + vm.dataSource.element.type;

      /*Init fragment.element.configuration*/
      vm.dataSource.element.configuration = {};

      vm.setFragmentData(0);
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
        if (i === 0) {
          vm.dataSource.element.configuration = vm.properties[fragmentName];
        }
      }
    }

    function setFragmentData(index) {
      /*Set fragment*/
      vm.dataSource.description = vm.templateFragmentsData[index].description.long;
      vm.dataSource.shortDescription = vm.templateFragmentsData[index].description.short;
      vm.dataSource.element.name = 'in-' + vm.dataSource.element.type;
    }

    function setProperties(index, fragmentName) {
      vm.selectedIndex = index;
      vm.dataSource.element.configuration = vm.properties[fragmentName]
      vm.setFragmentData(index);
    }

    function ok() {
      if (vm.form.$valid) {
        deleteNotVisibleProperties();
        checkFragmnetname();
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

    function checkFragmnetname(newInputData) {
      var fragmentNamesExisting = [];
      var newFragmentName = vm.dataSource.name.toLowerCase();
      fragmentNamesExisting = $filter('filter')(item.fragmentNamesList, {'name': newFragmentName}, true);

      if (fragmentNamesExisting.length > 0) {
        vm.error = true;
        vm.errorText = "_ERROR_._100_";
      }
      else {
        createfragment();
      }
    }

    function createfragment() {
      var newFragment = FragmentFactory.createFragment(vm.dataSource);

      newFragment.then(function (result) {
        var callBackData = result;

        $modalInstance.close(callBackData);

      }, function (error) {
        vm.error = true;
        vm.errorText = "_ERROR_._" + error.data.i18nCode + "_";
      });
    }

    function cancel() {
      $modalInstance.dismiss('cancel');
    }
  }
})();
