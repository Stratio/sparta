describe('policies.wizard.controller.policy-cube-accordion-controller', function () {
  beforeEach(module('webApp'));
  beforeEach(module('served/policy.json'));
  beforeEach(module('served/policyTemplate.json'));
  beforeEach(module('served/cube.json'));
  beforeEach(module('served/output.json'));

  var ctrl, scope, q, translate, fakeTranslation, fakePolicy, fakeCubeTemplate, fakeCube, policyModelFactoryMock, fakePolicyTemplate,
    $controller, cubeModelFactoryMock, cubeServiceMock, modalServiceMock, resolvedPromise, fakeOutput = null;

  // init mock modules

  beforeEach(inject(function (_$controller_, $q, $httpBackend, $rootScope) {
    scope = $rootScope.$new();
    q = $q;
    fakeTranslation = "fake translation";
    $controller = _$controller_;
    translate = jasmine.createSpy().and.returnValue(fakeTranslation);

    inject(function (_servedPolicy_, _servedPolicyTemplate_, _servedCube_, _servedOutput_) {
      fakePolicy = angular.copy(_servedPolicy_);
      fakePolicyTemplate = _servedPolicyTemplate_;
      fakeCubeTemplate = _servedPolicyTemplate_.cube;
      fakeCube = _servedCube_;
      fakeOutput = _servedOutput_;
    });

    $httpBackend.when('GET', 'languages/en-US.json')
      .respond({});

    resolvedPromise = function () {
      var defer = $q.defer();
      defer.resolve();

      return defer.promise;
    };
    policyModelFactoryMock = jasmine.createSpyObj('PolicyModelFactory', ['getCurrentPolicy', 'getTemplate', 'previousStep', 'nextStep', 'enableNextStep']);
    policyModelFactoryMock.getCurrentPolicy.and.callFake(function () {
      return fakePolicy;
    });

    policyModelFactoryMock.getTemplate.and.callFake(function () {
      return fakePolicyTemplate;
    });

    cubeModelFactoryMock = jasmine.createSpyObj('CubeFactory', ['resetCube', 'getCube', 'setCube', 'isValidCube', 'updateCubeInputs']);
    cubeModelFactoryMock.getCube.and.returnValue(fakeCube);

    cubeServiceMock = jasmine.createSpyObj('CubeService', ['findCubesUsingOutputs', 'resetCreatedCubes', 'areValidCubes',
      'getCreatedCubes', 'changeCubeCreationPanelVisibility', 'generateOutputList']);

    cubeServiceMock.generateOutputList.and.callFake(resolvedPromise);
    modalServiceMock = jasmine.createSpyObj('ModalService', ['openModal']);
    modalServiceMock.openModal.and.callFake(function () {
      var defer = $q.defer();
      defer.resolve();
      return {"result": defer.promise};
    });

    spyOn(scope, "$watchCollection").and.callThrough();

    ctrl = $controller('PolicyCubeAccordionCtrl  as vm', {
      'PolicyModelFactory': policyModelFactoryMock,
      'CubeModelFactory': cubeModelFactoryMock,
      'CubeService': cubeServiceMock,
      '$scope':scope
    });

  }));

  describe("when it is initialized", function () {

    it('it should get a policy template from from policy factory', function () {
      expect(ctrl.template).toBe(fakePolicyTemplate);
    });

    it('it should get the policy that is being created or edited from policy factory', function () {
      expect(ctrl.policy).toBe(fakePolicy);
    });

    it("if policy has a cube at least, next step is enabled", function () {
      fakePolicy.cubes = [fakeCube];
      ctrl = $controller('PolicyCubeAccordionCtrl  as vm', {
        'PolicyModelFactory': policyModelFactoryMock,
        'CubeModelFactory': cubeModelFactoryMock,
        'CubeService': cubeServiceMock,
        '$scope':scope
      });

      expect(policyModelFactoryMock.enableNextStep).toHaveBeenCalled();
    });

    it("if policy has not any cube, panel of cube creation is shown", function () {
      fakePolicy.cubes = [];
      ctrl = $controller('PolicyCubeAccordionCtrl  as vm', {
        'PolicyModelFactory': policyModelFactoryMock,
        'CubeModelFactory': cubeModelFactoryMock,
        'CubeService': cubeServiceMock,
        '$scope':scope
      });

      expect(cubeServiceMock.changeCubeCreationPanelVisibility).toHaveBeenCalled();
    });
  });

  it("should be able to change to previous step calling to policy cube factory", function () {
    ctrl.previousStep();

    expect(policyModelFactoryMock.previousStep).toHaveBeenCalled();
  });

  describe("should be able to change to next step calling to policy cube factory", function () {
    it("if there is not any cube added to policy, step is not changed", function () {
      ctrl.policy.cubes = [];
      ctrl.nextStep();

      expect(policyModelFactoryMock.nextStep).not.toHaveBeenCalled();
    });

    it("if there is a cube not valid, step is not changed", function () {
      ctrl.policy.cubes = [fakeCube, fakeCube];
      cubeServiceMock.areValidCubes.and.returnValue(false);
      ctrl.nextStep();

      expect(policyModelFactoryMock.nextStep).not.toHaveBeenCalled();
    });

    it("if policy have a cube at least and all them are valid, step is changed", function () {
      ctrl.policy.cubes = [fakeCube, fakeCube];
      cubeServiceMock.areValidCubes.and.returnValue(true);
      ctrl.nextStep();

      expect(policyModelFactoryMock.nextStep).toHaveBeenCalled();
    })

  });

  describe("should be able to act accordingly the accordion status to update the cube of the cube factory", function () {
    describe("if the new value of the accordion status is not null should find the cube that has been opened by user, and send it to the cube factory ", function () {
      var cubes, fakeCube2 = null;
      beforeEach(function () {
        fakeCube2 = angular.copy(fakeCube);
        fakeCube2.name = "fake cube 2";
        fakeCube2.order = 1;
        cubes = [fakeCube, fakeCube2];
      });
      it("if position is between 0 and policy cubes length, the factory cube is updated with the cube of that position in the policy cube array", function () {
        ctrl.policy.cubes = cubes;
        var position = 1;
        ctrl.cubeAccordionStatus[position] = true;

        ctrl.changeOpenedElement(position);

        expect(cubeModelFactoryMock.setCube).toHaveBeenCalledWith(fakeCube2, 1);
      });
      it("if position is not between 0 and policy cubes length, the factory cube is reset with the order of the previous cube", function () {
        var fakeCreatedCubes = 5;
        var position = 2;
        cubeServiceMock.getCreatedCubes.and.returnValue(fakeCreatedCubes);
        var fakeCube2 = angular.copy(fakeCube);
        fakeCube2.name = "fake cube 2";

        var cubes = [fakeCube, fakeCube2];
        ctrl.policy.cubes = cubes;
        ctrl.cubeAccordionStatus[position] = true;

        ctrl.changeOpenedElement(position);

        expect(cubeModelFactoryMock.resetCube).toHaveBeenCalledWith(fakeCubeTemplate, fakeCreatedCubes, ctrl.policy.cubes.length);
      })
    })
  });
});

