describe('Policy description controller', function () {
  beforeEach(module('webApp'));
  beforeEach(module('served/policy.json'));

    var ctrl, fakePolicy, fakeAllPoliciesResponse = null;


  // init mock modules

  var policyModelFactoryMock = jasmine.createSpyObj('PolicyModelFactory', ['getCurrentPolicy', 'nextStep']);
  policyModelFactoryMock.getCurrentPolicy.and.callFake(function () {
    return fakePolicy;
  });
  var policyStaticDataFactoryMock = jasmine.createSpyObj('PolicyStaticDataFactory', ['getSparkStreamingWindow',
    'getCheckpointInterval', 'getCheckpointAvailability', 'getPartitionFormat', 'getStorageLevel', 'getHelpLinks']);

  policyStaticDataFactoryMock.getHelpLinks.and.callFake(function () {
    return {description: {}}
  });

  var policyFactoryMock = jasmine.createSpyObj('PolicyFactory', ['getAllPolicies']);

  beforeEach(inject(function ($controller, $q, $httpBackend) {
    inject(function (_servedPolicy_) {
      fakePolicy = _servedPolicy_;
      fakeAllPoliciesResponse = [{policy: fakePolicy, status: "RUNNING"}];
    });

    $httpBackend.when('GET', 'languages/en-US.json')
      .respond({});

    policyFactoryMock.getAllPolicies.and.callFake(function () {
      var defer = $q.defer();
      defer.resolve(fakeAllPoliciesResponse);
      return defer.promise;
    });

    ctrl = $controller('PolicyDescriptionCtrl', {
      'PolicyModelFactory': policyModelFactoryMock,
      'PolicyStaticDataFactory': policyStaticDataFactoryMock,
      'PolicyFactory': policyFactoryMock
    });
  }));

  it('should get a policy from policy factory', function () {
    expect(ctrl.policy).toBe(fakePolicy);
  });

  describe("should validate form and policy before to change to the next step", function () {
    var rootScope, httpBackend;
    beforeEach(inject(function ($rootScope, $httpBackend) {
      rootScope = $rootScope;
      httpBackend = $httpBackend;
      policyModelFactoryMock.nextStep.calls.reset();
    }));

    describe("if view validations have been passed", function () {
      beforeEach(function () {
        ctrl.form = {$valid: true}; //view validations have been passed
      });
      it("It is invalid if there is another policy with the same name", function () {
        ctrl.policy = fakePolicy;
        ctrl.validateForm().then(function () {
          expect(ctrl.error).toBe(true);
        });
        httpBackend.flush();
        rootScope.$digest();
      });

      it("It is valid if there is not any policy with the same name", function () {
        ctrl.policy = angular.copy(fakePolicy);
        ctrl.policy.name = "new name";
        ctrl.validateForm().then(function () {
          expect(ctrl.error).toBe(false);
        });
        httpBackend.flush();
        rootScope.$digest();
      });

      it("It is valid if there is not any policy with the same name, next step is executed", function () {
        ctrl.policy = angular.copy(fakePolicy);
        ctrl.policy.name = "new name";
        ctrl.validateForm().then(function () {
          expect(policyModelFactoryMock.nextStep).toHaveBeenCalled();
        });
        httpBackend.flush();
        rootScope.$digest();
      });
    });

    describe("if view validations have not been passed", function () {
      beforeEach(function () {
        ctrl.form = {$valid: false}; //view validations have been passed
      });
      it("It is invalid and next step is not executed", function () {
        ctrl.policy = fakePolicy;
        ctrl.validateForm().then(function () {
          expect(ctrl.error).toBe(true);
          expect(policyModelFactoryMock.nextStep).not.toHaveBeenCalled();
        });

        httpBackend.flush();
        rootScope.$digest();
      })
    });
  });


});
