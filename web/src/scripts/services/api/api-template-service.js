(function() {
    'use strict';

    angular
        .module('webApp')
        .service('ApiTemplateService', ApiTemplateService);

    ApiTemplateService.$inject = ['$resource', 'apiConfigSettings'];

    function ApiTemplateService($resource, apiConfigSettings) {
        var vm = this;

        vm.getFragmentTemplateByType = getFragmentTemplateByType;

        /////////////////////////////////

        function getFragmentTemplateByType() {
            return $resource('/data-templates/:type', {type:'@type'},
            {
                'get'   : {method:'GET', isArray:true,
                  timeout: apiConfigSettings.timeout}
            });
        };
    };
})();
