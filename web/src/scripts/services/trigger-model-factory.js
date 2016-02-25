(function () {
  'use strict';

  angular
    .module('webApp')
    .factory('TriggerModelFactory', TriggerModelFactory);

  TriggerModelFactory.$inject = ['UtilsService'];

  function TriggerModelFactory(UtilsService) {
    var trigger = {};
    var error = {text: ""};
    var context = {"position": null};

    function init() {
      trigger.name = "";
      trigger.sql = "";
      trigger.outputs = [];
      trigger.configuration = {};
      error.text = "";
    }

    function resetTrigger() {
      init();
    }

    function getTrigger() {
      if (Object.keys(trigger).length == 0) {
        init()
      }
      return trigger;
    }

    function setTrigger(_trigger) {
      trigger.name = _trigger.name;
      trigger.sql = _trigger.sql;
      trigger.outputs = _trigger.outputs;
      trigger.configuration = _trigger.configuration;
    }

    function isValidTrigger(trigger, triggers, position) {
      var isValid = trigger.name != "" && trigger.sql != "" && trigger.outputs.length > 0 && trigger.condifuration != "";
      if (!isValid) {
        error.text = "_GENERIC_FORM_ERROR_";
      } else {
        if (!nameExists(trigger, triggers, position))
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

    function setPosition(p) {
      if (p === undefined) {
        p = 0;
      }
      context.position = p;
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


