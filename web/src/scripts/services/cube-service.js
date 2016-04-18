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
    .service('CubeService', CubeService);

  CubeService.$inject = ['WizardStatusService','PolicyModelFactory', 'ModalService', 'CubeModelFactory', 'UtilsService', '$q'];

  function CubeService(WizardStatusService,PolicyModelFactory, ModalService, CubeModelFactory,  UtilsService, $q) {
    var vm = this;
    var createdCubes = null;
    var cubeCreationStatus = {};

    vm.findCubesUsingOutputs = findCubesUsingOutputs;
    vm.showConfirmRemoveCube = showConfirmRemoveCube;
    vm.addCube = addCube;
    vm.saveCube = saveCube;
    vm.removeCube = removeCube;
    vm.isNewCube = isNewCube;
    vm.getCubeCreationStatus = getCubeCreationStatus;
    vm.getCreatedCubes = getCreatedCubes;
    vm.resetCreatedCubes = resetCreatedCubes;
    vm.changeCubeCreationPanelVisibility = changeCubeCreationPanelVisibility;
    vm.isActiveCubeCreationPanel = isActiveCubeCreationPanel;
    vm.activateCubeCreationPanel = activateCubeCreationPanel;
    vm.disableCubeCreationPanel = disableCubeCreationPanel;

    init();

    function init() {
      vm.policy = PolicyModelFactory.getCurrentPolicy();
      createdCubes = vm.policy.cubes.length;
      cubeCreationStatus.enabled = false;
    }

    function activateCubeCreationPanel() {
      cubeCreationStatus.enabled  = true;
    }

    function disableCubeCreationPanel() {
      cubeCreationStatus.enabled  = false;
    }

    function getCubeCreationStatus(){
      return cubeCreationStatus;
    }

    function showConfirmRemoveCube() {
      var defer = $q.defer();
      var controller = "ConfirmModalCtrl";
      var templateUrl = "templates/modal/confirm-modal.tpl.html";
      var title = "_REMOVE_CUBE_CONFIRM_TITLE_";
      var message = "";
      var resolve = {
        title: function () {
          return title
        }, message: function () {
          return message
        }
      };
      var modalInstance = ModalService.openModal(controller, templateUrl, resolve, "", "lg");

      modalInstance.result.then(function () {
        defer.resolve();
      }, function () {
        defer.reject();
      });
      return defer.promise;
    }

    function findCubesUsingOutputs(outputs) {
      var cubeNames = [];
      var cubePositions = [];
      var cubes = vm.policy.cubes;
      if (cubes && outputs) {
        var currentCube = null;
        var found = false;
        for (var i = 0; i < cubes.length; ++i) {
          found = false;
          currentCube = cubes[i];
          found = findDimensionUsingOutputs(currentCube, outputs);
          if (found) {
            cubeNames.push(currentCube.name);
            cubePositions.push(i);
          }
        }
      }
      return {names: cubeNames, positions: cubePositions};
    }

    function findDimensionUsingOutputs(cube, outputs) {
      var found = false;
      var currentDimension = null;
      var i = 0;
      while (!found && i < cube.dimensions.length) {
        currentDimension = cube.dimensions[i];
        if (outputs.indexOf(currentDimension.field) != -1) {
          found = true;
        } else {
          ++i;
        }
      }
      return found;
    }

    function addCube(cubeForm) {
      var newCube = angular.copy(CubeModelFactory.getCube());
      if (CubeModelFactory.isValidCube(newCube, vm.policy.cubes, CubeModelFactory.getContext().position, PolicyModelFactory.getAllModelOutputs())) {

        vm.policy.cubes.push(newCube);
        createdCubes++;
        WizardStatusService.enableNextStep();
        changeCubeCreationPanelVisibility(false);
        cubeForm.$submitted = false;
      } else {
        CubeModelFactory.setError();
      }
    }

    function saveCube(cubeForm) {
      cubeForm.$submitted = true;
      var cube = angular.copy(CubeModelFactory.getCube());
      if (CubeModelFactory.isValidCube(cube, vm.policy.cubes, CubeModelFactory.getContext().position, PolicyModelFactory.getAllModelOutputs())) {
        cubeForm.$submitted = false;
        vm.policy.cubes[CubeModelFactory.getContext().position] = cube;
        changeCubeCreationPanelVisibility(false);
        cubeForm.$submitted = false;
      } else {
        CubeModelFactory.setError();
      }
    }

    function removeCube() {
      var defer = $q.defer();
      var cubePosition = CubeModelFactory.getContext().position;
      showConfirmRemoveCube().then(function () {
        vm.policy.cubes.splice(cubePosition, 1);
        resetCreatedCubes();
        defer.resolve();
      }, function () {
        defer.reject()
      });
      return defer.promise;
    }

    function isNewCube(index) {
      return index == vm.policy.cubes.length;
    }

    function getCreatedCubes() {
      return createdCubes;
    }

    function changeCubeCreationPanelVisibility(isVisible) {
      cubeCreationStatus.enabled  = isVisible;
    }

    function isActiveCubeCreationPanel() {
      return cubeCreationStatus.enabled;
    }

    function resetCreatedCubes() {
      createdCubes = vm.policy.cubes.length;
    }
  }
})();
