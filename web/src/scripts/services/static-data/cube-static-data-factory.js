(function () {
  'use strict';

  angular
    .module('webApp')
    .service('CubeStaticDataFactory', CubeStaticDataFactory);

  function CubeStaticDataFactory() {

    return {
      GetFunctions: function () {
        return ["Accumulator", "Avg", "Count", "Entity count", "First value", "Full text", "Last value", "Max", "Median", "Min", "Mode", "Range", "Std_Dev", "Sum", "Variance", "Total entity count"]
      }
    }
  }
})
();
