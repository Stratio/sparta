describe('policies.wizard.controller.policy-output-controller', function () {
  beforeEach(module('webApp'));
  beforeEach(module('served/policy.json'));
  beforeEach(module('served/policyTemplate.json'));
  beforeEach(module('served/outputList.json'));

  var ctrl, scope, fakePolicy, fakeTemplate, fragmentFactoryMock, fakeOutputList, policyModelFactoryMock = null;

  // init mock modules

  beforeEach(inject(function ($controller, $q, $httpBackend, $rootScope, _servedPolicy_, _servedPolicyTemplate_, _servedOutputList_) {
    scope = $rootScope.$new();
    fakePolicy = angular.copy(_servedPolicy_);
    fakeTemplate = _servedPolicyTemplate_;
    fakeOutputList = _servedOutputList_;

    $httpBackend.when('GET', 'languages/en-US.json')
      .respond({});

    fragmentFactoryMock = jasmine.createSpyObj('FragmentFactory', ['getFragments']);
    fragmentFactoryMock.getFragments.and.callFake(function () {
      var defer = $q.defer();
      defer.resolve(fakeOutputList);
      return defer.promise;
    });

    policyModelFactoryMock = jasmine.createSpyObj('PolicyModelFactory', ['getCurrentPolicy', 'getTemplate', 'previousStep', 'nextStep']);
    policyModelFactoryMock.getCurrentPolicy.and.callFake(function () {
      return fakePolicy;
    });

    policyModelFactoryMock.getTemplate.and.callFake(function () {
      return fakeTemplate;
    });

    ctrl = $controller('PolicyOutputCtrl', {
      'FragmentFactory': fragmentFactoryMock,
      'PolicyModelFactory': policyModelFactoryMock
    });

    scope.$digest(); // update state of init promise
  }));

  describe("when it is initialized", function () {

    it('should get a policy template from policy factory', function () {
      expect(ctrl.template).toBe(fakeTemplate);
    });

    it('should get the policy that is being created or edited from policy factory', function () {
      expect(ctrl.policy).toBe(fakePolicy);
    });

    it('should get a list of available outputs retrieved from the fragment factory', function () {
      expect(fragmentFactoryMock.getFragments).toHaveBeenCalledWith('output');
      expect(ctrl.outputList).toBe(fakeOutputList);
    });

    it("should sort the policy outputs according to the output list", inject(function ($controller) {
      fakePolicy.outputs = [fakeOutputList[2], fakeOutputList[1], fakeOutputList[0], fakeOutputList[4]];
      var expectedOrderedOutputs = [fakeOutputList[0], fakeOutputList[1], fakeOutputList[2], undefined, fakeOutputList[4]];
      ctrl = $controller('PolicyOutputCtrl', {
        'FragmentFactory': fragmentFactoryMock,
        'PolicyModelFactory': policyModelFactoryMock
      });
      scope.$digest();

      expect(ctrl.policy.outputs[0]).toEqual(expectedOrderedOutputs[0]);
      expect(ctrl.policy.outputs[1]).toEqual(expectedOrderedOutputs[1]);
      expect(ctrl.policy.outputs[2]).toEqual(expectedOrderedOutputs[2]);
      expect(ctrl.policy.outputs[3]).toEqual(expectedOrderedOutputs[3]);
      expect(ctrl.policy.outputs[4]).toEqual(expectedOrderedOutputs[4]);
    }));
  });

  describe("should be able to add and remove an output when it is selected using it index", function () {
    it("if the policy output of the position introduced is not null, puts it to null (to remove it from the policy outputs)", function () {
      // remove output from policy output list
      var index = 2;
      ctrl.policy.outputs[index] = fakeOutputList[0];

      ctrl.setOutput(index);

      expect(ctrl.policy.outputs[index]).toBe(null);
    });
    it("if the policy output of the position introduced is  null, updates it with the element from controller output list of that position", function () {
      // add to policy output list
      var index = 2;
      ctrl.policy.outputs[index] = null;

      ctrl.setOutput(index);

      expect(ctrl.policy.outputs[index]).toBe(fakeOutputList[index]);
    });
  });

  it("should be able to change to previous step calling to policy model factory", function () {
    ctrl.previousStep();

    expect(policyModelFactoryMock.previousStep).toHaveBeenCalled();
  });


  describe("should be able to validate the form data introduced by user in order to change to the next step", function () {

    it("it should put the state of form to submitted", function () {
      ctrl.validateForm();

      expect(ctrl.formSubmmited).toBe(true);
    });

    it("if any output is not selected, error is generated and step is not changed", function () {
      ctrl.policy.outputs = [];
      ctrl.validateForm();

      expect(ctrl.error).toBeTruthy();
      expect(policyModelFactoryMock.nextStep).not.toHaveBeenCalled();
    });

    it("if an output is selected at least, error is not generated and step is changed", function () {
      ctrl.policy.outputs = [fakeOutputList[0]];
      ctrl.validateForm();

      expect(ctrl.error).toBeFalsy();
      expect(policyModelFactoryMock.nextStep).toHaveBeenCalled();
    });
  })
});
