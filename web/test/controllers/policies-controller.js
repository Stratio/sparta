describe('policies.wizard.controller.policies-controller', function() {
  beforeEach(module('webApp'));
  beforeEach(module('api/policyList.json'));
  beforeEach(module('api/policiesStatusList.json'));

  var ctrl, scope, $interval, $timeout, fakePolicyList, fakePolicyStatusList, fakeError, policyModelFactoryMock, policyFactoryMock,
      stateMock, fakeCreationStatus, modalServiceMock, resolvedPromise, rejectedPromise, fakeFinalPolicyJSON, wizardStatusServiceMock;

  // init mock modules

  beforeEach(inject(function($controller, $q, $httpBackend, $rootScope, _apiPolicyList_, _apiPoliciesStatusList_, _$interval_, _$timeout_) {
    scope = $rootScope.$new();
    $interval = _$interval_;
    $timeout = _$timeout_;
    fakePolicyList = _apiPolicyList_;
    fakePolicyStatusList = _apiPoliciesStatusList_;
    resolvedPromise = function() {
      var defer = $q.defer();
      defer.resolve();

      return defer.promise;
    };

    fakeError = {"data": {"i18nCode": "fake error message"}};
    rejectedPromise = function() {
      var defer = $q.defer();
      defer.reject(fakeError);

      return defer.promise;
    };

    $httpBackend.when('GET', 'languages/en-US.json')
        .respond({});
    fakeCreationStatus = {"currentStep": 0};
    policyFactoryMock = jasmine.createSpyObj('PolicyFactory', ['createPolicy', 'getAllPolicies', 'getPoliciesStatus', 'runPolicy', 'stopPolicy', 'downloadPolicy', 'deletePolicyCheckpoint']);
    policyFactoryMock.getAllPolicies.and.callFake(function() {
      var defer = $q.defer();
      defer.resolve(fakePolicyList);
      return defer.promise;
    });


    stateMock = jasmine.createSpyObj('$state', ['go']);

    wizardStatusServiceMock = jasmine.createSpyObj('wizardStatusService', ['getStatus']);
    wizardStatusServiceMock.getStatus.and.returnValue(fakeCreationStatus);
    policyModelFactoryMock = jasmine.createSpyObj('PolicyModelFactory', ['setError',
      'getError', 'getProcessStatus', 'resetPolicy']);

    fakeFinalPolicyJSON = {"fake_attribute": "fake value"};

    modalServiceMock = jasmine.createSpyObj('ModalService', ['openModal']);

    modalServiceMock.openModal.and.callFake(function() {
      var defer = $q.defer();
      defer.resolve();
      return {"result": defer.promise};
    });

    ctrl = $controller('PolicyListCtrl', {
      'WizardStatusService': wizardStatusServiceMock,
      'PolicyModelFactory': policyModelFactoryMock,
      'PolicyFactory': policyFactoryMock,
      'ModalService': modalServiceMock,
      '$state': stateMock,
      '$scope': scope
    });

    scope.$digest();

  }));

  describe("when it is initialized", function() {
    describe("Should request the current policy list and their status", function() {
      beforeEach(function() {
        policyFactoryMock.getPoliciesStatus.calls.reset();
      });


    })
  });

  describe("should be able to download a policy", function() {
    it("policy factory is called to download the policy passing its id", function() {
      var fakePolicyId = "fake policy id";
      policyFactoryMock.downloadPolicy.and.callFake(resolvedPromise);
      ctrl.downloadPolicy(fakePolicyId);

      expect(policyFactoryMock.downloadPolicy).toHaveBeenCalledWith(fakePolicyId);
    });

    it("a hidden element is created in order to force the file downloading", function() {

    })
  });

  describe("should be able to sort the current policy list", function(){

    beforeEach(function() {
      ctrl.tableReverse = false;
      ctrl.sortField = "name";
    });

    it("new sort field is different from the current one, it is sorted by that field", function() {
      var oldSortField = ctrl.sortField;
      ctrl.sortPolicies("description");
      expect(ctrl.sortField).not.toEqual(oldSortField);
      expect(ctrl.tableReverse).toBe(false);
    });

    it("new sort field is the same from the current one, change current reverse order", function() {
      var oldSortField = ctrl.sortField;
      ctrl.sortPolicies("name");
      expect(ctrl.sortField).toEqual(oldSortField);
      expect(ctrl.tableReverse).toBe(true);
    });
  });
});
