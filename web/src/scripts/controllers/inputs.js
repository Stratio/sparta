(function() {
    'use strict';

    angular
        .module('webApp')
        .controller('InputsCtrl', InputsCtrl);

    InputsCtrl.$inject = ['FragmentFactory', 'PolicyFactory', 'TemplateFactory', '$filter', '$modal'];

    function InputsCtrl(FragmentFactory, PolicyFactory, TemplateFactory, $filter, $modal) {
        /*jshint validthis: true*/
       var vm = this;

       vm.getInputs = getInputs;
       vm.deleteInput = deleteInput;
       vm.getInputTypes = getInputTypes;
       vm.createInput = createInput;
       vm.editInput = editInput;
       vm.createInputModal = createInputModal;
       vm.editInputModal = editInputModal;
       vm.duplicateInput = duplicateInput;
       vm.deleteInputConfirm = deleteInputConfirm;
       vm.getPolicyNames = getPolicyNames;
       vm.inputTypes = [];

       init();

        /////////////////////////////////

        function init() {
            getInputs();
        };

        function getInputs() {
            var inputList = FragmentFactory.GetFragments("input");

            inputList.then(function (result) {
                vm.inputsData = result;
                vm.getInputTypes(result);
                console.log(vm.inputsData);
            });
        };

        function deleteInput(fragmentType, fragmentId, index) {
            console.log('--> Deleting input');
            console.log('> Getting Policies affected');
            var policiesToDelete = PolicyFactory.GetPolicyByFragmentId(fragmentType, fragmentId);

            policiesToDelete.then(function (result) {
                console.log(result);

                var policies = vm.getPolicyNames(result);
                var inputToDelete =
                {
                    'type':fragmentType,
                    'id': fragmentId,
                    'policies': policies,
                    'index': index
                };
                vm.deleteInputConfirm('lg', inputToDelete);
            },
            function (error) {
              console.log('#ERROR#');
              console.log(error);
            });
        };

        function duplicateInput(inputName) {
            var inputSelected = $filter('filter')(angular.copy(vm.inputsData), {name:inputName}, true)[0];

            var newName = autoIncrementName(inputSelected.name);
            inputSelected.name = newName;

            var newName = SetDuplicatetedInput('sm', inputSelected);
       };

        function createInput() {
           var inputFragmentTemplate = TemplateFactory.GetNewFragmentTemplate('input');

           inputFragmentTemplate.then(function (result) {
                console.log('*********Controller');
                console.log(result);

                var fakeData =
                [
                  {
                    "name": "Flume",
                    "modelType": "Flume",
                    "description": {
                      "short": "Reads events from flume",
                      "long": "Reads events from flume",
                      "learnMore": "http://docs.stratio.com/modules/sparkta/0.6/inputs.html#flume-label"
                    },
                    "icon": {
                      "url": "logo_flume.png"
                    },
                    "properties": [
                      {
                        "propertyId": "type",
                        "propertyName": "Type",
                        "propertyType": "select",
                        "values": [
                          {
                            "label": "pull",
                            "value": "pull"
                          },
                          {
                            "label": "push",
                            "value": "push"
                          }
                        ],
                        "regexp": "pull|push",
                        "default": "pull",
                        "required": true,
                        "tooltip": ""
                      },
                      {
                      "propertyId": "addresses",
                      "propertyName": "Addresses",
                      "propertyType": "list",
                      "default": "",
                      "required": false,
                      "hidden": false,
                      "limit": 0,
                      "tooltip": "",
                      "visible": [
                          [
                            {
                              "propertyId": "type",
                              "value": "pull",
                              "overrideProps": [
                                {
                                  "label": "required",
                                  "value": "true"
                                }
                              ]
                            }
                          ]
                        ],
                      "fields": [
                        {
                          "propertyId": "address",
                          "propertyName": "Address",
                          "propertyType": "text",
                          "regexp": "",
                          "default": "aa",
                          "required": false,
                          "tooltip": "Flume's address.",
                          "hidden": false
                        },
                        {
                          "propertyId": "port",
                          "propertyName": "Port",
                          "propertyType": "number",
                          "regexp": "",
                          "default": 1000,
                          "required": false,
                          "tooltip": "Flume's port.",
                          "hidden": false
                        }
                      ]
                      },
                      {
                        "propertyId": "maxBatchSize",
                        "propertyName": "Max batch size",
                        "propertyType": "number",
                        "regexp": "",
                        "default": 1000,
                        "required": false,
                        "tooltip": "",
                        "hidden": false,
                        "visible": [
                          [
                            {
                              "propertyId": "type",
                              "value": "pull"
                            }
                          ]
                        ]
                      },
                      {
                        "propertyId": "parallelism",
                        "propertyName": "Parallelism",
                        "propertyType": "number",
                        "regexp": "",
                        "default": 5,
                        "required": false,
                        "tooltip": "",
                        "hidden": false,
                        "visible": [
                          [
                            {
                              "propertyId": "type",
                              "value": "pull"
                            }
                          ]
                        ]
                      },
                      {
                        "propertyId": "hostName",
                        "propertyName": "Host name",
                        "propertyType": "text",
                        "regexp": "((([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5]))|(((?![0-9]+$)(?!.*-$)(?!-)[a-zA-Z0-9-]{2,63}))",
                        "default": "",
                        "required": true,
                        "tooltip": "You must set the Flume host IP or name.",
                        "hidden": false,
                        "visible": [
                          [
                            {
                              "propertyId": "type",
                              "value": "push"
                            }
                          ]
                        ]
                      },
                      {
                        "propertyId": "port",
                        "propertyName": "Port",
                        "propertyType": "number",
                        "regexp": "(0|([1-9]\\d{0,3}|[1-5]\\d{4}|[6][0-5][0-5]([0-2]\\d|[3][0-5])))",
                        "default": 11999,
                        "required": true,
                        "tooltip": "Flume port",
                        "hidden": false,
                        "visible": [
                          [
                            {
                              "propertyId": "type",
                              "value": "push"
                            }
                          ]
                        ]
                      },
                      {
                        "propertyId": "enableDecompression",
                        "propertyName": "Decompression",
                        "propertyType": "boolean",
                        "regexp": "true|false",
                        "required": false,
                        "tooltip": "",
                        "hidden": false,
                        "visible": [
                          [
                            {
                              "propertyId": "type",
                              "value": "push"
                            }
                          ]
                        ]
                      }
                    ]
                  },
                  {
                    "name": "Kafka",
                    "modelType": "Kafka",
                    "description": {
                      "short": "Reads events from apache-kafka",
                      "long": "Reads events from apache-kafka",
                      "learnMore": "http://docs.stratio.com/modules/sparkta/0.6/inputs.html#kafka-label"
                    },
                    "icon": {
                      "url": "logo_kafka.png"
                    },
                    "properties": [
                      {
                        "propertyId": "kafkaParams.zookeeper.connect",
                        "propertyName": "Zookeeper host",
                        "propertyType": "text",
                        "regexp": "((([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5]))|(((?![0-9]+$)(?!.*-$)(?!-)[a-zA-Z0-9-]{2,63}))(:(0|([1-9]\\d{0,3}|[1-5]\\d{4}|[6][0-5][0-5]([0-2]\\d|[3][0-5]))))",
                        "default": "localhost:2181",
                        "required": true,
                        "tooltip": ""
                      },
                      {
                        "propertyId": "kafkaParams.group.id",
                        "propertyName": "Type",
                        "propertyType": "text",
                        "regexp": "",
                        "required": true,
                        "tooltip": ""
                      },
                      {
                        "propertyId": "topics",
                        "propertyName": "Topics",
                        "propertyType": "text",
                        "regexp": "TODO cualquier cadena de caracteres",
                        "required": true,
                        "tooltip": "Topics to connect"
                      }
                    ]
                  },
                  {
                    "name": "Kafka Direct",
                    "modelType": "KafkaDirect",
                    "description": {
                      "short": "Reads events from apache-kafka",
                      "long": "Reads events from apache-kafka",
                      "learnMore": "http://docs.stratio.com"
                    },
                    "icon": {
                      "url": "logo_kafka.png"
                    },
                    "properties": [
                      {
                        "propertyId": "kafkaParams.zookeeper.connect",
                        "propertyName": "Zookeeper host",
                        "propertyType": "text",
                        "regexp": "((([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5]))|(((?![0-9]+$)(?!.*-$)(?!-)[a-zA-Z0-9-]{2,63}))(:(0|([1-9]\\d{0,3}|[1-5]\\d{4}|[6][0-5][0-5]([0-2]\\d|[3][0-5]))))",
                        "default": "localhost:2181",
                        "required": true,
                        "tooltip": ""
                      },
                      {
                        "propertyId": "kafkaParams.group.id",
                        "propertyName": "Type",
                        "propertyType": "text",
                        "regexp": "",
                        "required": true,
                        "tooltip": ""
                      },
                      {
                        "propertyId": "topics",
                        "propertyName": "Topics",
                        "propertyType": "text",
                        "regexp": "TODO cualquier cadena de caracteres",
                        "required": true,
                        "tooltip": "Topics to connect"
                      }
                    ]
                  },
                  {
                    "name": "RabbitMQ",
                    "modelType": "RabbitMQ",
                    "description": {
                      "short": "Reads events from RabbitMQ",
                      "long": "Reads events from RabbitMQ",
                      "learnMore": "http://docs.stratio.com"
                    },
                    "icon": {
                      "url": "logo_rabbitmq.png"
                    },
                    "properties": [
                      {
                        "propertyId": "queue",
                        "propertyName": "Queue",
                        "propertyType": "text",
                        "regexp": "TODO cualquier cadena de caracteres",
                        "required": false,
                        "tooltip": "RabbitMQ's name queue."
                      },
                      {
                        "propertyId": "host",
                        "propertyName": "Host",
                        "propertyType": "text",
                        "regexp": "((([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5]))|(((?![0-9]+$)(?!.*-$)(?!-)[a-zA-Z0-9-]{2,63}))",
                        "default": "localhost",
                        "required": true,
                        "tooltip": "RabbitMQ's hostname."
                      },
                      {
                        "propertyId": "port",
                        "propertyName": "Port",
                        "propertyType": "text",
                        "regexp": "(0|([1-9]\\d{0,3}|[1-5]\\d{4}|[6][0-5][0-5]([0-2]\\d|[3][0-5])))",
                        "default": "5672",
                        "required": true,
                        "tooltip": ""
                      },
                      {
                        "propertyId": "exchangeName",
                        "propertyName": "ExchangeName",
                        "propertyType": "text",
                        "regexp": "",
                        "default": "",
                        "required": false,
                        "tooltip": "Name of the exchange where the data will be consumed"
                      },
                      {
                        "propertyId": "routingKeys",
                        "propertyName": "RoutingKeys",
                        "propertyType": "text",
                        "regexp": "",
                        "required": false,
                        "tooltip": "The exchange will delivery the messages to all the routing keys"
                      }
                    ]
                  },
                  {
                    "name": "Socket",
                    "modelType": "Socket",
                    "description": {
                      "short": "Reads events from a socket.",
                      "long": "Reads events from a socket.",
                      "learnMore": "http://docs.stratio.com/modules/sparkta/0.6/inputs.html#socket-label"
                    },
                    "icon": {
                      "url": "logo_socket.png"
                    },
                    "properties": [
                      {
                        "propertyId": "hostname",
                        "propertyName": "Hostname",
                        "propertyType": "text",
                        "regexp": "((([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5])\\.){3}([0-9]|[1-9][0-9]|1[0-9]{2}|2[0-4][0-9]|25[0-5]))|(((?![0-9]+$)(?!.*-$)(?!-)[a-zA-Z0-9-]{2,63}))",
                        "default": "",
                        "required": true,
                        "tooltip": "Socket hostname"
                      },
                      {
                        "propertyId": "port",
                        "propertyName": "Port",
                        "propertyType": "text",
                        "regexp": "(0|([1-9]\\d{0,3}|[1-5]\\d{4}|[6][0-5][0-5]([0-2]\\d|[3][0-5])))",
                        "default": "",
                        "required": true,
                        "tooltip": "Socket port"
                      }
                    ]
                  },
                  {
                    "name": "Twitter",
                    "modelType": "Twitter",
                    "description": {
                      "short": "Reads events from Twitter stream.",
                      "long": "Reads events from Twitter stream.",
                      "learnMore": "http://docs.stratio.com/modules/sparkta/0.6/inputs.html#twitter-label"
                    },
                    "icon": {
                      "url": "logo_twitter.png"
                    },
                    "properties": [
                      {
                        "propertyId": "consumerKey",
                        "propertyName": "Consumer key",
                        "propertyType": "text",
                        "regexp": "",
                        "default": "",
                        "required": true,
                        "tooltip": "Twitter's consumer key."
                      },
                      {
                        "propertyId": "consumerSecret",
                        "propertyName": "Consumer secret",
                        "propertyType": "text",
                        "regexp": "",
                        "default": "",
                        "required": true,
                        "tooltip": "Twitter's consumer secret"
                      },
                      {
                        "propertyId": "accessToken",
                        "propertyName": "Access token",
                        "propertyType": "text",
                        "regexp": "",
                        "default": "",
                        "required": true,
                        "tooltip": "Twitter's access token."
                      },
                      {
                        "propertyId": "tokenSecret",
                        "propertyName": "Token secret",
                        "propertyType": "text",
                        "regexp": "",
                        "default": "",
                        "required": true,
                        "tooltip": "Twitter's token secret."
                      },
                      {
                        "propertyId": "termsOfSearch",
                        "propertyName": "Terms of search",
                        "propertyType": "text",
                        "regexp": "",
                        "default": "",
                        "required": false,
                        "tooltip": "It allows to search tweets based on the words of this field. If you donâ€™t use this field, it searches over global trending topics."
                      }
                    ]
                  }
                ];

                var createInputData = {
                    'action': 'create',
                    'inputDataTemplate': fakeData,
                    'texts': {
                        'title': '_INPUT_WINDOW_NEW_TITLE_',
                        'button': '_INPUT_WINDOW_NEW_BUTTON_',
                        'button_icon': 'icon-circle-plus'
                    }
                };

               vm.createInputModal(createInputData);
           });
        };

        function editInput(inputType, inputName, inputId, index) {
           var inputSelected = $filter('filter')(angular.copy(vm.inputsData), {name:inputName}, true)[0];

           var inputFragmentTemplate = TemplateFactory.GetNewFragmentTemplate('input');

           inputFragmentTemplate.then(function (result) {
                console.log('--> Editing input');
                console.log('> Getting Fragment Template');
                console.log(result);
                console.log('> Getting Policies affected');
                var policiesAffected = PolicyFactory.GetPolicyByFragmentId(inputType, inputId);
                var inputDataTemplate = result;

                policiesAffected.then(function (result) {
                    console.log(result);

                    var policies = vm.getPolicyNames(result);
                    var editInputData = {
                        'index': index,
                        'action': 'edit',
                        'inputSelected': inputSelected,
                        'inputDataTemplate': inputDataTemplate,
                        'policies': policies,
                        'texts': {
                            'title': '_INPUT_WINDOW_MODIFY_TITLE_',
                            'button': '_INPUT_WINDOW_MODIFY_BUTTON_',
                            'button_icon': 'icon-circle-check'
                        }
                    };

                    vm.editInputModal(editInputData);
                },
                function (error) {
                  console.log('#ERROR#');
                  console.log(error);
                });
           });
        };

        function getPolicyNames(policiesData) {
            var policies = [];

            for (var i=0; i<policiesData.length; i++){
                policies.push(policiesData[i].name);
            }

            return policies;
        };

        function getInputTypes(inputs) {
            for (var i=0; i<inputs.length; i++) {
                var newType = false;
                var type    = inputs[i].element.type;

                if (i === 0) {
                    vm.inputTypes.push({'type': type, 'count': 1});
                }
                else {
                    for (var j=0; j<vm.inputTypes.length; j++) {
                        if (vm.inputTypes[j].type === type) {
                            vm.inputTypes[j].count++;
                            newType = false;
                            break;
                        }
                        else if (vm.inputTypes[j].type !== type){
                            newType = true;
                        }
                    }
                    if (newType) {
                        vm.inputTypes.push({'type': type, 'count':1});
                    }
                }
            }
        };

        function createInputModal(newInputTemplateData) {
            var modalInstance = $modal.open({
               animation: true,
               templateUrl: 'templates/inputs/input-details.tpl.html',
               controller: 'NewFragmentModalCtrl as vm',
               size: 'lg',
               resolve: {
                   item: function () {
                       return newInputTemplateData;
                   }
               }
            });

            modalInstance.result.
               then(function (newInputData) {
                   console.log('*************** Controller back');
                   console.log(newInputData);

                   var newFragment = FragmentFactory.CreateFragment(newInputData.data);

                   newFragment
                       .then(function (result) {
                           console.log('*********Fragment created');
                           console.log(result);

                           vm.inputsData.push(result);
                           console.log(vm.inputsData);
                       },
                       function (error) {
                           console.log(error);
                           console.log('Modal dismissed at: ' + new Date())
                       });

               }, function () {
                   console.log('Modal dismissed at: ' + new Date())
               });
            };

        function editInputModal(editInputData) {
           var modalInstance = $modal.open({
               animation: true,
               templateUrl: 'templates/inputs/input-details.tpl.html',
               controller: 'NewFragmentModalCtrl as vm',
               size: 'lg',
               resolve: {
                   item: function () {
                       return editInputData;
                   }
               }
           });

           modalInstance.result.
               then(function (updatedInputData) {
                   console.log('*************** Controller back');
                   console.log(updatedInputData);

                   var updatedFragment = FragmentFactory.UpdateFragment(updatedInputData.data);

                   updatedFragment
                       .then(function (result) {
                           console.log('*********Fragment updated');
                           console.log(result);

                           vm.inputsData[updatedInputData.index] = result;
                           console.log(vm.inputsData);
                       },
                       function (error) {
                           console.log(error);
                           console.log('Modal dismissed at: ' + new Date())
                       });

               }, function () {
                   console.log('Modal dismissed at: ' + new Date())
               });
        };

        function deleteInputConfirm(size, input) {
            var modalInstance = $modal.open({
                animation: true,
                templateUrl: 'templates/components/st-delete-modal.tpl.html',
                controller: 'DeleteFragmentModalCtrl',
                size: size,
                resolve: {
                    item: function () {
                        return input;
                    }
                }
            });

            modalInstance.result
                .then(function (selectedItem) {
                    console.log(selectedItem);
                    var fragmentDeleted = FragmentFactory.DeleteFragment(selectedItem.type, selectedItem.id);

                    fragmentDeleted
                        .then(function (result) {
                            console.log('*********Fragment deleted');
                            vm.inputsData.splice(selectedItem.index, 1);

                        });
                },
                function () {
                    console.log('Modal dismissed at: ' + new Date())
                });
        };

        function SetDuplicatetedInput(size, inputName) {
            var modalInstance = $modal.open({
                animation: true,
                templateUrl: 'templates/components/st-duplicate-modal.tpl.html',
                controller: 'DuplicateFragmentModalCtrl as vm',
                size: 'lg',
                resolve: {
                    item: function () {
                        return inputName;
                    }
                }
            });

            modalInstance.result
                .then(function (selectedItem) {
                    console.log(selectedItem);
                    delete selectedItem['id'];

                    var newFragment = FragmentFactory.CreateFragment(selectedItem);

                    newFragment
                        .then(function (result) {
                            console.log('*********Fragment created');
                            console.log(result);

                            vm.inputsData.push(result);
                            console.log(vm.inputsData);
                        },
                        function (error) {
                            console.log(error);
                            console.log('Modal dismissed at: ' + new Date())
                        });

                },
                function () {
                    console.log('Modal dismissed at: ' + new Date())
                });
        };
    };

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

    /*DELETE INPUT MODALS CONTROLLER */
    angular
        .module('webApp')
        .controller('DeleteFragmentModalCtrl', DeleteFragmentModalCtrl);

    DeleteFragmentModalCtrl.$inject = ['$scope', '$modalInstance', 'item'];

    function DeleteFragmentModalCtrl($scope, $modalInstance, item) {
        console.log('*********Modal');
        console.log(item);

        $scope.inputs = item;

        $scope.ok = function () {
            $modalInstance.close($scope.inputs);
        };

        $scope.cancel = function () {
            $modalInstance.dismiss('cancel');
        };
    };


    /*DUPLICATE INPUT MODAL CONTROLLER */
    angular
        .module('webApp')
        .controller('DuplicateFragmentModalCtrl', DuplicateFragmentModalCtrl);

    DuplicateFragmentModalCtrl.$inject = ['$modalInstance', 'item', 'FragmentFactory'];

    function DuplicateFragmentModalCtrl($modalInstance, item, FragmentFactory) {
        /*jshint validthis: true*/
        var vm = this;

        vm.ok = ok;
        vm.cancel = cancel;
        vm.error = false;

        init();

        ///////////////////////////////////////

        function init () {
            console.log('*********Modal');
            console.log(item);
            vm.inputData = item;
        };

        function ok() {
            if (vm.form.$valid){
                checkInputName(vm.inputData.fragmentType, vm.inputData.name);
            }
        };

        function checkInputName(inputType, inputName) {
            var newFragment = FragmentFactory.GetFragmentById(inputType, inputName);

            newFragment
            .then(function (result) {
                vm.error = true;
            },
            function (error) {
                $modalInstance.close(vm.inputData);
            });
        };

        function cancel() {
            $modalInstance.dismiss('cancel');
        };
    };
})();
