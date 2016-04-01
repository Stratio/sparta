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
    .factory('CubeModelFactory', CubeModelFactory);

  CubeModelFactory.$inject = ['UtilsService', 'PolicyModelFactory'];

  function CubeModelFactory(UtilsService, PolicyModelFactory) {
    var cube = {};
    var error = {text: ""};
    var context = {"position": null};

    function init(template, nameIndex, position) {
      setPosition(position);
      error.text = "";
      cube.name = template.defaultName + (nameIndex + 1);
      cube.dimensions = [];
      cube.operators = [];
      cube.checkpointConfig = {};
      cube.triggers = [];
      cube.writer = {};

      delete cube['writer.fixedMeasure'];
      delete cube['writer.isAutoCalculatedId'];
      delete cube['writer.dateType'];
      cube['writer.outputs'] = [];
    }

    function resetCube(template, nameIndex, position) {
      init(template, nameIndex, position);
    }

    function getCube(template, nameIndex, position) {
      if (Object.keys(cube).length == 0) {
        init(template, nameIndex, position)
      }
      return cube;
    }

    function setCube(c, position) {
      cube.name = c.name;
      cube.dimensions = c.dimensions;
      cube.operators = c.operators;
      cube.checkpointConfig = c.checkpointConfig;
      error.text = "";

      formatAttributes(c);
      setPosition(position);
      setTriggers(c.triggers);
    }

    function formatAttributes(c) {
      cube['writer.fixedMeasure'] = c['writer.fixedMeasure'] || c.writer.fixedMeasure;
      cube['writer.isAutoCalculatedId'] = c['writer.isAutoCalculatedId'] || c.writer.isAutoCalculatedId;
      cube['writer.dateType'] = c['writer.dateType'] || c.writer.dateType;
      cube['writer.outputs'] = c['writer.outputs'] || c.writer.outputs;
    }

    function setTriggers(triggers) {
      if (!cube.triggers) {
        cube.triggers = [];
      }
      while (cube.triggers.length > 0) {
        cube.triggers.pop();
      }
      for (var i = 0; i < triggers.length; ++i) {
        cube.triggers.push(triggers[i]);
      }
    }

    function areValidOperatorsAndDimensions(cube) {
      var validOperatorsAndDimensionsLength = cube.operators.length > 0 && cube.dimensions.length > 0;
      var validFieldList = PolicyModelFactory.getAllModelOutputs();
      for (var i = 0; i < cube.dimensions.length; ++i) {
        if (validFieldList.indexOf(cube.dimensions[i].field) == -1)
          return false;
      }
      return validOperatorsAndDimensionsLength;
    }

    function isValidCube(cube, cubes, position) {
      var validName = cube.name !== undefined && cube.name !== "";
      var validRequiredAttributes = cube.operators.length > 0 && cube.dimensions.length > 0 && (cube.triggers.length > 0 || cube['writer.outputs'].length > 0);
      var isValid = validName && validRequiredAttributes &&  areValidOperatorsAndDimensions(cube) && !nameExists(cube, cubes, position);
      return isValid;
    }

    function nameExists(cube, cubes, cubePosition) {
      var position = UtilsService.findElementInJSONArray(cubes, cube, "name");
      return position !== -1 && (position != cubePosition);
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

    function setError() {
      error.text = "";
      if (cube.operators.length === 0 && cube.dimensions.length === 0) {
        error.text = "_POLICY_CUBE_OPERATOR-DIMENSION_ERROR_";
      }
      else if (cube.operators.length === 0) {
        error.text = "_POLICY_CUBE_OPERATOR_ERROR_";
      }
      else {
        error.text = "_POLICY_CUBE_DIMENSION_ERROR_";
      }
    }

    return {
      resetCube: resetCube,
      getCube: getCube,
      setCube: setCube,
      getContext: getContext,
      setPosition: setPosition,
      isValidCube: isValidCube,
      setError: setError,
      getError: getError
    }
  }

})
();
