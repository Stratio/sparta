(function () {
  'use strict';

  angular
    .module('webApp')
    .service('PolicyStaticDataFactory', PolicyStaticDataFactory);

  function PolicyStaticDataFactory() {

    return {
      steps: [
        {name: "_POLICY_._STEPS_._DESCRIPTION_", icon: "icon-tag_left"},
        {name: "_POLICY_._STEPS_._INPUT_", icon: "icon-import"},
        {name: "_POLICY_._STEPS_._MODEL_", icon: "icon-content-left"},
        {name: "_POLICY_._STEPS_._CUBES_", icon: "icon-box"},
        {name: "_POLICY_._STEPS_._OUTPUTS_", icon: "icon-export"},
        {name: "_POLICY_._STEPS_._FINISH_", icon: "icon-paper"}
      ],
      sparkStreamingWindow: {min: 0, max: 10000},
      checkpointInterval: {
        min: 0, max: 10000
      }
      ,
      checkpointAvailability: {
        min: 0, max: 10000
      },
      partitionFormat: {
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
      },
      storageLevel: {
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
      },
      helpLinks: {
        description: 'http://docs.stratio.com/modules/sparkta/development/policy.html#general-configuration',
        inputs: 'http://docs.stratio.com/modules/sparkta/development/policy.html#inputs',
        models: 'http://docs.stratio.com/modules/sparkta/development/policy.html#transformations',
        cubes: 'http://docs.stratio.com/modules/sparkta/development/policy.html#cubes',
        outputs: 'http://docs.stratio.com/modules/sparkta/development/policy.html#outputs'
      }
    }
  }
})
();
