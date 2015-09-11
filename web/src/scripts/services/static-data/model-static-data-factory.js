(function () {
  'use strict';

  angular
    .module('webApp')
    .service('ModelStaticDataFactory', ModelStaticDataFactory);

  function ModelStaticDataFactory() {

    return {
      types: [{iconName: "morphline", name: "morphline"}, {iconName: "datetime", name: "datetime"}, {iconName: "type", name: "type"}],
      /* Altough defaultInput will be 1 element always, it must be an array */
      defaultInput:["raw"],
      configHelpLink: "http://docs.stratio.com/modules/sparkta/development/transformations.html"
    }
  }
})
();
