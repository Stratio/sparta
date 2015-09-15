(function () {
  'use strict';

  angular
    .module('webApp')
    .service('CubeStaticDataFactory', CubeStaticDataFactory);

  function CubeStaticDataFactory() {
    var granularityOptions = [
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

    var precisionOptions = [
      {
        type: "DateTime",
        precisions: [
          {
            "label": "_SECOND_",
            "value": "second"
          },
          {
            "label": "_5_SECONDS_",
            "value": "s5"
          },
          {
            "label": "_10_SECONDS_",
            "value": "s10"
          },
          {
            "label": "_15_SECONDS_",
            "value": "s15"
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
        ]
      },
      {
        type: "GeoHash",
        precisions: [
          {
            "label": "_PRECISION_1_",
            "value": "precision1"

          },
          {
            "label": "_PRECISION_2_",
            "value": "precision2"

          },
          {
            "label": "_PRECISION_3_",
            "value": "precision3"

          },
          {
            "label": "_PRECISION_4_",
            "value": "precision4"

          }, {
            "label": "_PRECISION_5_",
            "value": "precision5"

          }, {
            "label": "_PRECISION_6_",
            "value": "precision6"

          }, {
            "label": "_PRECISION_7_",
            "value": "precision7"

          }, {
            "label": "_PRECISION_8_",
            "value": "precision8"

          }, {
            "label": "_PRECISION_9_",
            "value": "precision9"

          }, {
            "label": "_PRECISION_10_",
            "value": "precision10"

          }, {
            "label": "_PRECISION_11_",
            "value": "precision11"

          }, {
            "label": "_PRECISION_12_",
            "value": "precision12"
          }
        ]
      },
      {
        type: "Default",
        precisions: []
      }
    ];

    var cubeTypes = [
      {
        "label": "Default",
        "value": "Default"
      },
      {
        "label": "DateTime",
        "value": "DateTime"
      },
      {
        "label": "GeoHash",
        "value": "GeoHash"
      }];
    var defaultInterval = 30000;
    var defaultTimeAvailability = 60000;
    var defaultGranularity = "minute";

    return {
      getFunctionNames: function () {
        return ["Accumulator", "Avg", "Count", "EntityCount", "FirstValue", "FullText", "LastValue", "Max", "Median", "Min", "Mode", "Range", "Stddev", "Sum", "Variance", "TotalEntityCount"]
      },
      getGranularityOptions: function () {
        return granularityOptions;
      },
      getDefaultType: function () {
        return cubeTypes[0];
      },
      getPrecisionOptions: function () {
        return precisionOptions
      },
      getCubeTypes: function () {
        return cubeTypes;
      },
      getDefaultInterval: function () {
        return defaultInterval;
      },
      getDefaultTimeAvailability: function () {
        return defaultTimeAvailability;
      },
      getDefaultGranularity: function(){
        return defaultGranularity;
      }
    }
  }
})
();
