(function () {
  'use strict';

  angular
    .module('webApp')
    .service('ModelStaticDataFactory', ModelStaticDataFactory);

  function ModelStaticDataFactory() {

    return {
      types: [{iconName: "grid-dots", name: "moreline"}, {iconName: "dashboard", name: "datetime"}],
      defaultInput:["raw"]
    }
  }
})
();
