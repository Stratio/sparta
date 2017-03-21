describe('policies.wizard.controller.policy-cube-controller', function () {
  beforeEach(module('webApp'));
  beforeEach(module('model/policy.json'));
  beforeEach(module('template/policy.json'));
  beforeEach(module('model/cube.json'));

  var ctrl, scope, q, fakePolicy, fakeCubeTemplate, fakeCube, policyModelFactoryMock, fakeOutputs, fakePolicyTemplate,
    cubeModelFactoryMock, cubeServiceMock, modalServiceMock, resolvedPromise, triggerModelFactoryMock,
    fakeDimension,fakeOperator, triggerServiceMock, rejectedPromise;

  // init mock modules

  beforeEach(inject(function ($controller, $q, $httpBackend, $rootScope) {
    scope = $rootScope.$new();
    q = $q;
    $httpBackend.when('GET', 'languages/en-US.json').respond({});
    $httpBackend.when('GET', '/fragment/output').respond({});


    inject(function (_modelPolicy_, _templatePolicy_, _modelCube_) {
      fakePolicy = angular.copy(_modelPolicy_);
      fakePolicyTemplate = _templatePolicy_;
      fakeCubeTemplate = _templatePolicy_.cube;
      fakeCube = angular.copy(_modelCube_);
    });

    policyModelFactoryMock = jasmine.createSpyObj('PolicyModelFactory', ['getCurrentPolicy', 'getTemplate', 'getAllModelOutputs']);
    policyModelFactoryMock.getCurrentPolicy.and.callFake(function () {
      return fakePolicy;
    });

    policyModelFactoryMock.getTemplate.and.callFake(function () {
      return fakePolicyTemplate;
    });

    resolvedPromise = function () {
      var defer = $q.defer();
      defer.resolve();

      return defer.promise;
    };

    spyOn(document, "querySelector").and.callFake(function () {
      return {"focus": jasmine.createSpy()}
    });

    cubeServiceMock = jasmine.createSpyObj('CubeService', ['isLastCube', 'isNewCube', 'addCube', 'removeCube', 'changeCubeCreationPanelVisibility', 'generateOutputList']);
    cubeServiceMock.generateOutputList.and.callFake(resolvedPromise);

    modalServiceMock = jasmine.createSpyObj('ModalService', ['openModal']);

    cubeModelFactoryMock = jasmine.createSpyObj('CubeFactory', ['getCube', 'getError', 'getCubeInputs', 'getContext', 'setError', 'resetCube', 'updateCubeInputs']);
    cubeModelFactoryMock.getCube.and.returnValue(fakeCube);

    fakeOutputs = ["output1", "output2", "output3", "output4", "output5"];
    policyModelFactoryMock.getAllModelOutputs.and.returnValue(fakeOutputs);

    triggerModelFactoryMock = jasmine.createSpyObj('TriggerFactory', ['getTrigger', 'getError', 'getTriggerInputs', 'getContext', 'setError', 'resetTrigger', 'updateTriggerInputs', 'setError']);

    triggerServiceMock = jasmine.createSpyObj('TriggerService', ['isLastTrigger', 'setTriggerContainer', 'isNewTrigger', 'addTrigger', 'changeVisibilityOfHelpForSql', 'disableTriggerCreationPanel', 'generateOutputList', 'getSqlSourceItems']);
    triggerServiceMock.generateOutputList.and.callFake(resolvedPromise);

    modalServiceMock.openModal.and.callFake(function () {
      var defer = $q.defer();
      defer.resolve();
      return {"result": defer.promise};
    });

    ctrl = $controller('CubeCtrl', {
      'PolicyModelFactory': policyModelFactoryMock,
      'CubeModelFactory': cubeModelFactoryMock,
      'CubeService': cubeServiceMock,
      'ModalService': modalServiceMock,
      'TriggerModelFactory': triggerModelFactoryMock,
      'TriggerService': triggerServiceMock,
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
    };

    fakeDimension = {name: "fake dimension", type: "DateTime", precision: "1m" };
    fakeOperator = {type: "Accumulator", name: "fake operator name", configuration:{}};
  }));

  describe("when it is initialized", function () {
    describe("if factory cube is not null", function () {

      it('it should get a policy template from from policy factory', function () {
        expect(ctrl.template).toBe(fakeCubeTemplate);
      });

      it('it should get the policy that is being created or edited from policy factory', function () {
        expect(ctrl.policy).toBe(fakePolicy);
      });

      it("it should load the cube from the cube factory", function () {
        expect(ctrl.cube).toBe(fakeCube);
      });

      it("it should load an output list with all outputs of the models", function () {
        expect(ctrl.outputList).toBe(fakeOutputs);
      });

      it("trigger accordion is initialized", function () {
        expect(triggerServiceMock.disableTriggerCreationPanel).toHaveBeenCalled();
        expect(triggerServiceMock.setTriggerContainer).toHaveBeenCalledWith(fakeCube.triggers, "cube");
        expect(ctrl.triggerContainer).toBe(fakeCube.triggers);
      })
    });

    it("if factory cube is null, no changes are executed", inject(function ($controller) {
      cubeModelFactoryMock.getCube.and.returnValue(null);
      var cleanCtrl = $controller('CubeCtrl', {
        'PolicyModelFactory': policyModelFactoryMock,
        'CubeModelFactory': cubeModelFactoryMock,
        'CubeService': cubeServiceMock,
        'ModalService': modalServiceMock,
        '$scope': scope
      });
      expect(cleanCtrl.template).toBe(undefined);
      expect(cleanCtrl.policy).toBe(undefined);
      expect(cleanCtrl.granularityOptions).toBe(undefined);
      expect(cleanCtrl.cubeError).toBe(undefined);
      expect(cleanCtrl.cubeContext).toBe(undefined);
      expect(cleanCtrl.functionList).toBe(undefined);
      expect(cleanCtrl.outputList).toBe(undefined);
    }));
  });

  describe("should be able to open a modal in order to convert the introduced output to dimension", function () {
    var fakeOutputName = "fake output name";

    it("modal is opened with the correct params", function () {
      ctrl.addOutputToDimensions(fakeOutputName);

      expect(modalServiceMock.openModal.calls.mostRecent().args[0]).toBe("NewDimensionModalCtrl");
      expect(modalServiceMock.openModal.calls.mostRecent().args[1]).toBe("templates/policies/dimension-modal.tpl.html");
      var resolve = (modalServiceMock.openModal.calls.mostRecent().args[2]);
      expect(resolve.fieldName()).toBe(fakeOutputName);
      expect(resolve.dimensionName()).toBe(fakeOutputName);
      expect(resolve.dimensions()).toBe(ctrl.cube.dimensions);
      expect(resolve.template()).toBe(fakeCubeTemplate);
    });

    it("when modal is closed, the created dimension is added to cube", function () {

      modalServiceMock.openModal.and.callFake(function () {
        var defer = q.defer();
        defer.resolve({dimension: fakeDimension, isTimeDimension: true});

        return  {"result": defer.promise};
      });
      var currentDimensionLength = ctrl.cube.dimensions.length;
      ctrl.addOutputToDimensions(fakeOutputName).then(function () {
        expect(ctrl.cube.dimensions.length).toBe(currentDimensionLength + 1);
      });
      scope.$digest();
    });

  });

  describe("should be able to open a modal in order to confirm if user want to remove a specific dimension from the cube", function () {

    it("modal is opened with the correct params", function () {
      var fakeDimensionIndex = 2;
      ctrl.removeOutputFromDimensions(fakeDimensionIndex);

      expect(modalServiceMock.openModal.calls.mostRecent().args[0]).toBe("ConfirmModalCtrl");
      expect(modalServiceMock.openModal.calls.mostRecent().args[1]).toBe("templates/modal/confirm-modal.tpl.html");
      var resolve = (modalServiceMock.openModal.calls.mostRecent().args[2]);
      expect(resolve.title()).toBe("_POLICY_._CUBE_._REMOVE_DIMENSION_CONFIRM_TITLE_");
      expect(resolve.message()).toBe("");
    });

    it("when modal is closed, the dimension from the specified position is removed from the cube", function () {
      var fakeDimensionIndex = 2;
      var fakeDimensions = [{"name": "fake dimension 1"}, {"name": "fake dimension 2"}, {"name": "fake dimension 3"}, {"name": "fake dimension 4"}];
      ctrl.cube.dimensions = angular.copy(fakeDimensions);
      ctrl.removeOutputFromDimensions(fakeDimensionIndex).then(function () {
        expect(ctrl.cube.dimensions.length).toBe(fakeDimensions.length - 1);
        expect(ctrl.cube.dimensions[0]).toEqual(fakeDimensions[0]);
        expect(ctrl.cube.dimensions[1]).toEqual(fakeDimensions[1]);
        expect(ctrl.cube.dimensions[2]).toEqual(fakeDimensions[3]);
      });
      scope.$digest();
    });

  });

  describe("should be able to open a modal in order to convert a function to operator", function () {
    var fakeFunctionName = "fake function name";

    it("modal is opened with the correct params", function () {

      ctrl.addFunctionToOperators(fakeFunctionName);

      expect(modalServiceMock.openModal.calls.mostRecent().args[0]).toBe("NewOperatorModalCtrl");
      expect(modalServiceMock.openModal.calls.mostRecent().args[1]).toBe("templates/policies/operator-modal.tpl.html");
      var resolve = (modalServiceMock.openModal.calls.mostRecent().args[2]);
      expect(resolve.operatorType()).toBe(fakeFunctionName);
      expect(resolve.operatorName()).toBe(fakeFunctionName.toLowerCase());
      expect(resolve.operators()).toBe(ctrl.cube.operators);
      expect(resolve.template()).toBe(fakeCubeTemplate);
    });

    it("when modal is closed, the created operator is added to cube", function () {
      var currentOperatorLength = ctrl.cube.operators.length;
      ctrl.addFunctionToOperators(fakeFunctionName).then(function () {
        expect(ctrl.cube.operators.length).toBe(currentOperatorLength + 1);
      });
      scope.$digest();
    });
  });

  describe("should be able to open a modal in order to confirm if user want to remove a specific operator from the cube", function () {

    it("modal is opened with the correct params", function () {
      var fakeDimensionIndex = 2;
      ctrl.removeFunctionFromOperators(fakeDimensionIndex);

      expect(modalServiceMock.openModal.calls.mostRecent().args[0]).toBe("ConfirmModalCtrl");
      expect(modalServiceMock.openModal.calls.mostRecent().args[1]).toBe("templates/modal/confirm-modal.tpl.html");
      var resolve = (modalServiceMock.openModal.calls.mostRecent().args[2]);
      expect(resolve.title()).toBe("_POLICY_._CUBE_._REMOVE_OPERATOR_CONFIRM_TITLE_");
      expect(resolve.message()).toBe("");
    });

    it("when modal is closed, the operator from the specified position is removed from the cube", function () {
      var fakeOperatorIndex = 2;
      var fakeOperators = [{"name": "fake operator 1"}, {"name": "fake operator 2"}, {"name": "fake operator 3"}, {"name": "fake operator 4"}];
      ctrl.cube.operators = angular.copy(fakeOperators);
      ctrl.removeFunctionFromOperators(fakeOperatorIndex).then(function () {
        expect(ctrl.cube.operators.length).toBe(fakeOperators.length - 1);
        expect(ctrl.cube.operators[0]).toEqual(fakeOperators[0]);
        expect(ctrl.cube.operators[1]).toEqual(fakeOperators[1]);
        expect(ctrl.cube.operators[2]).toEqual(fakeOperators[3]);
      });
      scope.$digest();
    });
  });

  it("should be able to remove the factory cube from the policy calling to the cube service", function () {
    ctrl.removeCube();

    expect(cubeServiceMock.removeCube).toHaveBeenCalled();
  });
});
