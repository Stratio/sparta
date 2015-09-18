(function () {
  'use strict';

  angular
    .module('webApp')
    .service('PolicyStaticDataFactory', PolicyStaticDataFactory);

  function PolicyStaticDataFactory() {

    return {
      getSteps: function () {
        return [
          {name: "_POLICY_._STEPS_._DESCRIPTION_", icon: "icon-tag_left"},
          {name: "_POLICY_._STEPS_._INPUT_", icon: "icon-import"},
          {name: "_POLICY_._STEPS_._MODEL_", icon: "icon-content-left"},
          {name: "_POLICY_._STEPS_._CUBES_", icon: "icon-box"},
          {name: "_POLICY_._STEPS_._OUTPUTS_", icon: "icon-export"},
          {name: "_POLICY_._STEPS_._FINISH_", icon: "icon-paper"}
        ]
      },
      getSparkStreamingWindow: function () {
        return {min: 0, max: 10000}
      },
      getCheckpointInterval: function () {
        return {
          min: 0, max: 10000
        }
      },
      getCheckpointAvailability: function () {
        return {
          min: 0, max: 10000
        }
      },
      getPartitionFormat: function () {
        return {
          "values": [
            {
              "label": "year",
              "value": "year"
            },
            {
              "label": "month",
              "value": "month"
            },
            {
              "label": "day",
              "value": "day"
            },
            {
              "label": "hour",
              "value": "hour"
            },
            {
              "label": "minute",
              "value": "minute"
            }
          ]
        }
      },
      getStorageLevel: function () {
        return {
          "values": [
            {
              "label": "DISK_ONLY",
              "value": "DISK_ONLY"
            },
            {
              "label": "DISK_ONLY_2",
              "value": "DISK_ONLY_2"
            },
            {
              "label": "MEMORY_ONLY",
              "value": "MEMORY_ONLY"
            },
            {
              "label": "MEMORY_ONLY_2",
              "value": "MEMORY_ONLY_2"
            },
            {
              "label": "MEMORY_ONLY_SER",
              "value": "MEMORY_ONLY_SER"
            },
            {
              "label": "MEMORY_ONLY_SER_2",
              "value": "MEMORY_ONLY_SER_2"
            },
            {
              "label": "MEMORY_AND_DISK",
              "value": "MEMORY_AND_DISK"
            },
            {
              "label": "MEMORY_AND_DISK_2",
              "value": "MEMORY_AND_DISK_2"
            },
            {
              "label": "MEMORY_AND_DISK_SER",
              "value": "MEMORY_AND_DISK_SER"
            },
            {
              "label": "MEMORY_AND_DISK_SER_2",
              "value": "MEMORY_AND_DISK_SER_2"
            }
          ]
        }
      },
      getHelpLinks: function () {
        return {
          description: 'http://docs.stratio.com/modules/sparkta/development/policy.html#general-configuration',
          inputs: 'http://docs.stratio.com/modules/sparkta/development/policy.html#inputs',
          models: 'http://docs.stratio.com/modules/sparkta/development/policy.html#transformations',
          cubes: 'http://docs.stratio.com/modules/sparkta/development/policy.html#cubes',
          outputs: 'http://docs.stratio.com/modules/sparkta/development/policy.html#outputs'
        }
      },
      getConfigPlaceholder: function () {
        return '{    \n    "inputFormat": "unixMillis", \n   "dbName": "sparkta"\n}'
      },
      getConfigurationHelpLink: function () {
        return "http://docs.stratio.com/modules/sparkta/development/transformations.html";
      }
    }
  }
})
();
