describe('policies.wizard.controller.policy-model-controller', function () {
  beforeEach(module('webApp'));
  beforeEach(module('api/policy.json'));
  beforeEach(module('model/policy.json'));
  beforeEach(module('template/policy.json'));
  beforeEach(module('model/transformation.json'));

  var ctrl, scope, fakePolicy, fakePolicyTemplate, fakeModelTemplate, fakeModel, policyModelFactoryMock,
    modelFactoryMock, modelServiceMock, resolvedPromise, rejectedPromise, fakeApiPolicy;

  // init mock modules

  beforeEach(inject(function ($controller, $q, $httpBackend, $rootScope) {
    scope = $rootScope.$new();

    inject(function (_modelPolicy_, _apiPolicy_,_templatePolicy_, _modelTransformation_) {
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

    modelServiceMock = jasmine.createSpyObj('ModelService', ['isLastModel', 'isNewModel', 'addModel', 'removeModel', 'changeModelCreationPanelVisibility']);

    modelFactoryMock = jasmine.createSpyObj('ModelFactory', ['getModel', 'getError', 'getModelInputs', 'getContext', 'setError', 'resetModel', 'updateModelInputs']);
    modelFactoryMock.getModel.and.returnValue(fakeModel);
    ctrl = $controller('PolicyModelCtrl', {
      'PolicyModelFactory': policyModelFactoryMock,
      'ModelFactory': modelFactoryMock,
      'ModelService': modelServiceMock,
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
        '$scope': scope
      });
      modelFactoryMock.getModel.and.returnValue(null);
      cleanCtrl.init();
      expect(cleanCtrl.model).toBe(null);
      expect(cleanCtrl.modelError).toBe('');
      expect(cleanCtrl.modelContext).toBe(undefined);
      expect(cleanCtrl.configPlaceholder).toBe(undefined);
      expect(cleanCtrl.outputPattern).toBe(undefined);
      expect(cleanCtrl.outputInputPlaceholder).toBe(undefined);
    }));
  });

  describe("should be able to change the default configuration when type is changed by user", function () {
    it("if type is Morphlines, it returns the morphlinesDefaultConfiguration", function () {
      ctrl.model.type = "Morphlines";
      ctrl.onChangeType();

      expect(ctrl.model.configuration).toEqual(fakeModelTemplate.Morphlines.defaultConfiguration);
    });

  });

  describe("should be able to add a model to the policy", function () {
    it("model is not added if view validations have not been passed and error is updated", function () {
      ctrl.form = {$valid: false}; //view validations have not been passed
      ctrl.addModel();

      expect(modelServiceMock.addModel).not.toHaveBeenCalled();
      expect(modelFactoryMock.setError).toHaveBeenCalledWith("_ERROR_._GENERIC_FORM_");
    });

    it("model is added if view validations have been passed and has an output field at least", function () {
      ctrl.form = {$valid: true}; //view validations have been passed

      ctrl.addModel();

      expect(modelServiceMock.addModel).toHaveBeenCalled();

      modelServiceMock.addModel.calls.reset();
      ctrl.model.outputFields = [];

      ctrl.addModel();

      expect(modelServiceMock.addModel).not.toHaveBeenCalled();
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
