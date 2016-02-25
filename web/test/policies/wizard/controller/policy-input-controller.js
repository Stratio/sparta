describe('policies.wizard.controller.policy-input-controller', function () {
  beforeEach(module('webApp'));
  beforeEach(module('served/policy.json'));
  beforeEach(module('served/policyTemplate.json'));
  beforeEach(module('served/inputList.json'));

  var ctrl, rootScope, fakePolicy, fakeTemplate, fragmentFactoryMock, fakeInputList, policyModelFactoryMock = null;

  // init mock modules

  beforeEach(inject(function ($controller, $q, $httpBackend, $rootScope) {
    rootScope = $rootScope;
    inject(function (_servedPolicy_, _servedPolicyTemplate_, _servedInputList_) {
      fakePolicy = angular.copy(_servedPolicy_);
      fakeTemplate = _servedPolicyTemplate_;
      fakeInputList = _servedInputList_;
    });

    $httpBackend.when('GET', 'languages/en-US.json')
      .respond({});

    fragmentFactoryMock = jasmine.createSpyObj('FragmentFactory', ['getFragments']);
    fragmentFactoryMock.getFragments.and.callFake(function () {
      var defer = $q.defer();
      defer.resolve(fakeInputList);
      return defer.promise;
    });

    policyModelFactoryMock = jasmine.createSpyObj('PolicyModelFactory', ['getCurrentPolicy', 'getTemplate','previousStep', 'nextStep', 'enableNextStep']);
    policyModelFactoryMock.getCurrentPolicy.and.callFake(function () {
      return fakePolicy;
    });

    policyModelFactoryMock.getTemplate.and.callFake(function () {
      return fakeTemplate;
    });

    ctrl = $controller('PolicyInputCtrl', {
      'FragmentFactory': fragmentFactoryMock,
      'PolicyModelFactory': policyModelFactoryMock
    });

    rootScope.$digest(); // update state of init promise
  }));

  it('should get a policy template from policy factory', function () {
    expect(ctrl.template).toBe(fakeTemplate);
  });

  it('should get the policy that is being created or edited from policy factory', function () {
    expect(ctrl.policy).toBe(fakePolicy);
  });

  it('should get a list of available inputs', function () {
    expect(ctrl.inputList).toBe(fakeInputList);
  });

  describe('should be able to set an input to the policy using the position in the input list', function () {
    it("if position is < 0, input is not changed", function () {
      var invalidPosition = -1;
      ctrl.setInput(invalidPosition);

      expect(ctrl.policy.input).toEqual({});
    });

    it("if position is >= inputList length, input is not changed", function () {
      var invalidPosition = fakeInputList.length;
      ctrl.setInput(invalidPosition);

      expect(ctrl.policy.input).toEqual({});
    });

    it("if position is valid, input is changed and next step is enabled", function () {
      var validPosition = fakeInputList.length - 1;
      var expectedInput = fakeInputList[validPosition];
      ctrl.setInput(validPosition);

      expect(ctrl.policy.input).toBe(expectedInput);

      expect(policyModelFactoryMock.enableNextStep).toHaveBeenCalled();
    });

  });

  describe('should be able to return if input is selected or not', function () {
    beforeEach(function () {
      ctrl.policy.input = fakeInputList[0];
    });
    it("if name to check is empty or undefined, returns false", function () {
      //empty name
      var invalidName = "";
      var isSelected = ctrl.isSelectedInput(invalidName);
      expect(isSelected).toBeFalsy();

      //undefined name
      invalidName = undefined;
      isSelected = ctrl.isSelectedInput(invalidName);
      expect(isSelected).toBeFalsy();

      //null name
      invalidName = null;
      isSelected = ctrl.isSelectedInput(invalidName);
      expect(isSelected).toBeFalsy();
    });

    it("if name to check is valid but it is not selected, returns false", function () {
      var validName = "any input";
      var isSelected = ctrl.isSelectedInput(validName);
      expect(isSelected).toBeFalsy();
    });

    it("if name to check is valid and it is selected, returns true", function () {
      var validName = fakeInputList[0].name;
      var isSelected = ctrl.isSelectedInput(validName);
      expect(isSelected).toBeTruthy();
    });

    it ("if policy input is undefined, returns false", function(){
      var validName = fakeInputList[0].name;
      ctrl.policy.input = null;
      var isSelected = ctrl.isSelectedInput(validName);
      expect(isSelected).toBeFalsy();
    });

  });

  it ("should be able to change to previous step calling to policy model factory", function(){
    ctrl.previousStep();

    expect(policyModelFactoryMock.previousStep).toHaveBeenCalled();
  });

  describe ("should be able to validate the form data introduced by user in order to change to the next step", function(){
    it ("if any input is not selected, error is generated and step is not changed", function(){
      ctrl.policy.input = {};
      ctrl.validateForm();

      expect(ctrl.error).toBeTruthy();
      expect(policyModelFactoryMock.nextStep).not.toHaveBeenCalled();
    });

    it ("if an input is selected, error is not generated and step is changed", function(){
      ctrl.policy.input = fakeInputList[0];
      ctrl.validateForm();

      expect(ctrl.error).toBeFalsy();
      expect(policyModelFactoryMock.nextStep).toHaveBeenCalled();
    });

  })
});
