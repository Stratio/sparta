describe('policies.wizard.controller.policy-cube-accordion-controller', function () {
  beforeEach(module('webApp'));
  beforeEach(module('served/policy.json'));
  beforeEach(module('served/policyTemplate.json'));
  beforeEach(module('served/cube.json'));

  var ctrl, scope, translate, fakeTranslation, fakePolicy, fakeCubeTemplate, fakeCube, policyModelFactoryMock,fakePolicyTemplate,
    accordionStatusServiceMock, cubeModelFactoryMock, cubeServiceMock, modalServiceMock, accordionStatus = null;

  // init mock modules

  beforeEach(inject(function ($controller, $q, $httpBackend, $rootScope) {
    scope = $rootScope.$new();
    fakeTranslation = "fake translation";
    translate = jasmine.createSpy().and.returnValue(fakeTranslation);

    inject(function (_servedPolicy_, _servedPolicyTemplate_, _servedCube_) {
      fakePolicy = angular.copy(_servedPolicy_);
      fakePolicyTemplate = _servedPolicyTemplate_;
      fakeCubeTemplate = _servedPolicyTemplate_.cube;
      fakeCube = _servedCube_;
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


    cubeModelFactoryMock = jasmine.createSpyObj('CubeFactory', ['resetCube', 'getCube', 'setCube', 'isValidCube', 'updateCubeInputs']);
    cubeModelFactoryMock.getCube.and.returnValue(fakeCube);

    accordionStatusServiceMock = jasmine.createSpyObj('AccordionStatusService', ['getAccordionStatus', 'resetAccordionStatus']);
    accordionStatus = [false, false];
    accordionStatusServiceMock.getAccordionStatus.and.returnValue(accordionStatus);
    cubeServiceMock = jasmine.createSpyObj('CubeService', ['findCubesUsingOutputs', 'resetCreatedCubes', 'areValidCubes', 'getCreatedCubes', 'changeCubeCreationPanelVisibility']);

    modalServiceMock = jasmine.createSpyObj('ModalService', ['openModal']);
    modalServiceMock.openModal.and.callFake(function () {
      var defer = $q.defer();
      defer.resolve();
      return {"result": defer.promise};

    });
    spyOn(scope, "$watchCollection").and.callThrough();

    ctrl = $controller('PolicyCubeAccordionCtrl  as vm', {
      'PolicyModelFactory': policyModelFactoryMock,
      'AccordionStatusService': accordionStatusServiceMock,
      'CubeModelFactory': cubeModelFactoryMock,
      'CubeService': cubeServiceMock,
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

    it ("if policy has a cube at least, next step is enabled", inject(function ($controller){
      fakePolicy.cubes = [fakeCube];
      ctrl = $controller('PolicyCubeAccordionCtrl  as vm', {
        'PolicyModelFactory': policyModelFactoryMock,
        'AccordionStatusService': accordionStatusServiceMock,
        'CubeModelFactory': cubeModelFactoryMock,
        'CubeService': cubeServiceMock,
        '$scope': scope
      });

      expect(policyModelFactoryMock.enableNextStep).toHaveBeenCalled();
    }));
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

  it("should be able to generate an index for each cube", function () {
    expect(ctrl.generateIndex()).toBe(0);
    expect(ctrl.generateIndex()).toBe(1);
    expect(ctrl.generateIndex()).toBe(2);
    expect(ctrl.generateIndex()).toBe(3);
  });

  describe("should be able to see changes in the accordion status to update the cube of the cube factory", function () {
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
        accordionStatus[1] = true;

        scope.$digest();

        expect(scope.$watchCollection).toHaveBeenCalled();
        expect(cubeModelFactoryMock.setCube).toHaveBeenCalledWith(fakeCube2, 1);
      });
      it("if position is not between 0 and policy cubes length, the factory cube is reset with the order of the previous cube", function () {
       var fakeCreatedCubes = 5;
        cubeServiceMock.getCreatedCubes.and.returnValue(fakeCreatedCubes);
        var fakeCube2 = angular.copy(fakeCube);
        fakeCube2.name = "fake cube 2";

        var cubes = [fakeCube, fakeCube2];
        ctrl.policy.cubes = cubes;
        accordionStatus[2] = true;

        scope.$digest();

        expect(scope.$watchCollection).toHaveBeenCalled();
        expect(cubeModelFactoryMock.resetCube).toHaveBeenCalledWith(fakeCubeTemplate, fakeCreatedCubes, ctrl.policy.cubes.length);
      })
    })
  });
});

