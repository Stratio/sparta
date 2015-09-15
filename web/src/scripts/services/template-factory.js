(function() {
    'use strict';

    angular
        .module('webApp')
        .factory('TemplateFactory', TemplateFactory);

    TemplateFactory.$inject = ['ApiTemplateService'];

    function TemplateFactory(ApiTemplateService) {
        return {
            getNewFragmentTemplate: function(fragmentType) {
                return ApiTemplateService.getFragmentTemplateByType().get({'type': fragmentType + '.json'}).$promise;
            }
        };
    };
})();
