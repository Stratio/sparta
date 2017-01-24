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
describe('policy-creation-modal-controller', function () {
  beforeEach(module('webApp'));
  beforeEach(module('model/policy.json'));
  beforeEach(module('template/policy.json'));

  var ctrl, $q, titleMock, PolicyFactoryMock, PolicyModelFactoryMock, TemplateFactoryMock, fakePolicy, fakePolicyTemplate, modalInstanceMock, scope, fakeTitle = null;

  beforeEach(inject(function ($controller, _$q_, $httpBackend, $rootScope, _modelPolicy_, _templatePolicy_) {
    $httpBackend.when('GET', 'languages/en-US.json')
      .respond({});
    scope = $rootScope.$new();
    $q = _$q_;
    fakePolicy =  angular.copy(_modelPolicy_);
    fakePolicyTemplate = _templatePolicy_;
    PolicyFactoryMock = jasmine.createSpyObj('PolicyFactory', ['existsPolicy']);
    PolicyModelFactoryMock = jasmine.createSpyObj('PolicyModelFactory', ['setTemplate', 'resetPolicy', 'getCurrentPolicy', 'nextStep']);
    TemplateFactoryMock = jasmine.createSpyObj('TemplateFactory', ['getPolicyTemplate']);
    modalInstanceMock = jasmine.createSpyObj('$uibModalInstance', ['close']);
    TemplateFactoryMock.getPolicyTemplate.and.callFake(function () {
      var defer = $q.defer();
      defer.resolve(fakePolicyTemplate);
      return defer.promise;
    });
    PolicyModelFactoryMock.getCurrentPolicy.and.callFake(function () {
      return fakePolicy;
    });
    spyOn(document, "querySelector").and.callFake(function () {
      return {"focus": jasmine.createSpy()}
    });
    fakeTitle = 'Fake title';
    titleMock = jasmine.createSpy().and.returnValue(fakeTitle);

    ctrl = $controller('PolicyCreationModalCtrl', {
      'PolicyModelFactory': PolicyModelFactoryMock,
      'PolicyFactory': PolicyFactoryMock,
      'TemplateFactory': TemplateFactoryMock,
      '$uibModalInstance': modalInstanceMock,
      'title': titleMock
    });

    scope.$apply();
  }));

  describe("when it is initialized", function () {

    it('should get a policy template from from policy factory', function () {
      expect(ctrl.template).toBe(fakePolicyTemplate);
    });

    it('should get the policy that is being created or edited from policy factory', function () {
      expect(ctrl.policy).toBe(fakePolicy);
    });

    it('it should get the policy template from from a template factory and put it to the policy model factory', function () {
      expect(TemplateFactoryMock.getPolicyTemplate).toHaveBeenCalled();
      expect(PolicyModelFactoryMock.setTemplate).toHaveBeenCalledWith(fakePolicyTemplate);
    });

  });

  describe("should validate form and policy before to close modal", function () {
    var rootScope, httpBackend;
    beforeEach(inject(function ($rootScope, $httpBackend) {
      rootScope = $rootScope;
      httpBackend = $httpBackend;
    }));

    afterEach(function () {
      httpBackend.flush();
      rootScope.$digest();
    });

    describe("if view validations have been passed", function () {
      beforeEach(function () {
        ctrl.form = {$valid: true}; //view validations have been passed
        ctrl.policy.rawDataEnabled = false;
      });

      it("but there is another policy with the same name and different id, modal is not closed", function () {
        PolicyFactoryMock.existsPolicy.and.callFake(function () {
          var defer = $q.defer();
          defer.resolve(true);
          return defer.promise;
        });

        var policy = angular.copy(fakePolicy);
        policy.id = "new id";
        ctrl.policy = policy;
        ctrl.validateForm();
        scope.$digest();

        expect(modalInstanceMock.close).not.toHaveBeenCalled();
      });

      describe("and there is not another policy with the same name", function () {
        beforeEach(function () {
          PolicyFactoryMock.existsPolicy.and.callFake(function () {
            var defer = $q.defer();
            defer.resolve(false);
            return defer.promise;
          });
          var policy = angular.copy(fakePolicy);
          ctrl.policy = policy;
        });

        it("modal is closed", function () {
          ctrl.validateForm();
          scope.$digest();

          expect(modalInstanceMock.close).toHaveBeenCalled();
        });

      });
    });
    describe("if view validations have not been passed", function () {
      beforeEach(function () {
        ctrl.form = {$valid: false}; //view validations have been passed
      });
      it("modal is not closed", function () {
        ctrl.policy = fakePolicy;
        ctrl.validateForm();
        scope.$digest();

        expect(modalInstanceMock.close).not.toHaveBeenCalled();
      })
    });
  });
});
