(function() {
    'use strict';

    angular
        .module('webApp')
        .factory('TemplateDataService', TemplateDataService);

    TemplateDataService.$inject = ['ApiTemplateService'];

    function TemplateDataService(ApiTemplateService) {
        return {
            GetNewFragmentTemplate: function(fragmentType) {
                return ApiTemplateService.GetFragmentTemplateByType().get({'type': fragmentType}).$promise;
            }
        };
    };
})();
