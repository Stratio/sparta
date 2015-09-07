(function () {
  'use strict';

  angular
    .module('webApp')
    .service('ModelStaticDataFactory', ModelStaticDataFactory);

  function ModelStaticDataFactory() {

    return {
      types: [{iconName: "moreline", name: "moreline"}, {iconName: "datetime", name: "datetime"}],
      defaultInput:["raw"]
    }
  }
})
();
