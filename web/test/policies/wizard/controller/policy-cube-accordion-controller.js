describe('policies.wizard.controller.policy-cube-accordion-controller', function () {
  beforeEach(module('webApp'));
  beforeEach(module('model/policy.json'));
  beforeEach(module('template/policy.json'));
  beforeEach(module('model/cube.json'));

  var ctrl, scope, q, translate, fakeTranslation, fakePolicy, fakeCubeTemplate, fakeCube, policyModelFactoryMock, fakePolicyTemplate,
    $controller, cubeModelFactoryMock, cubeServiceMock, modalServiceMock, resolvedPromise, wizardStatusServiceMock = null;

  // init mock modules

  beforeEach(inject(function (_$controller_, $q, $httpBackend, $rootScope) {
    scope = $rootScope.$new();
    q = $q;
    fakeTranslation = "fake translation";
    $controller = _$controller_;
    translate = jasmine.createSpy().and.returnValue(fakeTranslation);

    inject(function (_modelPolicy_, _templatePolicy_, _modelCube_) {
      fakePolicy = angular.copy(_modelPolicy_);
      fakePolicyTemplate = _templatePolicy_;
      fakeCubeTemplate = _templatePolicy_.cube;
      fakeCube = _modelCube_;
    });

    $httpBackend.when('GET', 'languages/en-US.json')
      .respond({});

    resolvedPromise = function () {
      var defer = $q.defer();
      defer.resolve();

      return defer.promise;
    };
    wizardStatusServiceMock = jasmine.createSpyObj('WizardStatusService', ['enableNextStep', 'disableNextStep']);
    policyModelFactoryMock = jasmine.createSpyObj('PolicyModelFactory', ['getCurrentPolicy', 'getTemplate', 'previousStep', 'nextStep', 'enableNextStep', 'setError']);
    policyModelFactoryMock.getCurrentPolicy.and.callFake(function () {
      return fakePolicy;
    });

    policyModelFactoryMock.getTemplate.and.callFake(function () {
      return fakePolicyTemplate;
    });

    cubeModelFactoryMock = jasmine.createSpyObj('CubeFactory', ['resetCube', 'getCube', 'setCube', 'isValidCube', 'updateCubeInputs']);
    cubeModelFactoryMock.getCube.and.returnValue(fakeCube);

    cubeServiceMock = jasmine.createSpyObj('CubeService', ['findCubesUsingOutputs', 'resetCreatedCubes', 'areValidCubes','isActiveCubeCreationPanel',
      'getCreatedCubes', 'changeCubeCreationPanelVisibility', 'generateOutputList', 'getCubeCreationStatus', 'activateCubeCreationPanel']);

    cubeServiceMock.generateOutputList.and.callFake(resolvedPromise);
    modalServiceMock = jasmine.createSpyObj('ModalService', ['openModal']);
    modalServiceMock.openModal.and.callFake(function () {
      var defer = $q.defer();
      defer.resolve();
      return {"result": defer.promise};
    });

    spyOn(scope, "$watchCollection").and.callThrough();

    ctrl = $controller('PolicyCubeAccordionCtrl  as vm', {
      'WizardStatusService': wizardStatusServiceMock,
      'PolicyModelFactory': policyModelFactoryMock,
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

        ctrl.changeOpenedCube(position);

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

        ctrl.changeOpenedCube(position);

        expect(cubeModelFactoryMock.resetCube).toHaveBeenCalledWith(fakeCubeTemplate, fakeCreatedCubes, ctrl.policy.cubes.length);
      })
    })
  });

  describe("Should be able to see changes in cube creation status and cube array in order to enable or disable next step", function () {
    it("next step is enabled only if cube creation is not activated and cube array is not empty", function () {
      ctrl.cubeCreationStatus = {};
      ctrl.policy.cubes = [];

      ctrl.cubeCreationStatus.enabled = true;
      scope.$apply();

      expect(wizardStatusServiceMock.disableNextStep).toHaveBeenCalled();

      ctrl.policy.cubes = [fakeCube];
      ctrl.cubeCreationStatus.enabled = true;
      scope.$apply();

      expect(wizardStatusServiceMock.disableNextStep).toHaveBeenCalled();

      ctrl.cubeCreationStatus.enabled = false;
      scope.$apply();

      expect(wizardStatusServiceMock.enableNextStep).toHaveBeenCalled();
    })
  });

  describe("Should be able to respond to an event to force the validations of current forms", function () {

    it("if user is creating a cube, policy error is updated to warn user about saving his changes'", function () {
      ctrl.policy.cubes = [fakeCube];
      ctrl.isActiveCubeCreationPanel.and.returnValue(true);

      scope.$broadcast("forceValidateForm");

      expect(policyModelFactoryMock.setError).toHaveBeenCalledWith('_ERROR_._CHANGES_WITHOUT_SAVING_', 'error');

      ctrl.isActiveCubeCreationPanel.and.returnValue(false);

      scope.$broadcast("forceValidateForm");

      expect(policyModelFactoryMock.setError).toHaveBeenCalledWith('_ERROR_._CHANGES_WITHOUT_SAVING_', 'error');
    });

    it("if cube creation are activated, creation panel is opened", function(){
      ctrl.policy.cubes = [fakeCube];
      ctrl.isActiveCubeCreationPanel.and.returnValue(true);

      scope.$broadcast("forceValidateForm");

      expect(ctrl.cubeAccordionStatus[ctrl.cubeAccordionStatus.length-1]).toBeTruthy();
    })
  });

});

