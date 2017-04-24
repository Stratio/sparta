describe('policies.wizard.controller.policy-model-controller', function () {
  beforeEach(module('webApp'));
  beforeEach(module('api/policy.json'));
  beforeEach(module('model/policy.json'));
  beforeEach(module('template/policy.json'));
  beforeEach(module('model/transformation.json'));

  var ctrl, scope, fakePolicy, fakePolicyTemplate, fakeModelTemplate, fakeModel, policyModelFactoryMock, fakeContext,
    modelFactoryMock, modelServiceMock, resolvedPromise, rejectedPromise, fakeApiPolicy, modelConstants, utilsServiceMock;

  // init mock modules

  beforeEach(inject(function ($controller, $q, $httpBackend, $rootScope, _modelConstants_) {
    scope = $rootScope.$new();
    modelConstants = _modelConstants_;
    inject(function (_modelPolicy_, _apiPolicy_, _templatePolicy_, _modelTransformation_) {
      fakePolicy = angular.copy(_modelPolicy_);
      fakeApiPolicy = angular.copy(_apiPolicy_);
      fakePolicyTemplate = _templatePolicy_;
      fakeModelTemplate = fakePolicyTemplate.model;
      fakeModel = angular.copy(_modelTransformation_);
    });

    $httpBackend.when('GET', 'languages/en-US.json')
      .respond({});

    policyModelFactoryMock = jasmine.createSpyObj('PolicyModelFactory', ['getCurrentPolicy', 'getTemplate']);
    policyModelFactoryMock.getCurrentPolicy.and.callFake(function () {
      return fakePolicy;
    });

    policyModelFactoryMock.getTemplate.and.callFake(function () {
      return fakePolicyTemplate;
    });

    modelServiceMock = jasmine.createSpyObj('ModelService', ['isLastModel', 'isNewModel', 'addModel', 'removeModel', 'changeModelCreationPanelVisibility', 'isOutputUsed', 'updateModel']);
    fakeContext = {
      position: 2
    };
    modelFactoryMock = jasmine.createSpyObj('ModelFactory', ['getModel', 'getError', 'getModelInputs', 'getContext', 'setError', 'resetModel', 'updateModelInputs', 'getPreviousOutputFields', 'getOutputFields']);
    modelFactoryMock.getModel.and.returnValue(fakeModel);
    modelFactoryMock.getContext.and.returnValue(fakeContext);
    utilsServiceMock = jasmine.createSpyObj('UtilsService', ['findElementInJSONArray']);

    ctrl = $controller('PolicyModelCtrl as vm', {
      'PolicyModelFactory': policyModelFactoryMock,
      'ModelFactory': modelFactoryMock,
      'ModelService': modelServiceMock,
      'UtilsService': utilsServiceMock,
      '$scope': scope
    });

    resolvedPromise = function () {
      var defer = $q.defer();
      defer.resolve();

      return defer.promise;
    };

    rejectedPromise = function () {
      var defer = $q.defer();
      defer.reject();

      return defer.promise;
    }
  }));

  describe("when it is initialized", function () {

    it('it should get a policy template from from policy factory', function () {
      expect(ctrl.template).toBe(fakePolicyTemplate);
    });

    it('it should get the policy that is being created or edited from policy factory', function () {
      expect(ctrl.policy).toBe(fakePolicy);
    });

    describe("if factory model is not null", function () {

      it("it should load the model from the model factory", function () {
        expect(ctrl.model).toBe(fakeModel);
      });
    });

    it("if factory model is null, no changes are executed", inject(function ($controller) {
      var cleanCtrl = $controller('PolicyModelCtrl', {
        'PolicyModelFactory': policyModelFactoryMock,
        'ModelFactory': modelFactoryMock,
        'ModelService': modelServiceMock,
        'UtilsService': utilsServiceMock,
        '$scope': scope
      });
      modelFactoryMock.getModel.and.returnValue(null);
      cleanCtrl.init();
      expect(cleanCtrl.model).toBe(null);
      expect(cleanCtrl.modelError).toBe('');
      expect(cleanCtrl.modelContext).toBe(fakeContext);
      expect(cleanCtrl.configPlaceholder).toBe(undefined);
      expect(cleanCtrl.outputPattern).toBe(undefined);
      expect(cleanCtrl.outputInputPlaceholder).toBe(undefined);
    }));
  });

  describe("should not be able to change type", function(){
    it("if an output is being used in policy", function(){
      ctrl.model.type = "Morphlines";
      spyOn(ctrl,'validateChange').and.returnValue(true);
      ctrl.onChangeType();
      expect(ctrl.model.configuration).not.toEqual(fakeModelTemplate.Morphlines.defaultConfiguration);
    });
  });

  describe("should be able to change the form settings when type is changed by user", function () {
    it("configuration and outputs list are reset", function () {
      ctrl.model.outputFields = [{
        name: "fake output",
        type: "String"
      }];
      ctrl.configuration = {
        "name": "fake parser"
      };

      ctrl.onChangeType();

      expect(ctrl.model.outputFields).toEqual([]);
      expect(ctrl.model.configuration).toEqual({});
    });

    it("if type is Morphlines, it puts a default morphline configuration", function () {
      ctrl.model.type = "Morphlines";
      ctrl.onChangeType();

      expect(ctrl.model.configuration).toEqual(fakeModelTemplate.Morphlines.defaultConfiguration);
    });

    it("if type is Geo, it puts a default geo configuration and its available output types and input field is removed", function () {
      ctrl.model.type = "Geo";
      ctrl.model.inputField = "fake input field";

      ctrl.onChangeType();

      expect(ctrl.outputFieldTypes).toEqual(fakeModelTemplate.Geo.outputFieldTypes);
      expect(ctrl.model.inputField).toBeUndefined();

    });

    it("if type is DateTime, it puts default output types", function () {
      ctrl.model.type = "DateTime";
      ctrl.onChangeType();

      expect(ctrl.outputFieldTypes).toEqual(fakeModelTemplate.defaultOutputFieldTypes)
    });
  });

  it("when type is changed once model has been initialized, output list is updated according to the new type", function () {
    modelFactoryMock.getModel.and.returnValue(fakeModel);

    scope.$apply();

    fakeModel.type = "DateTime";

    scope.$apply();

    expect(ctrl.outputFieldTypes).toEqual(fakeModelTemplate[modelConstants.DATETIME].outputFieldTypes);

    fakeModel.type = "Morphlines";

    scope.$apply();

    expect(ctrl.outputFieldTypes).toEqual(fakeModelTemplate.defaultOutputFieldTypes);

    fakeModel.type = "Geo";

    scope.$apply();

    expect(ctrl.outputFieldTypes).toEqual(fakeModelTemplate[modelConstants.GEO].outputFieldTypes);

    fakeModel.type = "other";

    scope.$apply();

    expect(ctrl.outputFieldTypes).toEqual(fakeModelTemplate.defaultOutputFieldTypes);
  });

  describe("should be able to add a model to the policy", function () {
    it("model is not added if view validations have not been passed and error is updated", function () {
      ctrl.form = {
        $valid: false
      }; //view validations have not been passed
      ctrl.addModel();

      expect(modelServiceMock.addModel).not.toHaveBeenCalled();
      expect(modelFactoryMock.setError).toHaveBeenCalledWith("_ERROR_._GENERIC_FORM_");
    });

    it("model is updated if it is not in creation mode", function () {
      ctrl.form = {
        $valid: true
      }; //view validations have not been passed
      utilsServiceMock.findElementInJSONArray.and.returnValue(-1); // there isn't any output field with the same name
      ctrl.addModel();

      expect(modelServiceMock.updateModel).toHaveBeenCalled();
      expect(modelServiceMock.addModel).not.toHaveBeenCalled();
    });

    describe("model is added if view validations have been passed and has an output field (not duplicated) at least", function () {
      beforeEach(function () {
        modelFactoryMock.getPreviousOutputFields.and.returnValue([{
          name: "different output field",
          value: "different"
        }]);
        utilsServiceMock.findElementInJSONArray.and.returnValue(5); // there is an output field with the same name
        ctrl.form = {
          $valid: true
        }; //view validations have been passed
      });

      it('if it is not a dateTime transformation', function () {
        ctrl.model.type = modelConstants.MORPHLINES;

        ctrl.addModel(true);

        expect(modelServiceMock.addModel).not.toHaveBeenCalled();
        utilsServiceMock.findElementInJSONArray.and.returnValue(-1); // there isn't any output field with the same name
        ctrl.addModel(true);

        expect(modelServiceMock.addModel).toHaveBeenCalled();
        modelServiceMock.addModel.calls.reset();
        ctrl.model.outputFields = [];

        ctrl.addModel(true);

        expect(modelServiceMock.addModel).not.toHaveBeenCalled();
      });

      it('if it is a dateTime transformation, granularityNumber and granularityTime are removed and granularity attribute is generated using both them', function () {
        ctrl.model.type = modelConstants.DATETIME;
        utilsServiceMock.findElementInJSONArray.and.returnValue(-1); // there isn't any output field with the same name
        var fakeGranularityNumber = "5";
        var fakeGranularityTime = "minutes";
        ctrl.model.configuration = {
          granularityNumber: fakeGranularityNumber,
          granularityTime: fakeGranularityTime
        };

        ctrl.addModel();

        expect(ctrl.model.configuration.granularity).toBe(fakeGranularityNumber + fakeGranularityTime);
      });

    });
  });

  describe("should be able to remove the factory model from the policy", function () {
    afterEach(function () {
      scope.$digest();
    });

    it("if model service removes successfully the model, current model is reset with order equal to the last model more one and position equal to the model list length", function () {
      modelServiceMock.removeModel.and.callFake(resolvedPromise);
      var lastModelOrder = 1;
      var fakeModel2 = angular.copy(fakeModel);
      fakeModel2.order = lastModelOrder;
      var models = [fakeModel, fakeModel2];
      ctrl.policy.transformations = models;

      ctrl.removeModel().then(function () {
        expect(modelFactoryMock.resetModel).toHaveBeenCalledWith(fakeModelTemplate, lastModelOrder + 1, models.length);
        expect(modelFactoryMock.updateModelInputs).toHaveBeenCalledWith(models);
      });
    });

    it("if model service is not able to remove the model, controller do not do anything", function () {
      modelServiceMock.removeModel.and.callFake(rejectedPromise);

      ctrl.removeModel().then(function () {
        expect(modelFactoryMock.resetModel).not.toHaveBeenCalled();
      });
    });
  });
});
