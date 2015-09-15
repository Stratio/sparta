(function() {
    'use strict';

    angular
      .module('webApp')
      .controller('OutputsCtrl', OutputsCtrl);

    OutputsCtrl.$inject = ['FragmentFactory', '$filter', '$modal'];

    function OutputsCtrl(FragmentFactory, $filter, $modal) {
      /*jshint validthis: true*/
      var vm = this;

      vm.createOutput = createOutput;
      vm.editOutput = editOutput;
      vm.deleteOutput = deleteOutput;
      vm.duplicateOutput = duplicateOutput;
      vm.outputsData = [];
      vm.outputTypes = [];

      init();

      /////////////////////////////////

      function init() {
        getOutputs();
      };

      function getOutputs() {
        var outputList = FragmentFactory.getFragments('output');

        outputList.then(function (result) {
          vm.outputsData = result;
          getOutputTypes(result);

        },function (error) {
          console.log('There was an error while loading the output list!');
          console.log(error);
        });
      };

      function getOutputTypes(outputs) {
        for (var i=0; i<outputs.length; i++) {
            var newType = false;
            var type    = outputs[i].element.type;

            if (i === 0) {
                vm.outputTypes.push({'type': type, 'count': 1});
            }
            else {
                for (var j=0; j<vm.outputTypes.length; j++) {
                    if (vm.outputTypes[j].type === type) {
                        vm.outputTypes[j].count++;
                        newType = false;
                        break;
                    }
                    else if (vm.outputTypes[j].type !== type){
                        newType = true;
                    }
                }
                if (newType) {
                    vm.outputTypes.push({'type': type, 'count':1});
                }
            }
        }
      };

      function createOutput() {
        var outputsList = getFragmentsNames(vm.outputsData);

        var createOutputData = {
          'fragmentType': 'output',
          'fragmentNamesList' : outputsList,
          'texts': {
            'title': '_OUTPUT_WINDOW_NEW_TITLE_',
            'button': '_OUTPUT_WINDOW_NEW_BUTTON_',
            'button_icon': 'icon-circle-plus'
          }
        };

        createOutputModal(createOutputData);
      };

      function editOutput(outputType, outputName, outputId, index) {
        var outputSelected = $filter('filter')(angular.copy(vm.outputsData), {'id':outputId}, true)[0];
        var outputsList = getFragmentsNames(vm.outputsData);

        var editOutputData = {
            'originalName': outputName,
            'fragmentType': 'output',
            'index': index,
            'fragmentSelected': outputSelected,
            'fragmentNamesList' : outputsList,
            'texts': {
                'title': '_OUTPUT_WINDOW_MODIFY_TITLE_',
                'button': '_OUTPUT_WINDOW_MODIFY_BUTTON_',
                'button_icon': 'icon-circle-check',
                'secondaryText2': '_OUTPUT_WINDOW_EDIT_MESSAGE2_'
            }
        };

        editOutputModal(editOutputData);
      };

      function deleteOutput(fragmentType, fragmentId, index) {
               var outputToDelete =
        {
          'type':fragmentType,
          'id': fragmentId,
          'index': index,
          'texts': {
            'title': '_OUTPUT_WINDOW_DELETE_TITLE_',
            'mainText': '_OUTPUT_CANNOT_BE_DELETED_',
            'mainTextOK': '_ARE_YOU_COMPLETELY_SURE_',
            'secondaryText1': '_OUTPUT_WINDOW_DELETE_MESSAGE_',
            'secondaryText2': '_OUTPUT_WINDOW_DELETE_MESSAGE2_'
          }
        };
        deleteOutputConfirm('lg', outputToDelete);
      };

      function duplicateOutput(outputId) {
        var outputSelected = $filter('filter')(angular.copy(vm.outputsData), {'id':outputId}, true)[0];

        var newName = autoIncrementName(outputSelected.name);
        outputSelected.name = newName;

        var outputsList = getFragmentsNames(vm.outputsData);

        var duplicateOutputData = {
          'fragmentData': outputSelected,
          'fragmentNamesList': outputsList,
          'texts': {
            'title': '_OUTPUT_WINDOW_DUPLICATE_TITLE_'
          }
        };

        setDuplicatetedOutput('sm', duplicateOutputData);
      };

      function createOutputModal(newOutputTemplateData) {
        var modalInstance = $modal.open({
          animation: true,
          templateUrl: 'templates/fragments/fragment-details.tpl.html',
          controller: 'NewFragmentModalCtrl as vm',
          size: 'lg',
          resolve: {
            item: function () {
              return newOutputTemplateData;
            },
            fragmentTemplates: function (TemplateFactory) {
              return TemplateFactory.getNewFragmentTemplate(newOutputTemplateData.fragmentType);
            }
          }
        });

        modalInstance.result.then(function (newOutputData) {
          vm.outputsData.push(newOutputData);
        }, function () {
        });
      };

      function editOutputModal(editOutputData) {
         var modalInstance = $modal.open({
             animation: true,
             templateUrl: 'templates/fragments/fragment-details.tpl.html',
             controller: 'EditFragmentModalCtrl as vm',
             size: 'lg',
             resolve: {
                 item: function () {
                    return editOutputData;
                 },
                 fragmentTemplates: function (TemplateFactory) {
                    return TemplateFactory.getNewFragmentTemplate(editOutputData.fragmentSelected.fragmentType);
                 },
                 policiesAffected: function (PolicyFactory) {
                    return PolicyFactory.getPolicyByFragmentId(editOutputData.fragmentSelected.fragmentType, editOutputData.fragmentSelected.id);
                 }
             }
         });

        modalInstance.result.then(function (updatedOutputData) {
          vm.outputsData[updatedOutputData.index] = updatedOutputData.data;

        },function () {
                });
      };

      function deleteOutputConfirm(size, output) {
        var modalInstance = $modal.open({
          animation: true,
          templateUrl: 'templates/components/st-delete-modal.tpl.html',
          controller: 'DeleteFragmentModalCtrl as vm',
          size: size,
          resolve: {
              item: function () {
                  return output;
              },
              policiesAffected: function (PolicyFactory) {
                return PolicyFactory.GetPolicyByFragmentId(output.type, output.id);
              }
          }
        });

        modalInstance.result.then(function (selectedItem) {
          vm.outputsData.splice(selectedItem.index, 1);
        },function () {
        });
      };

      function setDuplicatetedOutput(size, OutputData) {
        var modalInstance = $modal.open({
          animation: true,
          templateUrl: 'templates/components/st-duplicate-modal.tpl.html',
          controller: 'DuplicateFragmentModalCtrl as vm',
          size: 'lg',
          resolve: {
              item: function () {
                  return OutputData;
              }
          }
        });

        modalInstance.result.then(function (newOutput) {
          vm.outputsData.push(newOutput);

        },function () {
        });
      };

    };
})();
