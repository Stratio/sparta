(function() {
  'use strict';

    /*NEW FRAGMENT MODAL CONTROLLER*/
    angular
        .module('webApp')
        .controller('NewFragmentModalCtrl', NewFragmentModalCtrl);

    NewFragmentModalCtrl.$inject = ['$modalInstance', 'item', 'FragmentFactory', 'TemplateFactory', '$filter'];

    function NewFragmentModalCtrl($modalInstance, item, FragmentFactory, TemplateFactory, $filter) {
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
        vm.errorText = '';
        vm.fragmentType = '';

        init();

        /////////////////////////////////

        function init() {
          console.log('--> NewFragmentModalCtrl');
          console.log('> Data received');
          console.log(item);

          vm.fragmentType = item.fragmentType;

          setTexts(item.texts);
          getTemplates(item.fragmentType);
        };

        function setTexts(texts) {
          vm.modalTexts = {};
          vm.modalTexts.title = texts.title;
          vm.modalTexts.button = texts.button;
          vm.modalTexts.icon = texts.button_icon;
        };

        function getTemplates(fragmentType) {
          var fragmentTemplates = TemplateFactory.GetNewFragmentTemplate(fragmentType);

          fragmentTemplates.then(function (result) {
            console.log('*********Templates result');
            console.log(result);

            vm.templateInputsData = result;

            vm.initFragmentObject(vm.templateInputsData);
            vm.createTypeModels(vm.templateInputsData);
            vm.selectedIndex = 0;

          },function (error) {
              vm.error = true;
              vm.errorText = "_INPUT_ERROR_TEMPLATES_";
          });
        };

        function initFragmentObject(fragmentData) {
            /*Init fragment*/
            vm.dataSource.fragmentType = vm.fragmentType;
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
                if(i === 0) {
                  console.log(differentObjects);
                  console.log(vm.properties[fragmentName]);
                  console.log([selectValue.values[0].value]);
                  vm.dataSource.element.configuration = (differentObjects) ? vm.properties[fragmentName][selectValue.values[0].value] : vm.properties[fragmentName];
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
          if (vm.form.$valid) {
            checkFragmnetname();
          }
        };

        function checkFragmnetname() {
          var inputNameExist = [];
          inputNameExist = $filter('filter')(item.inputNamesList, {'name': vm.dataSource.name}, true);

          if (inputNameExist.length > 0) {
            vm.error = true;
            vm.errorText = "_INPUT_ERROR_100_";
          }
          else {
            createfragment();
          }
        };

        function createfragment() {
          var newFragment = FragmentFactory.CreateFragment(vm.dataSource);

          newFragment.then(function (result) {
              console.log('*********Fragment created');
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
