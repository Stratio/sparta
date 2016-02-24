describe('policies.wizard.controller.policy-model-accordion-controller', function () {
  var ctrl, scope, translate, fakeTranslation, fakePolicy, fakePolicyTemplate, fakeModel, policyModelFactoryMock,
    accordionStatusServiceMock, modelFactoryMock, cubeServiceMock, ModelServiceMock, accordionStatus = null;

  beforeEach(module('webApp'));
  beforeEach(module('served/policy.json'));
  beforeEach(module('served/policyTemplate.json'));
  beforeEach(module('served/model.json'));

  beforeEach(inject(function ($controller, $q, $httpBackend, $rootScope) {
    scope = $rootScope.$new();
    fakeTranslation = "fake translation";
    translate = jasmine.createSpy().and.returnValue(fakeTranslation);

    inject(function (_servedPolicy_, _servedPolicyTemplate_, _servedModel_) {
      fakePolicy = angular.copy(_servedPolicy_);
      fakePolicyTemplate = _servedPolicyTemplate_;
      fakeModel = _servedModel_;
    });

    $httpBackend.when('GET', 'languages/en-US.json')
      .respond({});

    policyModelFactoryMock = jasmine.createSpyObj('PolicyModelFactory', ['getCurrentPolicy', 'getTemplate', 'previousStep', 'nextStep', 'enableNextStep']);
    policyModelFactoryMock.getCurrentPolicy.and.callFake(function () {
      return fakePolicy;
    });

    policyModelFactoryMock.getTemplate.and.callFake(function () {
      return fakePolicyTemplate;
    });


    modelFactoryMock = jasmine.createSpyObj('ModelFactory', ['resetModel', 'getModel', 'setModel', 'isValidModel', 'updateModelInputs']);
    modelFactoryMock.getModel.and.returnValue(fakeModel);

    accordionStatusServiceMock = jasmine.createSpyObj('AccordionStatusService', ['getAccordionStatus', 'resetAccordionStatus']);
    accordionStatus = [false, false];
    accordionStatusServiceMock.getAccordionStatus.and.returnValue(accordionStatus);
    cubeServiceMock = jasmine.createSpyObj('CubeService', ['findCubesUsingOutputs']);

    ModelServiceMock = jasmine.createSpyObj('ModelService', ['isActiveModelCreationPanel', 'changeModelCreationPanelVisibility']);

    spyOn(scope, "$watchCollection").and.callThrough();

    ctrl = $controller('PolicyModelAccordionCtrl  as vm', {
      'PolicyModelFactory': policyModelFactoryMock,
      'AccordionStatusService': accordionStatusServiceMock,
      'ModelFactory': modelFactoryMock,
      'CubeService': cubeServiceMock,
      'ModelService': ModelServiceMock,
      '$translate': translate,
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

    it('it should reset the accordion status and saved it as a variable', function () {
      expect(ctrl.accordionStatus).toEqual(accordionStatusServiceMock.getAccordionStatus());
      expect(accordionStatusServiceMock.resetAccordionStatus).toHaveBeenCalled();
    });

    it ("if policy has a model at least, next step is enabled", inject(function ($controller){
      fakePolicy.transformations = [fakeModel];
      ctrl = $controller('PolicyModelAccordionCtrl  as vm', {
        'PolicyModelFactory': policyModelFactoryMock,
        'AccordionStatusService': accordionStatusServiceMock,
        'ModelFactory': modelFactoryMock,
        'CubeService': cubeServiceMock,
        'ModelService': ModelServiceMock,
        '$translate': translate,
        '$scope': scope
      });

      expect(policyModelFactoryMock.enableNextStep).toHaveBeenCalled();
    }));
  });

  it("should be able to change to previous step calling to policy model factory", function () {
    ctrl.previousStep();

    expect(policyModelFactoryMock.previousStep).toHaveBeenCalled();
  });

  describe("should be able to change to next step calling to policy model factory", function () {
    it("if there is not any model added to policy, step is not changed", function () {
      ctrl.policy.transformations = [];
      ctrl.nextStep();

      expect(policyModelFactoryMock.nextStep).not.toHaveBeenCalled();
    });

    it("if there is a model added at least, step is changed", function () {
      ctrl.policy.transformations = [fakeModel];
      ctrl.nextStep();

      expect(policyModelFactoryMock.nextStep).toHaveBeenCalled();
    })

  });

  it("should be able to generate an index for each model", function () {
    expect(ctrl.generateIndex()).toBe(0);
    expect(ctrl.generateIndex()).toBe(1);
    expect(ctrl.generateIndex()).toBe(2);
    expect(ctrl.generateIndex()).toBe(3);
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
        accordionStatus[1] = true;

        scope.$digest();

        expect(scope.$watchCollection).toHaveBeenCalled();
        expect(modelFactoryMock.setModel).toHaveBeenCalledWith(fakeModel2, 1);
      });
      it("if position is not between 0 and policy models length, the factory model is reset with the order of the previous model", function () {
        var fakeModel2 = angular.copy(fakeModel);
        fakeModel2.name = "fake model 2";

        var models = [fakeModel, fakeModel2];
        ctrl.policy.transformations = models;
        accordionStatus[2] = true;

        scope.$digest();

        expect(scope.$watchCollection).toHaveBeenCalled();
        expect(modelFactoryMock.resetModel).toHaveBeenCalledWith(fakePolicyTemplate.model, fakeModel2.order + 1, ctrl.policy.transformations.length);
      })
    })
  });

  describe("should be able to activate the panel to create a new model", function () {
    var modelLength = null;
    beforeEach(function () {
      modelLength = ctrl.policy.transformations.length;
      ctrl.activateModelCreationPanel();
    });

    it("visibility of model creation panel is changed to true", function () {
      expect(ModelServiceMock.changeModelCreationPanelVisibility).toHaveBeenCalledWith(true);
    });

    it("Accordion status is reset to show the last position", function () {
      expect(accordionStatusServiceMock.resetAccordionStatus).toHaveBeenCalledWith(modelLength, modelLength);
    });

  });
});

