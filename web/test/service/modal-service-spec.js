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
describe('modal-service', function () {
  beforeEach(module('webApp'));
  var service, $uibModalMock, utilsServiceMock, resolvedModal = null;
  beforeEach(module(function ($provide) {
    $uibModalMock = jasmine.createSpyObj('$uibModal', ['open']);


    utilsServiceMock = jasmine.createSpyObj('UtilsService', ['getInCamelCase']);
    // inject mocks
    $provide.value('$uibModal', $uibModalMock);
    $provide.value('UtilsService', utilsServiceMock);
  }));

  beforeEach(inject(function (ModalService, $q) {
    resolvedModal = function () {
      var defer = $q.defer();
      defer.resolve();

      return {"result": defer.promise};
    };
    $uibModalMock.open.and.callFake(resolvedModal);
    service = ModalService;
  }));

  describe("should be able to open a modal by a introduced template", function () {
    var fakeTemplate = {
      modalType: "fake modal type", properties: function () {
        []
      }
    };

    beforeEach(function () {
      spyOn(service, "openModal");
      $uibModalMock.open.calls.reset();
    });

    it("if template is null or undefined, it does not open any modal", function () {
      service.openModalByTemplate();

      expect(service.openModal).not.toHaveBeenCalled();
    });

    describe("if template is valid", function () {
      it("modal is open with data of template", function () {
        utilsServiceMock.getInCamelCase.and.callFake(function (string) {
          return string
        });
        var fakeMode = "edition";
        var fakeItemConfiguration = {"fakeAttribute": "fake value"};
        service.openModalByTemplate(fakeTemplate, fakeMode, fakeItemConfiguration);

        var controller = fakeTemplate.modalType + "ModalCtrl as vm";
        var templateUrl = "templates/modal/" + fakeTemplate.modalType + "-modal.tpl.html";
        var openModalArgs = $uibModalMock.open.calls.mostRecent().args[0];

        expect(openModalArgs.templateUrl).toEqual(templateUrl);
        expect(openModalArgs.controller).toEqual(controller);
        expect(openModalArgs.resolve.propertiesTemplate()).toEqual(fakeTemplate.properties);
        expect(openModalArgs.resolve.mode()).toEqual(fakeMode);
        expect(openModalArgs.resolve.configuration()).toEqual(fakeItemConfiguration);
      });
    })
  });


  it("should be able to open a confirmation modal rendering data introduced", function () {
    var fakeTitle = "fake title";
    var question = "fake question";
    var message = "fake message";
    $uibModalMock.open.calls.reset();
    service.showConfirmDialog(fakeTitle, question, message);

    var controller = "ConfirmModalCtrl as vm";
    var templateUrl = "templates/modal/confirm-modal.tpl.html";
    var openModalArgs = $uibModalMock.open.calls.mostRecent().args[0];

    expect(openModalArgs.templateUrl).toEqual(templateUrl);
    expect(openModalArgs.controller).toEqual(controller);
    expect(openModalArgs.resolve.title()).toEqual(fakeTitle);
    expect(openModalArgs.resolve.question()).toEqual(question);
    expect(openModalArgs.resolve.message()).toEqual(message);
  });

});
