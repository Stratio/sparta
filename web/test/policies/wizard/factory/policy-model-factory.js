describe('policies.wizard.factory.policy-model-factory', function () {
  beforeEach(module('webApp'));
  beforeEach(module('api/policy.json'));
  beforeEach(module('model/policy.json'));
  beforeEach(module('model/input.json'));
  beforeEach(module('model/output.json'));
  beforeEach(module('template/policy.json'));

  var factory, fakePolicy, fakeApiPolicy, fakeInput, fakeOutput, fakePolicyTemplate, fragmentConstantsMock = null;

  beforeEach(module(function ($provide) {
    fragmentConstantsMock = jasmine.createSpyObj('fragmentConstants', ['OUTPUT']);

    // inject mocks
    $provide.value('fragmentConstants', fragmentConstantsMock);
  }));

  beforeEach(inject(function (_PolicyModelFactory_, _apiPolicy_, _modelPolicy_, _templatePolicy_, _modelInput_, _modelOutput_) {
    factory = _PolicyModelFactory_;
    fakePolicy = angular.copy(_modelPolicy_);
    fakeApiPolicy = angular.copy(_apiPolicy_);
    fakePolicy.rawData = {enabled: 'false'};
    fakePolicyTemplate = _templatePolicy_;
    fakeInput = angular.copy(_modelInput_);
    fakeOutput = _modelOutput_;
  }));

  it("should be able to load a policy from a json", function () {
    fakeApiPolicy.fragments = [fakeInput];
    fakePolicy.rawData = {enabled: 'false'};
    factory.setPolicy(fakeApiPolicy);

    var policy = factory.getCurrentPolicy();

    expect(policy.id).toBe(fakeApiPolicy.id);
    expect(policy.name).toBe(fakeApiPolicy.name);
    expect(policy.description).toBe(fakeApiPolicy.description);
    expect(policy.cubes).toEqual(fakeApiPolicy.cubes);
    expect(policy.input).toEqual(fakeInput);
  });

  describe("should return its current policy", function () {
    var cleanFactory = null;
    beforeEach(inject(function (_PolicyModelFactory_) {
      cleanFactory = _PolicyModelFactory_; // inject a new factory in each test to can check the initial state of the factory when it is created
      cleanFactory.setTemplate(fakePolicyTemplate);
    }));

    it("if there is not any policy, it initializes a new one with empty attributes and removing attributes loaded from template", function () {
      var policy = cleanFactory.getCurrentPolicy();

      expect(policy.name).toBe("");
      expect(policy.description).toBe("");
      expect(policy.rawDataEnabled).toBe(undefined);
      expect(policy.rawDataPath).toBe(undefined);
      expect(policy.checkpointPath).toBe(undefined);
      expect(policy.autoDeleteCheckpoint).toBe(undefined);
      expect(policy.input).toEqual({});
      expect(policy.outputs).toEqual([]);
      expect(policy.transformations).toEqual([]);
      expect(policy.cubes).toEqual([]);
    });

    it("if there is a policy, returns that policy", function () {
      fakeApiPolicy.fragments = [fakeInput];
      fakeApiPolicy.rawData = {enabled: 'false'};
      cleanFactory.setPolicy(fakeApiPolicy);
      var policy = cleanFactory.getCurrentPolicy();
      var sparkStreamingWindowTime = fakeApiPolicy.sparkStreamingWindow.split(/([0-9]+)/);
      expect(policy.name).toBe(fakeApiPolicy.name);
      expect(policy.description).toBe(fakeApiPolicy.description);
      expect(policy.sparkStreamingWindowNumber).toBe(Number(sparkStreamingWindowTime[1]));
      expect(policy.sparkStreamingWindowTime).toBe(sparkStreamingWindowTime[2]);
      expect(policy.storageLevel).toBe(fakeApiPolicy.storageLevel);
      expect(policy.input).toEqual(fakeInput);
      expect(policy.transformations).toEqual(fakeApiPolicy.transformations);
      expect(policy.cubes).toEqual(fakeApiPolicy.cubes);
    });
  });

  it("should be able to reset its policy to set all attributes with default values", function () {
    factory.setTemplate(fakePolicyTemplate);

    factory.setPolicy(fakeApiPolicy);
    factory.resetPolicy();

    var policy = factory.getCurrentPolicy();
    expect(policy.id).toBeUndefined();
    expect(policy.name).toBe("");
    expect(policy.description).toBe("");
    expect(policy.rawDataEnabled).toBe(undefined);
    expect(policy.rawDataPath).toBe(undefined);
    expect(policy.checkpointPath).toBe(undefined);
    expect(policy.autoDeleteCheckpoint).toBe(undefined);
    expect(policy.input).toEqual({});
    expect(policy.outputs).toEqual([]);
    expect(policy.transformations).toEqual([]);
    expect(policy.cubes).toEqual([]);
  });

  it("should be able to return the template that is being used to set the default values", function () {
    factory.setTemplate(fakePolicyTemplate);
    expect(factory.getTemplate()).toBe(fakePolicyTemplate);
  });

  describe("should be able to return an array with all outputs of the policy models", function () {
    it("if policy has model, return all outputs of these models", function () {
      var fakeModel1 = {"outputFields": [{name: "fake output1 of model 1"}, {name: "fake output2 of model 1"}]};
      var fakeModel2 = {"outputFields": [{name: "fake output1 of model 2"}, {name: "fake output2 of model 2"}]};
      var fakeModel3 = {"outputFields": [{name: "fake output1 of model 3"}, {name: "fake output2 of model 3"}]};

      fakeApiPolicy.transformations = [fakeModel1, fakeModel2, fakeModel3];
      factory.setPolicy(fakeApiPolicy);

      var modelOutputs = factory.getAllModelOutputs();

      expect(modelOutputs.length).toBe(fakeModel1.outputFields.length + fakeModel2.outputFields.length + fakeModel3.outputFields.length);
      expect(modelOutputs).toEqual([fakeModel1.outputFields[0].name, fakeModel1.outputFields[1].name, fakeModel2.outputFields[0].name, fakeModel2.outputFields[1].name, fakeModel3.outputFields[0].name, fakeModel3.outputFields[1].name]);
    });
  });

  it("should be able to update and return the final json of the policy", function () {
    var fakeFinalJSON = {"any": "fake policy json attribute"};
    factory.setFinalJSON(fakeFinalJSON);

    expect(factory.getFinalJSON()).toEqual(fakeFinalJSON);
  });


  describe("should be able to valid the Spark streaming window", function () {


    it("if policy has stream triggers, all their overLast have to be multiple of the Spark streaming window", function () {
      var fakeSparkStreamingWindowNumber = 4;
      fakeApiPolicy.streamTriggers = [{overLast: 2 * fakeSparkStreamingWindowNumber + 1 + 's', computeEvery: 2 * fakeSparkStreamingWindowNumber + 's'}];
      fakeApiPolicy.sparkStreamingWindow = fakeSparkStreamingWindowNumber + 's';

      factory.setPolicy(fakeApiPolicy);

      expect(factory.isValidSparkStreamingWindow()).toBeFalsy();

      fakeApiPolicy.streamTriggers = [{overLast: 2 * fakeSparkStreamingWindowNumber+ 's', computeEvery: 2 * fakeSparkStreamingWindowNumber + 's'} ];
      factory.setPolicy(fakeApiPolicy);

      expect(factory.isValidSparkStreamingWindow()).toBeTruthy();
    });

    it("if policy has stream triggers, all their computeEvery have to be multiple of the Spark streaming window", function () {
      var fakeSparkStreamingWindowNumber = 4;
      fakeApiPolicy.streamTriggers = [{overLast: 2 * fakeSparkStreamingWindowNumber + 's', computeEvery: 2 * fakeSparkStreamingWindowNumber + 1 + 's'}];
      fakeApiPolicy.sparkStreamingWindow = fakeSparkStreamingWindowNumber + 's';

      factory.setPolicy(fakeApiPolicy);

      expect(factory.isValidSparkStreamingWindow()).toBeFalsy();

      fakeApiPolicy.streamTriggers = [{overLast: 2 * fakeSparkStreamingWindowNumber + 's', computeEvery: 2 * fakeSparkStreamingWindowNumber+ 's'} ];
      factory.setPolicy(fakeApiPolicy);

      expect(factory.isValidSparkStreamingWindow()).toBeTruthy();
    });

    it("if policy hasn't got triggers, spark stream window is valid", function () {
      fakeApiPolicy.streamTriggers = [];
      fakeApiPolicy.sparkStreamingWindowNumber = 8;

      factory.setPolicy(fakeApiPolicy);

      expect(factory.isValidSparkStreamingWindow()).toBeTruthy();

      fakeApiPolicy.sparkStreamingWindowNumber = 13;

      factory.setPolicy(fakeApiPolicy);

      expect(factory.isValidSparkStreamingWindow()).toBeTruthy();
    });
  });
})
;
