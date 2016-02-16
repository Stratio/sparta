describe('policies.wizard.controller.new-policy-controller', function () {
  beforeEach(module('webApp'));
  beforeEach(module('served/policy.json'));
  beforeEach(module('served/policyTemplate.json'));

  var ctrl, scope, fakePolicy, fakeTemplate, fakeError, policyModelFactoryMock, templateFactoryMock, policyFactoryMock,
    stateMock, fakeCreationStatus, modalServiceMock, resolvedPromise, rejectedPromise, fakeFinalPolicyJSON;

  // init mock modules

  beforeEach(inject(function ($controller, $q, $httpBackend, $rootScope, _servedPolicy_, _servedPolicyTemplate_) {
    scope = $rootScope.$new();

    fakePolicy = angular.copy(_servedPolicy_);
    fakeTemplate = _servedPolicyTemplate_;

    resolvedPromise = function () {
      var defer = $q.defer();
      defer.resolve();

      return defer.promise;
    };

    fakeError = {"data": {"i18nCode": "fake error message"}};
    rejectedPromise = function () {
      var defer = $q.defer();
      defer.reject(fakeError);

      return defer.promise;
    };

    $httpBackend.when('GET', 'languages/en-US.json')
      .respond({});
    fakeCreationStatus = {"currentStep": 0};
    templateFactoryMock = jasmine.createSpyObj('TemplateFactory', ['getPolicyTemplate']);
    templateFactoryMock.getPolicyTemplate.and.callFake(function () {
      var defer = $q.defer();
      defer.resolve(fakeTemplate);
      return defer.promise;
    });
    policyFactoryMock = jasmine.createSpyObj('PolicyFactory', ['createPolicy']);
    policyFactoryMock.createPolicy.and.callFake(resolvedPromise);

    stateMock = jasmine.createSpyObj('$state', ['go']);

    policyModelFactoryMock = jasmine.createSpyObj('PolicyModelFactory', ['getCurrentPolicy', 'getFinalJSON', 'setTemplate', 'getTemplate', 'getProcessStatus', 'resetPolicy']);
    policyModelFactoryMock.getCurrentPolicy.and.callFake(function () {
      return fakePolicy;
    });

    policyModelFactoryMock.getTemplate.and.callFake(function () {
      return fakeTemplate;
    });

    fakeFinalPolicyJSON = {"fake_attribute": "fake value"};
    policyModelFactoryMock.getFinalJSON.and.returnValue(fakeFinalPolicyJSON);

    policyModelFactoryMock.getProcessStatus.and.returnValue(fakeCreationStatus);

    modalServiceMock = jasmine.createSpyObj('ModalService', ['openModal']);

    modalServiceMock.openModal.and.callFake(function () {
      var defer = $q.defer();
      defer.resolve();
      return {"result": defer.promise};
    });

    ctrl = $controller('NewPolicyCtrl', {
      'PolicyModelFactory': policyModelFactoryMock,
      'TemplateFactory': templateFactoryMock,
      'PolicyFactory': policyFactoryMock,
      'ModalService': modalServiceMock,
      '$state': stateMock
    });


    scope.$digest();
  }));

  describe("when it is initialized", function () {

    it('it should get the policy from policy factory that will be created', function () {
      expect(ctrl.policy).toBe(fakePolicy);
    });

    it("should load the steps of the policy creation/edition from the policy template", function () {
      expect(ctrl.steps).toBe(fakeTemplate.steps);
    });

    it("should load the creation status from the policy model factory", function () {
      expect(policyModelFactoryMock.getProcessStatus).toHaveBeenCalled();
      expect(ctrl.status).toEqual(fakeCreationStatus);
    });

    describe("should open a modal when user wants to create a policy", function () {
      it("Policy modal is open", function () {
        var expectedController = "PolicyCreationModalCtrl";
        var expectedTemplateUrl = "templates/modal/policy-creation-modal.tpl.html";
        var expectedResolve = {};
        var expectedExtraClass = "";
        var expectedSize = "lg";

        var openModalArgs = modalServiceMock.openModal.calls.mostRecent().args;

        expect(openModalArgs[0]).toEqual(expectedController);
        expect(openModalArgs[1]).toEqual(expectedTemplateUrl);
        expect(openModalArgs[2]).toEqual(expectedResolve);
        expect(openModalArgs[3]).toEqual(expectedExtraClass);
        expect(openModalArgs[4]).toEqual(expectedSize);
      })
    })
  });

  describe("should be able to confirm the sent of the created policy", function () {
    it("modal is opened with the correct params", function () {
      ctrl.confirmPolicy();

      expect(modalServiceMock.openModal.calls.mostRecent().args[0]).toBe("ConfirmModalCtrl");
      expect(modalServiceMock.openModal.calls.mostRecent().args[1]).toBe("templates/modal/confirm-modal.tpl.html");
      var resolve = (modalServiceMock.openModal.calls.mostRecent().args[2]);
      expect(resolve.title()).toBe("_POLICY_._WINDOW_._CONFIRM_._TITLE_");
      expect(resolve.message()).toBe("");
    });

    it("when modal is confirmed, the policy is sent using an http request", function () {
      ctrl.confirmPolicy().then(function () {
        expect(policyModelFactoryMock.getFinalJSON).toHaveBeenCalled();
        expect(policyFactoryMock.createPolicy).toHaveBeenCalledWith(fakeFinalPolicyJSON);
      });
      scope.$digest();
    });

    it("If the policy is sent successfully, user is redirected to policy list page and policy is reset", function () {
      ctrl.confirmPolicy().then(function () {
        expect(policyModelFactoryMock.resetPolicy).toHaveBeenCalled();
        expect(stateMock.go).toHaveBeenCalledWith("dashboard.policies");
      });
      scope.$digest();
    });

    it("If the policy sent fails, the occurred error is shown", function () {
      policyFactoryMock.createPolicy.and.callFake(rejectedPromise);
      ctrl.confirmPolicy().then(function () {
        expect(ctrl.error).toEqual(fakeError.data);
      });
      scope.$digest();
    });
  });
});
