(function() {
    'use strict';

    angular
        .module('webApp')
        .factory('TemplateFactory', TemplateFactory);

    TemplateFactory.$inject = ['ApiTemplateService'];

    function TemplateFactory(ApiTemplateService) {
        return {
            GetNewFragmentTemplate: function(fragmentType) {
                return ApiTemplateService.GetFragmentTemplateByType().get({'type': fragmentType + '.json'}).$promise;
            }
        };
    };
})();
