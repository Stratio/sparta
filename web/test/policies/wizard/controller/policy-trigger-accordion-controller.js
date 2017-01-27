describe('policies.wizard.controller.policy-model-accordion-controller', function () {
  var ctrl, scope, translate, fakeTranslation, fakePolicy, fakePolicyTemplate, fakeTrigger, policyModelFactoryMock,
      triggerServiceMock, wizardStatusServiceMock = null;

  beforeEach(module('webApp'));
  beforeEach(module('model/policy.json'));
  beforeEach(module('template/policy.json'));

  beforeEach(inject(function ($controller, $q, $httpBackend, $rootScope) {
    scope = $rootScope.$new();
    fakeTranslation = "fake translation";
    translate = jasmine.createSpy().and.returnValue(fakeTranslation);

    inject(function (_modelPolicy_, _templatePolicy_) {
      fakePolicy = angular.copy(_modelPolicy_);
      fakePolicyTemplate = _templatePolicy_;
    });

    $httpBackend.when('GET', 'languages/en-US.json')
      .respond({});

    wizardStatusServiceMock = jasmine.createSpyObj('WizardStatusService', ['enableNextStep', 'disableNextStep']);

    policyModelFactoryMock = jasmine.createSpyObj('PolicyModelFactory', ['getCurrentPolicy', 'getTemplate', 'previousStep', 'nextStep', 'enableNextStep', 'setError']);
    policyModelFactoryMock.getCurrentPolicy.and.callFake(function () {
      return fakePolicy;
    });

    policyModelFactoryMock.getTemplate.and.callFake(function () {
      return fakePolicyTemplate;
    });

    triggerServiceMock = jasmine.createSpyObj('TriggerService', ['setTriggerContainer', 'changeVisibilityOfHelpForSql', 'getTriggerCreationStatus', 'activateTriggerCreationPanel', 'disableTriggerCreationPanel', 'isActiveTriggerCreationPanel']);


    ctrl = $controller('PolicyTriggerAccordionCtrl  as vm', {
      'WizardStatusService': wizardStatusServiceMock,
      'PolicyModelFactory': policyModelFactoryMock,
      '$translate': translate,
      'TriggerService': triggerServiceMock,
      '$scope': scope
    });

  }));

  describe("when it is initialized", function () {

    it('it should get a policy template from from policy factory', function () {
      expect(ctrl.template).toBe(fakePolicyTemplate);
    });

    it('it should get the policy that is being created or edited from policy factory', function () {
      expect(ctrl.policy).toBe(fakePolicy);
    });

    it('it should put as trigger container the attribute streamTriggers of policy', function () {
      expect(triggerServiceMock.setTriggerContainer).toHaveBeenCalledWith(ctrl.policy.streamTriggers, "transformation");
    });

  });

  describe("Should be able to activate the trigger creation panel", function () {
    beforeEach(function () {
      ctrl.activateTriggerCreationPanel();
    });

    it("trigger accordion status is updated to show last position", function () {
      expect(ctrl.triggerAccordionStatus[ctrl.triggerAccordionStatus.length - 1]).toBeTruthy();
    });

    it("trigger service is called to modify visibility of new trigger panel", function () {
      expect(triggerServiceMock.activateTriggerCreationPanel).toHaveBeenCalled();
    });
  });

  describe("Should be able to respond to an event to force the validations of current forms", function () {
    beforeEach(function () {

    });
    it("If user is creating a trigger, " +
      "policy error is updated to warn user about saving his changes'", function () {
      ctrl.isActiveTriggerCreationPanel.and.returnValue(true);

      scope.$broadcast("forceValidateForm");

      expect(policyModelFactoryMock.setError).toHaveBeenCalledWith('_ERROR_._CHANGES_WITHOUT_SAVING_', 'error');
    });

    it("if trigger creation are activated, creation panel is opened", function(){
      ctrl.isActiveTriggerCreationPanel.and.returnValue(true);

      scope.$broadcast("forceValidateForm");

      expect(ctrl.triggerAccordionStatus[ctrl.triggerAccordionStatus.length-1]).toBeTruthy();
    })
  });

  describe("Should be able to see changes in trigger creation status in order to enable or disable next step", function(){
    it ("next step is enabled only if trigger creation are not activated", function(){
      ctrl.triggerCreationStatus = {};

      ctrl.triggerCreationStatus.enabled = true;
      scope.$apply();

      expect(wizardStatusServiceMock.disableNextStep).toHaveBeenCalled();

      ctrl.triggerCreationStatus.enabled = false;
      scope.$apply();

      expect(wizardStatusServiceMock.disableNextStep).toHaveBeenCalled();
    })
  });
});

