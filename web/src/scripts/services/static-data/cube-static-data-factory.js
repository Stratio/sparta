(function () {
  'use strict';

  angular
    .module('webApp')
    .service('CubeStaticDataFactory', CubeStaticDataFactory);

  function CubeStaticDataFactory() {
    var granularityOptions =  [
        {
          "label": "5 seconds",
          "value": "5 seconds"
        },
        {
          "label": "_10_SECONDS_",
          "value": "10 seconds"
        },
        {
          "label": "_15_SECONDS_",
          "value": "15 seconds"
        },
        {
          "label": "_MINUTE_",
          "value": "minute"
        },
        {
          "label": "_HOUR_",
          "value": "hour"
        },
        {
          "label": "_DAY_",
          "value": "day"
        },
        {
          "label": "_MONTH_",
          "value": "month"
        },
        {
          "label": "_YEAR_",
          "value": "year"
        }
      ];

    return {
      GetFunctions: function () {
        return ["Accumulator", "Avg", "Count", "EntityCount", "FirstValue", "FullText", "LastValue", "Max", "Median", "Min", "Mode", "Range", "Stddev", "Sum", "Variance", "TotalEntityCount"]
      },
      GetGranularityOptions: function () {
        return granularityOptions;
      },

      GetDefaultType: function(){
        return "Default"
      }
    }
  }
})
();
