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
    var service, $modalMock, utilsServiceMock = null;
    beforeEach(module(function ($provide) {
        $modalMock = jasmine.createSpyObj('$modal', ['open']);
        utilsServiceMock = jasmine.createSpyObj('UtilsService', ['getInCamelCase']);
        // inject mocks
        $provide.value('$modal', $modalMock);
        $provide.value('UtilsService', utilsServiceMock);
    }));

    beforeEach(inject(function (ModalService) {
        service = ModalService;
    }));

    describe("should be able to open a modal by a introduced template", function () {
        var fakeTemplate = {modalType: "fake modal type", properties:function(){[]}};

        beforeEach(function () {
            spyOn(service, "openModal");
            $modalMock.open.calls.reset();
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

                service.openModalByTemplate(fakeTemplate);

                var controller = fakeTemplate.modalType + "ModalCtrl as vm";
                var templateUrl = "templates/modal/" + fakeTemplate.modalType + "-modal.tpl.html";
                var openModalArgs = $modalMock.open.calls.mostRecent().args[0];

                expect(openModalArgs.templateUrl).toEqual(templateUrl);
                expect(openModalArgs.controller).toEqual(controller);
                expect(openModalArgs.resolve.propertiesTemplate()).toEqual(fakeTemplate.properties);
            });
        })
    });

});
