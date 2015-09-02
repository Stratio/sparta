(function() {
  'use strict';

    /*EDIT FRAGMENT MODAL CONTROLLER*/
    angular
        .module('webApp')
        .controller('EditFragmentModalCtrl', EditFragmentModalCtrl);

    EditFragmentModalCtrl.$inject = ['$modalInstance', 'item', 'FragmentFactory', '$filter', 'fragmentTemplates', 'policiesAffected'];

    function EditFragmentModalCtrl($modalInstance, item, FragmentFactory, $filter, fragmentTemplates, policiesAffected) {
        /*jshint validthis: true*/
        var vm = this;

        vm.setProperties = setProperties;
        vm.ok = ok;
        vm.cancel = cancel;
        vm.setFragmentData = setFragmentData;
        vm.createTypeModels = createTypeModels;
        vm.dataSource = {};
        vm.dataSource.element = {};
        vm.templateInputsData = [];
        vm.properties = [];
        vm.error = false;
        vm.errorText = '';
        vm.fragmentTemplateData = {};

        init();

        /////////////////////////////////

        function init() {
          console.log('--> EditFragmentModalCtrl');
          console.log('> Data received');
          console.log(item);

          vm.originalName = item.originalName;

          setTexts(item.texts);

          vm.templateInputsData = fragmentTemplates;
          vm.dataSource = item.inputSelected;
          vm.createTypeModels(vm.templateInputsData);
          vm.selectedIndex = vm.index;
          vm.policiesAffected = policiesAffected;

        };

        function setTexts(texts) {
            vm.modalTexts = {};
            vm.modalTexts.title = texts.title;
            vm.modalTexts.button = texts.button;
            vm.modalTexts.icon = texts.button_icon;
        };

        function createTypeModels(fragmentData) {
            /*Creating one properties model for each input type*/
            for (var i=0; i<fragmentData.length; i++){
                //var differentObjects = false;

                var fragmentName = fragmentData[i].name;
                vm.properties[fragmentName] = {};

                /*Flag to check if there are any visible field*/
                vm.fragmentTemplateData[fragmentName] = $filter('filter')(fragmentData[i].properties, {'visible': []}, true);
                vm.properties[fragmentName]._visible = (vm.fragmentTemplateData[fragmentName].length > 0) ? true : false;

/*
                var selectValue = $filter('filter')(fragmentData[i].properties, {'values': []}, true)[0];

                if (selectValue){
                  for (var k=0; k < selectValue.values.length; k++){
                    vm.properties[fragmentName][selectValue.values[k].value] = {};
                    vm.properties[fragmentName][selectValue.values[k].value][selectValue.propertyId] = selectValue.values[k].value;
                  }
                  differentObjects = true;
                  vm.properties[fragmentName].select = true;
                }
*/
                for (var j=0; j<fragmentData[i].properties.length; j++) {
                    var fragmentProperty = fragmentData[i].properties[j];
                    //var dif = (fragmentProperty.visible) ? fragmentProperty.visible[0][0].value : '';

                    switch (fragmentProperty.propertyType) {
                      case 'boolean':
                        /*
                        if (dif !== '') {
                          vm.properties[fragmentName][dif][fragmentProperty.propertyId] = false;
                        }
                        else {
                          vm.properties[fragmentName][fragmentProperty.propertyId] = false;
                        }
                        */
                        vm.properties[fragmentName][fragmentProperty.propertyId] = false;
                        break;

                      case 'list':
                        /*
                        if (dif !== '') {
                          vm.properties[fragmentName][dif][fragmentProperty.propertyId] = [];
                          var newFields = {};

                          for (var m=0; m<fragmentProperty.fields.length; m++) {
                            var defaultValue = (fragmentProperty.fields[m].default) ? fragmentProperty.fields[m].default : '';
                            defaultValue = (fragmentProperty.fields[m].propertyType === 'number') ? parseInt(defaultValue) : defaultValue;
                            newFields[fragmentProperty.fields[m].propertyId] = defaultValue;
                          }
                          vm.properties[fragmentName][dif][fragmentProperty.propertyId].push(newFields);
                        }
                        else {
                          vm.properties[fragmentName][fragmentProperty.propertyId] = [];
                          var newFields = {};

                          for (var m=0; m<fragmentProperty.fields.length; m++) {
                            var defaultValue = (fragmentProperty.fields[m].default) ? fragmentProperty.fields[m].default : '';
                            defaultValue = (fragmentProperty.fields[m].propertyType === 'number') ? parseInt(defaultValue) : defaultValue;
                            newFields[fragmentProperty.fields[m].propertyId] = defaultValue;
                          }
                          vm.properties[fragmentName][fragmentProperty.propertyId].push(newFields);
                        }
                        */
                        vm.properties[fragmentName][fragmentProperty.propertyId] = [];
                        var newFields = {};

                        for (var m=0; m<fragmentProperty.fields.length; m++) {
                          var defaultValue = (fragmentProperty.fields[m].default) ? fragmentProperty.fields[m].default : '';
                          defaultValue = (fragmentProperty.fields[m].propertyType === 'number') ? parseInt(defaultValue) : defaultValue;
                          newFields[fragmentProperty.fields[m].propertyId] = defaultValue;
                        }
                        vm.properties[fragmentName][fragmentProperty.propertyId].push(newFields);
                        break;

                      default:
                        var defaultValue = (fragmentProperty.default) ? fragmentProperty.default : '';
                        defaultValue = (fragmentProperty.propertyType === 'number') ? parseInt(defaultValue) : defaultValue;
                        /*
                        if (dif !== '') {
                          vm.properties[fragmentName][dif][fragmentProperty.propertyId] = defaultValue;
                        }
                        else {
                          vm.properties[fragmentName][fragmentProperty.propertyId] = defaultValue;
                        }
                        */
                        vm.properties[fragmentName][fragmentProperty.propertyId] = defaultValue;
                        break;
                    }
                }

                /*Init properties*/
                if(fragmentName === vm.dataSource.element.type) {
                  console.log('**');
                  console.log(vm.dataSource.element);
                  console.log(vm.dataSource.element.configuration);
                  console.log(vm.properties);
/*
                  angular.forEach(vm.dataSource.element.configuration, function(value, key) {
                    console.log(key + ': ' + value);
                  });
*/
/*
                  vm.properties[fragmentName] = vm.dataSource.element.configuration;
                  vm.dataSource.element.configuration = vm.properties[fragmentName];
*/
/*
                  if (differentObjects) {
                    console.log(vm.dataSource.element.configuration.addresses);
                    vm.properties[fragmentName][vm.dataSource.element.configuration.type] = vm.dataSource.element.configuration;
                  }
                  else {
                    vm.properties[fragmentName] = vm.dataSource.element.configuration;
                  }
*/

                  angular.forEach(vm.dataSource.element.configuration, function(value, key) {
                    vm.properties[fragmentName][key] = value;
                  });

                  vm.dataSource.element.configuration = vm.properties[fragmentName];
                  vm.index = i;
                }
            }
        };

        function setProperties(index, inputName) {
            vm.selectedIndex = index;
            vm.dataSource.element.configuration = (vm.properties[inputName].select) ? vm.properties[inputName][vm.properties[inputName].type] : vm.properties[inputName];
            vm.setFragmentData(index);
        };

        function setFragmentData(index) {
            /*Set fragment*/
            vm.dataSource.description = vm.templateInputsData[index].description.long;
            vm.dataSource.shortDescription = vm.templateInputsData[index].description.short;
            vm.dataSource.icon = vm.templateInputsData[index].icon.url;
            vm.dataSource.element.name = 'in-' + vm.dataSource.element.type;
        };

        function ok() {
          if (vm.form.$valid) {
            deleteNotVisibleProperties();
            checkFragmnetname();
          }
        };

        function deleteNotVisibleProperties() {
          if (vm.dataSource.element.configuration._visible){
            var fragmentType = vm.dataSource.element.type;

            for (var i=0; i<vm.fragmentTemplateData[fragmentType].length; i++) {
              var propertyId = vm.fragmentTemplateData[fragmentType][i].propertyId;
              var originalProperty = vm.fragmentTemplateData[fragmentType][i].visible[0][0].propertyId;
              var originalPropertyValue = vm.fragmentTemplateData[fragmentType][i].visible[0][0].value;

              if (vm.dataSource.element.configuration[originalProperty] !== originalPropertyValue) {
                delete vm.dataSource.element.configuration[propertyId]
              }
            }
          }
          delete vm.dataSource.element.configuration['_visible'];
        };

        function checkFragmnetname() {
          var inputNameExist = [];
          inputNameExist = $filter('filter')(item.inputNamesList, {'name': vm.dataSource.name}, true);

          if (inputNameExist.length > 0 && inputNameExist[0].name !== vm.originalName) {
            vm.error = true;
            vm.errorText = "_INPUT_ERROR_100_";
          }
          else {
            editfragment();
          }
        };

        function editfragment() {
          var updateFragment = FragmentFactory.UpdateFragment(vm.dataSource);

          updateFragment.then(function (result) {
            console.log('*********Fragment updated');
            console.log(result);

            var callBackData = {
              'index': item.index,
              'data': result,
            };

            $modalInstance.close(callBackData);

          },function (error) {
            vm.error = true;
            vm.errorText = "_INPUT_ERROR_" + error.data.i18nCode + "_";
          });
        };

        function cancel() {
            $modalInstance.dismiss('cancel');
        };
    };
})();
