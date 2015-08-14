(function() {
    'use strict';

    angular.module('webApp')
    .service('ApiPolicyService', ['$resource', function($resource){

    	this.GetPolicyByFragmentName = function() {
    		return $resource('/policy/fragment/:type/:name', {type:'@type', name:'@name'},
            {
                'get'   : {method:'GET', isArray:true}
            });
    	};

    }]);
})();