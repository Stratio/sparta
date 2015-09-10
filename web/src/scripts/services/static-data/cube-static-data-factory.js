(function () {
  'use strict';

  angular
    .module('webApp')
    .service('CubeStaticDataFactory', CubeStaticDataFactory);

  function CubeStaticDataFactory() {

    return {
      GetFunctions: function () {
        return ["Accumulator", "Avg", "Count", "EntityCount", "FirstValue", "FullText", "LastValue", "Max", "Median", "Min", "Mode", "Range", "Stddev", "Sum", "Variance", "TotalEntityCount"]
      },
      GetGranularityOptions: function () {
        return ["seconds", "5 seconds", "10 seconds", "15 seconds", "minute", "hour", "day", "month", "year"]
      },
      GetConfigurationHelpLink: function(){
        return "http://docs.stratio.com/modules/sparkta/development/transformations.html"
      }
    }
  }
})
();
