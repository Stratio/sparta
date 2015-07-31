'use strict';

angular.module('webApp')
  .service('ApiTest', ['$resource', function ApiDownloadModules($resource) {

    var url = '/policy/all';

    var methods = {
      'get'   : {method:'GET', isArray:true}
    };

    var resource =  $resource(url,{},methods);

    return resource;
  }]);