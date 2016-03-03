(function () {
  'use strict';

  angular
    .module('webApp')
    .factory('TriggerModelFactory', TriggerModelFactory);

  TriggerModelFactory.$inject = ['UtilsService', 'PolicyModelFactory'];

  function TriggerModelFactory(UtilsService, PolicyModelFactory) {
    var trigger = {};
    var error = {text: ""};
    var context = {"position": null};

    function init(position) {
      setPosition(position);
      trigger.name = "";
      trigger.sql = "";
      trigger.outputs = [];
      trigger.primaryKey = [];
      trigger.overLastNumber = PolicyModelFactory.getCurrentPolicy().sparkStreamingWindowNumber;
      trigger.overLastTime = PolicyModelFactory.getCurrentPolicy().sparkStreamingWindowTime;
      delete trigger.configuration;
      error.text = "";
    }

    function resetTrigger(position) {
      init(position);
    }

    function getTrigger(position) {
      if (Object.keys(trigger).length == 0) {
        init(position)
      }
      return trigger;
    }

    function setTrigger(_trigger, position) {
      trigger.name = _trigger.name;
      trigger.sql = _trigger.sql;
      trigger.outputs = _trigger.outputs;
      trigger.overLast = _trigger.overLast;
      if (_trigger.configuration) {
        trigger.configuration = _trigger.configuration;
      }
      setPosition(position);
      formatAttributes();
    }

    function setPosition(p) {
      if (p === undefined) {
        p = 0;
      }
      context.position = p;
    }


    function formatAttributes() {
      var overLast = trigger.overLast.split(/([0-9]+)/);
      trigger.overLastNumber = Number(overLast[1]);
      trigger.overLastTime = overLast[2];
      delete trigger.overLast;
    }

    function isValidTrigger(trigger, triggers, position) {
      var isValid = trigger.name != "" && trigger.sql != "" && !nameExists(trigger, triggers, position);
      if (!isValid) {
        error.text = "_GENERIC_FORM_ERROR_";
      } else {
          error.text = "";
      }
      return isValid;
    }

    function nameExists(trigger, triggers, triggerPosition) {
      var position = UtilsService.findElementInJSONArray(triggers, trigger, "name");
      return position !== -1 && (position != triggerPosition);
    }

    function getContext() {
      return context;
    }

    function getError() {
      return error;
    }

    return {
      resetTrigger: resetTrigger,
      getTrigger: getTrigger,
      setTrigger: setTrigger,
      getContext: getContext,
      setPosition: setPosition,
      isValidTrigger: isValidTrigger,
      getError: getError
    }
  }
})
();
