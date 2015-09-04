(function () {
  'use strict';

  angular
    .module('webApp')
    .service('PolicyStaticDataFactory', PolicyStaticDataFactory);

  function PolicyStaticDataFactory() {

    return {
      steps: [{name: "_POLICY_._STEPS_._DESCRIPTION_", icon: "icon-tag_left"}, {
        name: "_POLICY_._STEPS_._INPUT_",
        icon: "icon-import"
      },
        {name: "_POLICY_._STEPS_._MODEL_", icon: "icon-content-left"}, {
          name: "_POLICY_._STEPS_._CUBES_",
          icon: "icon-box"
        },
        {name: "_POLICY_._STEPS_._OUTPUTS_", icon: "icon-export"}, {
          name: "_POLICY_._STEPS_._ADVANCED_",
          icon: "icon-star"
        }, {name: "_POLICY_._STEPS_._FINISH_", icon: "icon-paper"}],
      sparkStreamingWindow: {min: 0, max: 10000},
      checkpointInterval: {
        min: 0, max: 10000
      }
      ,
      checkpointAvailability: {
        min: 0, max: 10000
      }
    }
  }
})
();
