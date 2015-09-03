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
      vm.outputsData = [];
      vm.outputTypes = [];

      init();

      /////////////////////////////////

      function init() {
      };

      function createOutput() {
        var outputsList = getFragmentsNames(vm.outputsData);

        var createOutputData = {
          'fragmentType': 'output',
          'inputNamesList' : outputsList,
          'texts': {
            'title': '_OUTPUT_WINDOW_NEW_TITLE_',
            'button': '_OUTPUT_WINDOW_NEW_BUTTON_',
            'button_icon': 'icon-circle-plus'
          }
        };

        createOutputModal(createOutputData);
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
              return TemplateFactory.GetNewFragmentTemplate(newOutputTemplateData.fragmentType);
            }
          }
        });

        modalInstance.result.then(function (newOutputData) {
          console.log('*************** Controller back');
          console.log(newOutputData);

          vm.outputsData.push(newOutputData);
          console.log(vm.outputsData);

        }, function () {
          console.log('Modal dismissed at: ' + new Date())
        });
      };
    };
})();
