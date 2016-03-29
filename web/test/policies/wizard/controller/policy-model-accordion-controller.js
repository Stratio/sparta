describe('policies.wizard.controller.policy-model-accordion-controller', function () {
  var ctrl, scope, translate, fakeTranslation, fakePolicy, fakePolicyTemplate, fakeModel, policyModelFactoryMock,
    modelFactoryMock, cubeServiceMock, modelServiceMock, triggerServiceMock, wizardStatusServiceMock = null;

  beforeEach(module('webApp'));
  beforeEach(module('model/policy.json'));
  beforeEach(module('template/policy.json'));
  beforeEach(module('model/transformation.json'));

  beforeEach(inject(function ($controller, $q, $httpBackend, $rootScope) {
    scope = $rootScope.$new();
    fakeTranslation = "fake translation";
    translate = jasmine.createSpy().and.returnValue(fakeTranslation);

    inject(function (_modelPolicy_, _templatePolicy_, _modelTransformation_) {
      fakePolicy = angular.copy(_modelPolicy_);
      fakePolicyTemplate = _templatePolicy_;
      fakeModel = _modelTransformation_;
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

    modelFactoryMock = jasmine.createSpyObj('ModelFactory', ['resetModel', 'getModel', 'setModel', 'isValidModel', 'updateModelInputs']);
    modelFactoryMock.getModel.and.returnValue(fakeModel);

    cubeServiceMock = jasmine.createSpyObj('CubeService', ['findCubesUsingOutputs']);

    modelServiceMock = jasmine.createSpyObj('ModelService', ['isActiveModelCreationPanel', 'changeModelCreationPanelVisibility', 'getModelCreationStatus', 'activateModelCreationPanel', 'disableModelCreationPanel', 'resetModel']);
    triggerServiceMock = jasmine.createSpyObj('TriggerService', ['setTriggerContainer', 'changeVisibilityOfHelpForSql', 'getTriggerCreationStatus', 'activateTriggerCreationPanel', 'disableTriggerCreationPanel', 'isActiveTriggerCreationPanel']);


    ctrl = $controller('PolicyModelAccordionCtrl  as vm', {
      'WizardStatusService': wizardStatusServiceMock,
      'PolicyModelFactory': policyModelFactoryMock,
      'ModelFactory': modelFactoryMock,
      'CubeService': cubeServiceMock,
      'ModelService': modelServiceMock,
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

  describe("should be able to see changes in the accordion status to update the model of the model factory", function () {
    describe("if the new value of the accordion status is not null should find the model that has been opened by user, and send it to the model factory ", function () {
      var models, fakeModel2 = null;
      beforeEach(function () {
        fakeModel2 = angular.copy(fakeModel);
        fakeModel2.name = "fake model 2";
        fakeModel2.order = 1;
        models = [fakeModel, fakeModel2];
      });
      it("if position is between 0 and policy models length, the factory model is updated with the model of that position in the policy model array", function () {
        ctrl.policy.transformations = models;
        var position = 1;
        ctrl.modelAccordionStatus[position] = true;

        ctrl.changeOpenedModel(position);

        expect(modelFactoryMock.setModel).toHaveBeenCalledWith(fakeModel2, 1);
      });
      it("if position is not between 0 and policy models length, the factory model is reset with the order of the previous model", function () {
        var fakeModel2 = angular.copy(fakeModel);
        fakeModel2.name = "fake model 2";

        var models = [fakeModel, fakeModel2];
        ctrl.policy.transformations = models;
        var position = 2;
        ctrl.modelAccordionStatus[position] = true;

        ctrl.changeOpenedModel(position);

        expect(modelServiceMock.resetModel).toHaveBeenCalledWith(fakePolicyTemplate);
      })
    })
  });

  describe("Should be able to activate the model creation panel", function () {
    beforeEach(function () {
      ctrl.activateModelCreationPanel();
    });
    it("model accordion status is updated to show last position", function () {
      expect(ctrl.modelAccordionStatus[ctrl.modelAccordionStatus.length - 1]).toBeTruthy();
    });

    it("trigger service is called to disable the trigger creation", function () {
      expect(triggerServiceMock.disableTriggerCreationPanel).toHaveBeenCalled();
    });

    it("model service is called to modify visibility of new model panel and reset model", function () {
      expect(modelServiceMock.activateModelCreationPanel).toHaveBeenCalled();
      expect(modelServiceMock.resetModel).toHaveBeenCalledWith(ctrl.template);
    });
  });

  describe("Should be able to activate the trigger creation panel", function () {
    beforeEach(function () {
      ctrl.activateTriggerCreationPanel();
    });

    it("trigger accordion status is updated to show last position", function () {
      expect(ctrl.triggerAccordionStatus[ctrl.triggerAccordionStatus.length - 1]).toBeTruthy();
    });

    it("model service is called to disable the model creation", function () {
      expect(modelServiceMock.disableModelCreationPanel).toHaveBeenCalled();
    });

    it("trigger service is called to modify visibility of new trigger panel", function () {
      expect(triggerServiceMock.activateTriggerCreationPanel).toHaveBeenCalled();
    });
  });

  describe("Should be able to respond to an event to force the validations of current forms", function () {
    beforeEach(function () {

    });
    it("if transformation array is empty, policy error is updated to 'at least one transformation is needed'", function () {
      ctrl.policy.transformations = [];
      scope.$broadcast("forceValidateForm");

      expect(policyModelFactoryMock.setError).toHaveBeenCalledWith('_ERROR_._TRANSFORMATION_STEP_', 'error');
    });
    it("if transformation array is not empty, but user is creating a transformation or trigger, " +
      "policy error is updated to warn user about saving his changes'", function () {
      ctrl.policy.transformations = [fakeModel];
      ctrl.isActiveModelCreationPanel.and.returnValue(true);
      ctrl.isActiveTriggerCreationPanel.and.returnValue(false);

      scope.$broadcast("forceValidateForm");

      expect(policyModelFactoryMock.setError).toHaveBeenCalledWith('_ERROR_._CHANGES_WITHOUT_SAVING_', 'error');

      ctrl.isActiveModelCreationPanel.and.returnValue(false);
      ctrl.isActiveTriggerCreationPanel.and.returnValue(true);

      scope.$broadcast("forceValidateForm");

      expect(policyModelFactoryMock.setError).toHaveBeenCalledWith('_ERROR_._CHANGES_WITHOUT_SAVING_', 'error');
    });

    it("if transformation or trigger creation are activated, creation panel is opened", function(){
      ctrl.policy.transformations = [fakeModel];
      ctrl.isActiveModelCreationPanel.and.returnValue(true);

      scope.$broadcast("forceValidateForm");

      expect(ctrl.modelAccordionStatus[ctrl.modelAccordionStatus.length-1]).toBeTruthy();

      ctrl.isActiveTriggerCreationPanel.and.returnValue(true);

      scope.$broadcast("forceValidateForm");

      expect(ctrl.triggerAccordionStatus[ctrl.triggerAccordionStatus.length-1]).toBeTruthy();
    })
  });

  describe("Should be able to see changes in model and trigger creation status and transformation array in order to enable or disable next step", function(){
    it ("next step is enabled only if model and trigger creation are not activated and transformation array is not empty", function(){
      ctrl.modelCreationStatus = {};
      ctrl.triggerCreationStatus = {};
      ctrl.policy.transformations = [];

      ctrl.modelCreationStatus.enabled = true;
      ctrl.triggerCreationStatus.enabled = true;
      scope.$apply();

      expect(wizardStatusServiceMock.disableNextStep).toHaveBeenCalled();

      ctrl.policy.transformations = [fakeModel];
      ctrl.modelCreationStatus.enabled = true;
      ctrl.triggerCreationStatus.enabled = false;
      scope.$apply();

      expect(wizardStatusServiceMock.disableNextStep).toHaveBeenCalled();

      ctrl.modelCreationStatus.enabled = true;
      ctrl.triggerCreationStatus.enabled = true;
      scope.$apply();

      expect(wizardStatusServiceMock.disableNextStep).toHaveBeenCalled();

      ctrl.modelCreationStatus.enabled = false;
      ctrl.triggerCreationStatus.enabled = true;
      scope.$apply();

      expect(wizardStatusServiceMock.disableNextStep).toHaveBeenCalled();

      ctrl.triggerCreationStatus.enabled = true;

      ctrl.modelCreationStatus.enabled = true;

      scope.$apply();

      expect(wizardStatusServiceMock.disableNextStep).toHaveBeenCalled();
      ctrl.modelCreationStatus.enabled = false;
      ctrl.triggerCreationStatus.enabled = false;
      scope.$apply();

      expect(wizardStatusServiceMock.enableNextStep).toHaveBeenCalled();
    })
  });
});

