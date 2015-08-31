(function() {
  'use strict';

    /*NEW & EDIT FRAGMENT MODAL CONTROLLER*/
    angular
        .module('webApp')
        .controller('NewFragmentModalCtrl', NewFragmentModalCtrl);

    NewFragmentModalCtrl.$inject = ['$modalInstance', 'item', 'FragmentFactory', '$filter'];

    function NewFragmentModalCtrl($modalInstance, item, FragmentFactory, $filter) {
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
        vm.templateInputsData = [];
        vm.properties = [];
        vm.error = false;

        init();

        /////////////////////////////////

        function init() {
            console.log('--> NewFragmentModalCtrl');
            console.log('> Data received');
            console.log(item);

            setTexts(item.texts);

            vm.action = item.action;

            if (vm.action === 'edit') {
                vm.templateInputsData = item.inputDataTemplate;
                vm.dataSource = item.inputSelected;
                vm.createTypeModels(vm.templateInputsData);
                vm.selectedIndex = vm.index;
                vm.policiesAffected = item.policies;
            }
            else {
                vm.templateInputsData = item.inputDataTemplate;
                vm.initFragmentObject(vm.templateInputsData);
                vm.createTypeModels(vm.templateInputsData);
                vm.selectedIndex = 0;
            }
        };

        function setTexts(texts) {
            vm.modalTexts = {};
            vm.modalTexts.title = texts.title;
            vm.modalTexts.button = texts.button;
            vm.modalTexts.icon = texts.button_icon;
        }

        function initFragmentObject(fragmentData) {
            /*Init fragment*/
            vm.dataSource.fragmentType = 'input';
            vm.dataSource.name = '';

            /*Init fragment.element*/
            vm.dataSource.element.type = fragmentData[0].name;
            vm.dataSource.element.name = 'in-' + vm.dataSource.element.type;

            /*Init fragment.element.configuration*/
            vm.dataSource.element.configuration = {};

            vm.setFragmentData(0);
        };

        function createTypeModels(fragmentData) {
            /*Creating one properties model for each input type*/
            for (var i=0; i<fragmentData.length; i++){
                var differentObjects = false;

                var fragmentName = fragmentData[i].name;
                vm.properties[fragmentName] = {};

                var selectValue = $filter('filter')(fragmentData[i].properties, {'values': []}, true)[0];

                if (selectValue){
                  for (var k=0; k < selectValue.values.length; k++){
                    vm.properties[fragmentName][selectValue.values[k].value] = {};
                    vm.properties[fragmentName][selectValue.values[k].value][selectValue.propertyId] = selectValue.values[k].value;
                  }
                  differentObjects = true;
                  vm.properties[fragmentName].select = true;
                }

                for (var j=0; j<fragmentData[i].properties.length; j++) {
                    var fragmentProperty = fragmentData[i].properties[j];
                    var dif = (fragmentProperty.visible) ? fragmentProperty.visible[0][0].value : '';

                    switch (fragmentProperty.propertyType) {
                      case 'boolean':
                        if (dif !== '') {
                          vm.properties[fragmentName][dif][fragmentProperty.propertyId] = false;
                        }
                        else {
                          vm.properties[fragmentName][fragmentProperty.propertyId] = false;
                        }
                        break;

                      case 'list':
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
                        break;

                      default:
                        var defaultValue = (fragmentProperty.default) ? fragmentProperty.default : '';
                        defaultValue = (fragmentProperty.propertyType === 'number') ? parseInt(defaultValue) : defaultValue;

                        if (dif !== '') {
                          vm.properties[fragmentName][dif][fragmentProperty.propertyId] = defaultValue;
                        }
                        else {
                          vm.properties[fragmentName][fragmentProperty.propertyId] = defaultValue;
                        }
                        break;
                    }
                }

                /*Init properties*/
                if(vm.action !== 'edit' && i === 0) {
                  console.log(differentObjects);
                  console.log(vm.properties[fragmentName]);
                  console.log([selectValue.values[0].value]);
                  vm.dataSource.element.configuration = (differentObjects) ? vm.properties[fragmentName][selectValue.values[0].value] : vm.properties[fragmentName];
                }
                else if(vm.action === 'edit' && fragmentName === vm.dataSource.element.type) {
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
                  if (differentObjects) {
                    console.log(vm.dataSource.element.configuration.addresses);
                    vm.properties[fragmentName][vm.dataSource.element.configuration.type] = vm.dataSource.element.configuration;
                  }
                  else {
                    vm.properties[fragmentName] = vm.dataSource.element.configuration;
                  }
                  /*vm.dataSource.element.configuration = (differentObjects) ?  : ;*/
                  vm.index = i;
                }
            }
        };

        function setFragmentData(index) {
            /*Set fragment*/
            vm.dataSource.description = vm.templateInputsData[index].description.long;
            vm.dataSource.shortDescription = vm.templateInputsData[index].description.short;
            vm.dataSource.icon = vm.templateInputsData[index].icon.url;
            vm.dataSource.element.name = 'in-' + vm.dataSource.element.type;
        };

        function setProperties(index, inputName) {
            vm.selectedIndex = index;
            vm.dataSource.element.configuration = (vm.properties[inputName].select) ? vm.properties[inputName][vm.properties[inputName].type] : vm.properties[inputName];
            vm.setFragmentData(index);
        };

        function ok() {
          console.log(vm.dataSource);
          if (vm.form.$valid){
              checkInputName(vm.dataSource.fragmentType, vm.dataSource.name);
          }
        };

        function checkInputName(inputType, inputName) {
            var newFragment = FragmentFactory.GetFragmentById(inputType, inputName);

            newFragment
            .then(function (result) {
                vm.error = true;
            },
            function (error) {
                var callBackData = {
                    'index': item.index,
                    'data': vm.dataSource,
                };
               $modalInstance.close(callBackData);
            });
        };

        function cancel() {
            $modalInstance.dismiss('cancel');
        };
    };
})();
