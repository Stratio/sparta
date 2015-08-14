(function() {
    'use strict';

    angular
        .module('webApp')
        .service('ApiFragmentDataService', ApiFragmentDataService);

    ApiFragmentDataService.$inject = ['$resource'];

    function ApiFragmentDataService($resource) {
        return $resource(
/*
             API_URL+'dataview/:id',
            { id: '@id' },
*/
            {

/*
                forcedelete: { method: 'DELETE', url: API_URL+'dataview/:id/force' },
                query: { method: 'GET', url: API_URL+'dataviews', isArray: false },
                save: { method: 'POST', url: API_URL+'dataviews' },
                update: { method:'PUT' },
                widgets: { method: 'GET', url: API_URL+'dataview/:id/widgets' }
*/
            }
        );
    }
})();