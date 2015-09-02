(function() {
    'use strict';

    angular
        .module('webApp')
        .service('ApiTemplateService', ApiTemplateService);

    ApiTemplateService.$inject = ['$resource'];

    function ApiTemplateService($resource) {
        var vm = this;

        vm.GetFragmentTemplateByType = GetFragmentTemplateByType;

        /////////////////////////////////

        function GetFragmentTemplateByType() {
            return $resource('/data-templates/:type', {type:'@type'},
            {
                'get'   : {method:'GET', isArray:true}
            });
        };
    };
})();