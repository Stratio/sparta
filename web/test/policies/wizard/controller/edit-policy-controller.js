describe('policies.wizard.controller.edit-policy-controller', function () {
  beforeEach(module('webApp'));
  beforeEach(module('served/policy.json'));
  beforeEach(module('served/policyTemplate.json'));

  var ctrl, scope, fakePolicy, fakeTemplate, fakeError, policyModelFactoryMock, templateFactoryMock, policyFactoryMock,
    stateMock, fakeCreationStatus, modalServiceMock, resolvedPromise, rejectedPromise, fakeFinalPolicyJSON, stateParamsMock, fakePolicyId;

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
    policyFactoryMock = jasmine.createSpyObj('PolicyFactory', ['savePolicy', 'getPolicyById']);
    policyFactoryMock.savePolicy.and.callFake(resolvedPromise);
    policyFactoryMock.getPolicyById.and.callFake(function () {
      var defer = $q.defer();
      defer.resolve(fakePolicy);
      return defer.promise;
    });

    stateMock = jasmine.createSpyObj('$state', ['go']);

    fakePolicyId = "fake policy id";
    stateParamsMock = {"id": fakePolicyId};

    policyModelFactoryMock = jasmine.createSpyObj('PolicyModelFactory', ['getCurrentPolicy', 'getFinalJSON', 'setPolicy', 'setTemplate', 'getTemplate', 'getProcessStatus', 'resetPolicy']);
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

    ctrl = $controller('EditPolicyCtrl', {
      'PolicyModelFactory': policyModelFactoryMock,
      'TemplateFactory': templateFactoryMock,
      'PolicyFactory': policyFactoryMock,
      'ModalService': modalServiceMock,
      '$state': stateMock,
      '$stateParams': stateParamsMock
    });

    scope.$digest();
  }));

  describe("when it is initialized", function () {

    it('it should get the policy template from from a template factory and put it to the policy model factory', function () {
      expect(templateFactoryMock.getPolicyTemplate).toHaveBeenCalled();
      expect(policyModelFactoryMock.setTemplate).toHaveBeenCalledWith(fakeTemplate);
    });

    it("it should load the policy  using the id loaded as param of the url", function () {
      expect(policyFactoryMock.getPolicyById).toHaveBeenCalledWith(fakePolicyId);
    });

    it("should load the steps of the policy creation/edition from the policy template", function () {
      expect(ctrl.steps).toBe(fakeTemplate.steps);
    });

    it("should load the policy that is going to be edited using the policy retrieved by id", function () {
      expect(policyModelFactoryMock.setPolicy).toHaveBeenCalledWith(fakePolicy);
      expect(ctrl.policy).toBe(fakePolicy);
    });

    it("should load the edition status from the policy model factory", function () {
      expect(policyModelFactoryMock.getProcessStatus).toHaveBeenCalled();
      expect(ctrl.status).toEqual(fakeCreationStatus);
    });
  });

  describe("should be able to confirm the sent of the modified policy", function () {
    it("modal is opened with the correct params", function () {
      ctrl.confirmPolicy();

      expect(modalServiceMock.openModal.calls.mostRecent().args[0]).toBe("ConfirmModalCtrl");
      expect(modalServiceMock.openModal.calls.mostRecent().args[1]).toBe("templates/modal/confirm-modal.tpl.html");
      var resolve = (modalServiceMock.openModal.calls.mostRecent().args[2]);
      expect(resolve.title()).toBe("_POLICY_._WINDOW_._EDIT_._TITLE_");
      expect(resolve.message()).toBe("");
    });

    it("when modal is confirmed, the policy is sent using an http request", function () {
      ctrl.confirmPolicy().then(function () {
        expect(policyModelFactoryMock.getFinalJSON).toHaveBeenCalled();
        expect(policyFactoryMock.savePolicy).toHaveBeenCalledWith(fakeFinalPolicyJSON);
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
      policyFactoryMock.savePolicy.and.callFake(rejectedPromise);
      ctrl.confirmPolicy().then(function () {
        expect(ctrl.error).toEqual(fakeError.data);
      });
      scope.$digest();
    });
  });
});
