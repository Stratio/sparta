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
(function() {
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
      trigger.writer = {autoCalculatedFields: [], outputs: []};
      error.text = "";
      if (type == triggerConstants.TRANSFORMATION) {
        var currentPolicy = PolicyModelFactory.getCurrentPolicy();
        trigger.overLastNumber = currentPolicy.sparkStreamingWindowNumber;
        trigger.overLastTime = currentPolicy.sparkStreamingWindowTime;
        trigger.computeEveryNumber = currentPolicy.sparkStreamingWindowNumber;
        trigger.computeEveryTime = currentPolicy.sparkStreamingWindowTime;
      } else {
        delete trigger.overLast;
        delete trigger.overLastNumber;
        delete trigger.overLastTime;
        delete trigger.computeEvery;
        delete trigger.computeEveryNumber;
        delete trigger.computeEveryTime;
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
      trigger.writer = _trigger.writer;
      if (type == triggerConstants.TRANSFORMATION) {
        trigger.overLast = _trigger.overLast;
        trigger.overLastNumber = _trigger.overLastNumber;
        trigger.overLastTime = _trigger.overLastTime;
        trigger.computeEvery = _trigger.computeEvery;
        trigger.computeEveryNumber = _trigger.computeEveryNumber;
        trigger.computeEveryTime = _trigger.computeEveryTime;
      }
      convertWindowAttributes();
      setPosition(position);
    }

    function setPosition(p) {
      if (p === undefined) {
        p = 0;
      }
      context.position = p;
    }

    function convertWindowAttributes() {
      if (trigger.overLast) {
        var overLast = trigger.overLast.split(/([0-9]+)/);
        trigger.overLastNumber = Number(overLast[1]);
        trigger.overLastTime = overLast[2];
      }
      if (trigger.computeEvery) {
        var computeEvery = trigger.computeEvery.split(/([0-9]+)/);
        trigger.computeEveryNumber = Number(computeEvery[1]);
        trigger.computeEveryTime = computeEvery[2];
      }
      delete trigger.computeEvery;
      delete trigger.overLast;
    }

    function areValidWindowAttributes() {
      var sparkStreamingWindow = PolicyModelFactory.getCurrentPolicy().sparkStreamingWindowNumber;
      return (!sparkStreamingWindow ||
      ((!trigger.overLastNumber || trigger.overLastNumber % sparkStreamingWindow == 0) &&
      (!trigger.computeEveryNumber || trigger.computeEveryNumber % sparkStreamingWindow == 0)));
    }

    function isValidTrigger(triggers, position) {
      var uniqueName = !nameExists(trigger, triggers, position);
      var isValid = trigger.name != "" && trigger.sql != "" && uniqueName;
      var validWindowAttributes = areValidWindowAttributes();
      if (!validWindowAttributes) {
        error.text = "_ERROR_._WINDOW_ATTRIBUTE_NOT_MULTIPLE_ERROR_";
        error.type = 'error';
        error.attribute = 'streamTrigger'
      }
      if (!uniqueName){
        error.text = "_ERROR_._TRIGGER_ALREADY_EXITS_";
        error.type = 'error';
      }
      if (isValid && validWindowAttributes) {
        error.text = "";
      }
      return isValid && validWindowAttributes;
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
