describe('policies.wizard.factory.policy-model-factory', function () {
  beforeEach(module('webApp'));
  beforeEach(module('served/policy.json'));
  beforeEach(module('served/input.json'));
  beforeEach(module('served/output.json'));
  beforeEach(module('served/policyTemplate.json'));

  var factory, fakePolicy, fakeInput, fakeOutput, fakePolicyTemplate, fragmentConstantsMock = null;

  beforeEach(module(function ($provide) {
    fragmentConstantsMock = jasmine.createSpyObj('fragmentConstants', ['OUTPUT']);

    // inject mocks
    $provide.value('fragmentConstants', fragmentConstantsMock);
  }));

  beforeEach(inject(function (_PolicyModelFactory_, _servedPolicy_, _servedPolicyTemplate_, _servedInput_, _servedOutput_) {
    factory = _PolicyModelFactory_;
    fakePolicy = angular.copy(_servedPolicy_);
    fakePolicy.rawData={enabled: 'false'};
    fakePolicyTemplate = _servedPolicyTemplate_;
    fakeInput = _servedInput_;
    fakeOutput = _servedOutput_;
  }));

  it("should be able to load a policy from a json", function () {
    fakePolicy.fragments = [fakeInput, fakeOutput];
    fakePolicy.rawData={enabled: 'false'};
    factory.setPolicy(fakePolicy);

    var policy = factory.getCurrentPolicy();

    expect(factory.getProcessStatus().currentStep).toBe(0);
    expect(policy.id).toBe(fakePolicy.id);
    expect(policy.name).toBe(fakePolicy.name);
    expect(policy.description).toBe(fakePolicy.description);
    expect(policy.cubes).toEqual(fakePolicy.cubes);
    expect(policy.input).toBe(fakeInput);
  });


  describe("should return its current policy", function () {
    var cleanFactory = null;
    beforeEach(inject(function (_PolicyModelFactory_) {
      cleanFactory = _PolicyModelFactory_; // inject a new factory in each test to can check the initial state of the factory when it is created
      cleanFactory.setTemplate(fakePolicyTemplate);
    }));

    it("if there is not any policy, it initializes a new one with empty attributes and removing attributes loaded from template", function () {
      var policy = cleanFactory.getCurrentPolicy();

      expect(cleanFactory.getProcessStatus().currentStep).toBe(-1);
      expect(policy.name).toBe("");
      expect(policy.description).toBe("");
      expect(policy.rawDataEnabled).toBe(undefined);
      expect(policy.rawDataPath).toBe(undefined);
      expect(policy.input).toEqual({});
      expect(policy.outputs).toEqual([]);
      expect(policy.transformations).toEqual([]);
      expect(policy.cubes).toEqual([]);
    });

    it("if there is a policy, returns that policy", function () {
      fakePolicy.fragments = [fakeInput, fakeOutput];
      fakePolicy.rawData={enabled: 'false'};
      cleanFactory.setPolicy(fakePolicy);
      var policy = cleanFactory.getCurrentPolicy();

      expect(policy.name).toBe(fakePolicy.name);
      expect(policy.description).toBe(fakePolicy.description);
      expect(policy.sparkStreamingWindow).toBe(fakePolicy.sparkStreamingWindow);
      expect(policy.storageLevel).toBe(fakePolicy.storageLevel);
      expect(policy.checkpointPath).toBe(fakePolicy.checkpointPath);
      expect(policy.input).toEqual(fakeInput);
      expect(policy.transformations).toEqual(fakePolicy.transformations);
      expect(policy.cubes).toEqual(fakePolicy.cubes);
    });
  });


  it("should be able to reset its policy to set all attributes with default values", function () {
    factory.setTemplate(fakePolicyTemplate);

    factory.setPolicy(fakePolicy);
    factory.resetPolicy();

    var policy = factory.getCurrentPolicy();
    expect(factory.getProcessStatus().currentStep).toBe(-1);
    expect(policy.name).toBe("");
    expect(policy.description).toBe("");
    expect(policy.rawDataEnabled).toBe(undefined);
    expect(policy.rawDataPath).toBe(undefined);
    expect(policy.input).toEqual({});
    expect(policy.outputs).toEqual([]);
    expect(policy.transformations).toEqual([]);
    expect(policy.cubes).toEqual([]);
  });

  describe("should be able to change the current step in the process of modify or create the current policy", function () {
    beforeEach(function () {
      factory.setTemplate(fakePolicyTemplate);
      factory.setPolicy(fakePolicy);
    });
    it("should be able to change to the next step", function () {
      var currentStep = factory.getProcessStatus().currentStep;
      factory.previousStep();
      expect(factory.getProcessStatus().currentStep).toBe(currentStep - 1);
    });

    it("should be able to change to the next step", function () {
      var currentStep = factory.getProcessStatus().currentStep;
      factory.nextStep();
      expect(factory.getProcessStatus().currentStep).toBe(currentStep + 1);
    });
  });

  it("should be able to return the template that is being used to set the default values", function () {
    factory.setTemplate(fakePolicyTemplate);
    expect(factory.getTemplate()).toBe(fakePolicyTemplate);
  });

  describe("should be able to return an array with all outputs of the policy models", function () {
    it("if policy has model, return all outpus of these models", function () {
      var fakeModel1 = {"outputFields": [{name:"fake output1 of model 1"}, {name:"fake output2 of model 1"}]};
      var fakeModel2 = {"outputFields": [{name:"fake output1 of model 2"},{name: "fake output2 of model 2"}]};
      var fakeModel3 = {"outputFields": [{name:"fake output1 of model 3"}, {name:"fake output2 of model 3"}]};

      var policy = angular.copy(fakePolicy);
      policy.transformations = [fakeModel1, fakeModel2, fakeModel3];
      factory.setPolicy(policy);

      var modelOutputs = factory.getAllModelOutputs();

      expect(modelOutputs.length).toBe(fakeModel1.outputFields.length + fakeModel2.outputFields.length + fakeModel3.outputFields.length);
      expect(modelOutputs).toEqual([fakeModel1.outputFields[0].name,fakeModel1.outputFields[1].name,fakeModel2.outputFields[0].name,fakeModel2.outputFields[1].name, fakeModel3.outputFields[0].name, fakeModel3.outputFields[1].name]);
    });
  });

  it("should be able to update and return the final json of the policy", function () {
    var fakeFinalJSON = {"any": "fake policy json attribute"};
    factory.setFinalJSON(fakeFinalJSON);

    expect(factory.getFinalJSON()).toEqual(fakeFinalJSON);
  });

})
;
