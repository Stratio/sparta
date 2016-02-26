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

    function init(position) {
      setPosition(position);
      trigger.name = "";
      trigger.sql = "";
      trigger.outputs = [];
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
      if (_trigger.configuration) {
        trigger.configuration = _trigger.configuration;
      }
      setPosition(position);
    }

    function setPosition(p) {
      if (p === undefined) {
        p = 0;
      }
      context.position = p;
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
