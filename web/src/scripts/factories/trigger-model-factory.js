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

  TriggerModelFactory.$inject = ['UtilsService', 'PolicyModelFactory', 'triggerConstants'];

  function TriggerModelFactory(UtilsService, PolicyModelFactory, triggerConstants) {
    var trigger = {};
    var error = {text: ""};
    var context = {"position": null};


    function init(position, type) {
      setPosition(position);
      trigger.name = "";
      trigger.sql = "";
      trigger.outputs = [];
      trigger.primaryKey = [];
      error.text = "";
      if (type == triggerConstants.TRANSFORMATION) {
        trigger.overLastNumber = PolicyModelFactory.getCurrentPolicy().sparkStreamingWindowNumber;
        trigger.overLastTime = PolicyModelFactory.getCurrentPolicy().sparkStreamingWindowTime;
      } else {
        delete trigger.overLast;
        delete trigger.overLastNumber;
        delete trigger.overLastTime;
      }
    }

    function resetTrigger(position, type) {
      init(position, type);
    }

    function getTrigger(position, type) {
      if (Object.keys(trigger).length == 0) {
        init(position, type)
      }
      return trigger;
    }

    function setTrigger(_trigger, position, type) {
      trigger.name = _trigger.name;
      trigger.sql = _trigger.sql;
      trigger.outputs = _trigger.outputs;
      if (type == triggerConstants.TRANSFORMATION) {
        trigger.overLast = _trigger.overLast;
        trigger.overLastNumber = _trigger.overLastNumber;
        trigger.overLastTime = _trigger.overLastTime;
      }
      convertOverLast();
      setPosition(position);
    }

    function setPosition(p) {
      if (p === undefined) {
        p = 0;
      }
      context.position = p;
    }

    function convertOverLast() {
      if (trigger.overLast) {
        var overLast = trigger.overLast.split(/([0-9]+)/);
        trigger.overLastNumber = Number(overLast[1]);
        trigger.overLastTime = overLast[2];
      }
      delete trigger.overLast;
    }

    function isValidOverLast() {
      var sparkStreamingWindow = PolicyModelFactory.getCurrentPolicy().sparkStreamingWindowNumber;
      return (!sparkStreamingWindow || !trigger.overLastNumber || trigger.overLastNumber % PolicyModelFactory.getCurrentPolicy().sparkStreamingWindowNumber == 0);
    }

    function isValidTrigger(triggers, position) {
      var isValid = trigger.name != "" && trigger.sql != "" && !nameExists(trigger, triggers, position);
      var validOverLast = isValidOverLast();
      if (!validOverLast) {
        error.text = "_ERROR_._OVERLAST_NOT_MULTIPLE_ERROR_";
        error.type = 'error';
        error.attribute = 'overLastNumber'
      }
      if (isValid && validOverLast){
        error.text = "";
      }
      return isValid && validOverLast;
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

    function setError() {
      error.text = '';
    }

    return {
      resetTrigger: resetTrigger,
      getTrigger: getTrigger,
      setTrigger: setTrigger,
      getContext: getContext,
      setPosition: setPosition,
      isValidTrigger: isValidTrigger,
      getError: getError,
      setError: setError
    }
  }
})
();
