(function () {
  'use strict';

  angular
    .module('webApp')
    .service('PolicyStaticDataService', PolicyStaticDataService);

  function PolicyStaticDataService() {
    var vm = this;
    vm.steps = [{name: "description", icon: "icon-tag_left"}, {name: "input", icon: "icon-import"},
      {name: "model", icon: "icon-content-left"}, {name: "cubes", icon: "icon-box"},
      {name: "outputs", icon: "icon-export"}, {name: "advanced", icon: "icon-star"}];

    vm.sparkStreamingWindow = {min: 0, max: 10000} ;
    vm.checkpointInterval = {min: 0, max: 10000} ;
    vm.checkpointAvailability = {min: 0, max: 10000} ;
  }

})();
