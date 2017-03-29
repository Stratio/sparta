describe('policies.wizard.controller.policy-input-controller', function () {
  beforeEach(module('webApp'));
  beforeEach(module('model/policy.json'));
  beforeEach(module('template/policy.json'));
  beforeEach(module('api/inputList.json'));

  var ctrl, scope, fakePolicy, fakeTemplate, fragmentFactoryMock, fakeInputList, policyModelFactoryMock, wizardStatusServiceMock = null;

  // init mock modules

  beforeEach(inject(function ($controller, $q, $httpBackend, $rootScope) {
    scope = $rootScope.$new();
    inject(function (_modelPolicy_, _templatePolicy_, _apiInputList_) {
      fakePolicy = angular.copy(_modelPolicy_);
      fakeTemplate = _templatePolicy_;
      fakeInputList = _apiInputList_;
    });

    $httpBackend.when('GET', 'languages/en-US.json')
      .respond({});

    wizardStatusServiceMock =  jasmine.createSpyObj('WizardStatusService', ['enableNextStep', 'previousStep', 'nextStep', 'disableNextStep']);

    fragmentFactoryMock = jasmine.createSpyObj('FragmentFactory', ['getFragments']);
    fragmentFactoryMock.getFragments.and.callFake(function () {
      var defer = $q.defer();
      defer.resolve(fakeInputList);
      return defer.promise;
    });

    policyModelFactoryMock = jasmine.createSpyObj('PolicyModelFactory', ['getCurrentPolicy', 'getTemplate', 'setError']);
    policyModelFactoryMock.getCurrentPolicy.and.callFake(function () {
      return fakePolicy;
    });

    policyModelFactoryMock.getTemplate.and.callFake(function () {
      return fakeTemplate;
    });

    ctrl = $controller('PolicyInputCtrl', {
      'WizardStatusService': wizardStatusServiceMock,
      'FragmentFactory': fragmentFactoryMock,
      'PolicyModelFactory': policyModelFactoryMock,
      '$scope': scope
    });

    scope.$digest(); // update state of init promise
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
      var oldInput = ctrl.policy.input;
      ctrl.setInput(invalidPosition);

      expect(ctrl.policy.input).toEqual(oldInput);
    });

    it("if position is >= inputList length, input is not changed", function () {
      var invalidPosition = fakeInputList.length;
      var oldInput = ctrl.policy.input;
      ctrl.setInput(invalidPosition);

      expect(ctrl.policy.input).toEqual(oldInput);
    });

    it("if position is valid, input is changed and next step is enabled", function () {
      var validPosition = fakeInputList.length - 1;
      var expectedInput = fakeInputList[validPosition];
      ctrl.setInput(validPosition);

      expect(ctrl.policy.input).toBe(expectedInput);

      expect(wizardStatusServiceMock.enableNextStep).toHaveBeenCalled();
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

    expect(wizardStatusServiceMock.previousStep).toHaveBeenCalled();
  });

  describe ("should be able to validate the form data introduced by user in order to change to the next step", function(){
    it ("if any input is not selected, error is generated and step is not changed", function(){
      ctrl.policy.input = {};
      ctrl.validateForm();

      expect(policyModelFactoryMock.setError).toHaveBeenCalledWith("_ERROR_._POLICY_INPUTS_", 'error');
      expect(wizardStatusServiceMock.nextStep).not.toHaveBeenCalled();
    });

    it ("if an input is selected, error is not generated and step is changed", function(){
      ctrl.policy.input = fakeInputList[0];
      ctrl.validateForm();

      expect(ctrl.error).toBeFalsy();
      expect(wizardStatusServiceMock.nextStep).toHaveBeenCalled();
    });

  })
});
