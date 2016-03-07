/*
 * Copyright (C) 2015 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
      trigger.overLastNumber =_trigger.overLastNumber;
      trigger.overLastTime =  _trigger.overLastTime;
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
      if (trigger.overLast) {
        var overLast = trigger.overLast.split(/([0-9]+)/);
        trigger.overLastNumber = Number(overLast[1]);
        trigger.overLastTime = overLast[2];
        delete trigger.overLast;
      }
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
