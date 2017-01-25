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
    .service('ModalService', ModalService);

  ModalService.$inject = ['$uibModal', 'UtilsService'];

  function ModalService($uibModal, UtilsService) {
    var vm = this;
    vm.openModal = openModal;
    vm.openModalByTemplate = openModalByTemplate;
    vm.showConfirmDialog = showConfirmDialog;

    function openModal(controller, templateUrl, resolve, extraClass, size) {
      var modalInstance = $uibModal.open({
        animation: true,
        templateUrl: templateUrl,
        controller: controller + ' as vm',
        size: size,
        resolve: resolve,
        windowClass: extraClass,
        backdrop: 'static',
        keyboard: false
      });
      return modalInstance;
    }

    function openModalByTemplate(template, mode, itemConfiguration) {
      if (template && template.modalType) {
        var modalType = template.modalType;
        var controller = UtilsService.getInCamelCase(modalType, "-", true) + "ModalCtrl";
        var templateUrl = "templates/modal/" + modalType + "-modal.tpl.html";
        var resolve = {
          propertiesTemplate: function () {
            return template.properties
          },
          mode: function () {
            return mode
          },
          configuration: function () {
            if (!itemConfiguration)
              itemConfiguration = {};
            return itemConfiguration;
          }
        };
        var modalInstance = openModal(controller, templateUrl, resolve, 'x-lg');

        return modalInstance;
      }
    }

    function showConfirmDialog(title, question, message) {
      var templateUrl = "templates/modal/confirm-modal.tpl.html";
      var controller = "ConfirmModalCtrl";
      var resolve = {
        title: function () {
          return title
        },
        question: function () {
          return question
        },
        message: function () {
          return message;
        }
      };
      var modalInstance = openModal(controller, templateUrl, resolve);
      return modalInstance.result;
    }
  }

})();
