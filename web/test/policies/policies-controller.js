describe('policies.wizard.controller.new-policy-controller', function () {
  beforeEach(module('webApp'));
  beforeEach(module('served/policy.json'));

  var ctrl, scope, fakePolicy, fakeTemplate, fakeError, policyModelFactoryMock, templateFactoryMock, policyFactoryMock,
    stateMock, fakeCreationStatus, modalServiceMock, resolvedPromise, rejectedPromise, fakeFinalPolicyJSON;

  // init mock modules

  beforeEach(inject(function ($controller, $q, $httpBackend, $rootScope, _servedPolicy_) {
    scope = $rootScope.$new();

    fakePolicy = angular.copy(_servedPolicy_);

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
    policyFactoryMock = jasmine.createSpyObj('PolicyFactory', ['createPolicy', 'getAllPolicies']);
    policyFactoryMock.getAllPolicies.and.callFake(resolvedPromise);
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

    ctrl = $controller('PoliciesCtrl', {
      'PolicyModelFactory': policyModelFactoryMock,
      'PolicyFactory': policyFactoryMock,
      'ModalService': modalServiceMock,
      '$state': stateMock,
      '$scope': scope
    });

    scope.$digest();
  }));


  describe("should open a modal when user wants to create a policy", function () {
    it("Policy modal is open", function () {
      var expectedController = "PolicyCreationModalCtrl";
      var expectedTemplateUrl = "templates/modal/policy-creation-modal.tpl.html";
      var expectedResolve = {};
      var expectedExtraClass = "";
      var expectedSize = "lg";

      ctrl.createPolicy();

      var openModalArgs = modalServiceMock.openModal.calls.mostRecent().args;

      expect(openModalArgs[0]).toEqual(expectedController);
      expect(openModalArgs[1]).toEqual(expectedTemplateUrl);
      expect(openModalArgs[2]).toEqual(expectedResolve);
      expect(openModalArgs[3]).toEqual(expectedExtraClass);
      expect(openModalArgs[4]).toEqual(expectedSize);
    });

    it("should redirect user to policy creation wizard when modal is confirmed", function(){
      ctrl.createPolicy().then(function(){

        expect(stateMock.go).toHaveBeenCalledWith('wizard.newPolicy');
      });

    });
  });


});
