//describe('policies.wizard.controller.policy-model-controller', function () {
//  beforeEach(module('webApp'));
//  beforeEach(module('served/policy.json'));
//  beforeEach(module('served/policyTemplate.json'));
//  beforeEach(module('served/model.json'));
//
//  var ctrl, rootScope, fakePolicy, fakeTemplate, fakeModel, policyModelFactoryMock,
//    modelFactoryMock;
//
//  // init mock modules
//
//  beforeEach(inject(function ($controller, $q, $httpBackend, $rootScope) {
//    rootScope = $rootScope;
//
//    inject(function (_servedPolicy_, _servedPolicyTemplate_, _servedModel_) {
//      fakePolicy = _servedPolicy_;
//      fakeTemplate = _servedPolicyTemplate_;
//      fakeModel = _servedModel_;
//    });
//
//    $httpBackend.when('GET', 'languages/en-US.json')
//      .respond({});
//
//    policyModelFactoryMock = jasmine.createSpyObj('PolicyModelFactory', ['getCurrentPolicy', 'getTemplate']);
//    policyModelFactoryMock.getCurrentPolicy.and.callFake(function () {
//      return fakePolicy;
//    });
//
//    policyModelFactoryMock.getTemplate.and.callFake(function () {
//      return fakeTemplate;
//    });
//
//
//    modelFactoryMock = jasmine.createSpyObj('ModelFactory', ['getError']);
//
//    ctrl = $controller('PolicyModelCtrl', {
//      'PolicyModelFactory': policyModelFactoryMock,
//      'ModelFactory': modelFactoryMock
//    });
//  }));
//
//  describe("when it is initialized", function () {
//    describe("if model introduced as param is not null", function () {
//      beforeEach(function(){
//        ctrl.init(fakeModel);
//      });
//      it('it should get a policy template from from policy factory', function () {
//        expect(ctrl.template).toBe(fakeTemplate);
//      });
//
//      it('it should get the policy that is being created or edited from policy factory', function () {
//        expect(ctrl.policy).toBe(fakePolicy);
//      });
//
//      it("it should load the introduced model as an owner model", function(){
//        expect(ctrl.model).toBe(fakeModel);
//      });
//
//      it("it should load the introduced model as an owner model", function(){
//        expect(ctrl.model).toBe(fakeModel);
//      });
//
//
//    });
//
//    it ("if model introduced as param is null, no changes are executed", function(){
//      ctrl.init();
//      expect(ctrl.template).toBe(undefined);
//      expect(ctrl.policy).toBe(undefined);
//      expect(ctrl.model).toBe(undefined);
//
//    })
//  });
//
//  //describe("should be able to add a model to the policy", function () {
//  //
//  //  it("model is not added if it is not valid", function () {
//  //    modelFactoryMock.isValidModel.and.returnValue(false);
//  //    ctrl.addModel();
//  //
//  //    expect(ctrl.policy.models.length).toBe(0);
//  //  });
//  //
//  //  describe("if model is valid", function () {
//  //    beforeEach(function () {
//  //      modelFactoryMock.isValidModel.and.returnValue(true);
//  //      ctrl.newModel = fakeModel;
//  //      ctrl.addModel();
//  //    });
//  //
//  //    it("it is added to policy with its order", function () {
//  //      expect(ctrl.policy.models.length).toBe(1);
//  //      expect(ctrl.policy.models[0].name).toEqual(fakeModel.name);
//  //      expect(ctrl.policy.models[0].order).toEqual(1);
//  //    });
//  //
//  //    it("model and accordion status are reset", function () {
//  //      expect(modelFactoryMock.resetModel).toHaveBeenCalled();
//  //      expect(accordionStatusServiceMock.resetAccordionStatus).toHaveBeenCalled();
//  //    });
//  //
//  //    it("the status of new model accordion tab is expanded", function () {
//  //      expect(ctrl.accordionStatus.newItem).toBeTruthy();
//  //    });
//  //  });
//  //
//  //});
//  //
//  //describe("should be able to remove a model from the policy using the position of the model", function () {
//  //  beforeEach(inject(function ($rootScope) {
//  //    ctrl.policy.models = [fakeModel];
//  //    rootScope = $rootScope;
//  //  }));
//  //
//  //  it("if position introduced as param is not valid, no model is removed", function () {
//  //    var invalidPosition = -1;
//  //    //position minor than 0
//  //    ctrl.removeModel(invalidPosition).then(function () {
//  //    }, function () {
//  //      expect(ctrl.policy.models.length).toBe(1);
//  //    });
//  //    //position equal to model list length
//  //    invalidPosition = ctrl.policy.models.length;
//  //    ctrl.removeModel(invalidPosition).then(function () {
//  //    }, function () {
//  //      expect(ctrl.policy.models.length).toBe(1);
//  //    });
//  //
//  //    //null position
//  //    invalidPosition = null;
//  //    ctrl.removeModel(invalidPosition).then(function () {
//  //    }, function () {
//  //      expect(ctrl.policy.models.length).toBe(1);
//  //    });
//  //
//  //    //undefined position
//  //    invalidPosition = undefined;
//  //    ctrl.removeModel(invalidPosition).then(function () {
//  //    }, function () {
//  //      expect(ctrl.policy.models.length).toBe(1);
//  //    });
//  //    rootScope.$apply();
//  //
//  //  });
//  //
//  //  describe("if position is valid", function () {
//  //    var validPosition = 0;
//  //    var cubeMockWithModelOutput, fakeCubeNames = null;
//  //
//  //    beforeEach(function () {
//  //      fakeCubeNames = [fakeModel.outputFields[0]];
//  //      accordionStatusServiceMock.resetAccordionStatus.calls.reset();
//  //      modelFactoryMock.resetModel.calls.reset();
//  //
//  //      cubeMockWithModelOutput = {
//  //        "dimensions": [{"field": fakeModel.outputFields[0]}, {"field": "any"}]
//  //      };
//  //      var cubeMockWithoutModelOutput = {
//  //        "dimensions": [{"field": "any"}, {"field": "another"}]
//  //      };
//  //      ctrl.policy.cubes = [cubeMockWithoutModelOutput, cubeMockWithModelOutput];
//  //      cubeServiceMock.findCubesUsingOutputs.and.returnValue({names: fakeCubeNames, positions: [1]});
//  //    });
//  //
//  //    afterEach(function () {
//  //      rootScope.$apply();
//  //    });
//  //
//  //    it("should find and remove all cubes which use the outputs of the model", function () {
//  //      ctrl.removeModel(validPosition).then(function () {
//  //        expect(ctrl.policy.models.length).toBe(0);
//  //        expect(ctrl.policy.cubes.length).toBe(1);
//  //      });
//  //    });
//  //
//  //    it("should show a confirmation modal with the cubes which use some of the model outputs", function () {
//  //      var expectedModalResolve = {
//  //        title: function () {
//  //          return "_REMOVE_MODEL_CONFIRM_TITLE_"
//  //        },
//  //        message: function () {
//  //          return fakeTranslation;
//  //        }
//  //      };
//  //      ctrl.removeModel(validPosition).then(function () {
//  //        expect(modalServiceMock.openModal.calls.mostRecent().args[0]).toBe('ConfirmModalCtrl');
//  //        expect(modalServiceMock.openModal.calls.mostRecent().args[1]).toBe('templates/modal/confirm-modal.tpl.html');
//  //        expect(modalServiceMock.openModal.calls.mostRecent().args[2].title()).toEqual(expectedModalResolve.title());
//  //        expect(modalServiceMock.openModal.calls.mostRecent().args[2].message()).toEqual(expectedModalResolve.message());
//  //        expect(translate).toHaveBeenCalledWith('_REMOVE_MODEL_MESSAGE_', {modelList: fakeCubeNames.toString()});
//  //      });
//  //    });
//  //
//  //  })
//  //});
//  //
//  //describe("should be able to validate the models created by user in order to change to the next step", function () {
//  //  it("if no model is created, error is generated and step is not changed", function () {
//  //    ctrl.policy.models = [];
//  //    ctrl.nextStep();
//  //
//  //    expect(ctrl.error).toBe("_POLICY_._MODEL_ERROR_");
//  //    expect(policyModelFactoryMock.nextStep).not.toHaveBeenCalled();
//  //  });
//  //
//  //  it("if a model is created at least, error is not generated and step is changed", function () {
//  //    ctrl.policy.models.push(fakeModel);
//  //    ctrl.nextStep();
//  //
//  //    expect(ctrl.error).toBe("");
//  //    expect(policyModelFactoryMock.nextStep).toHaveBeenCalled();
//  //  });
//  //
//  //});
//  //
//  //it("should be able to return if a model is the last model in the model array by its position", function () {
//  //  ctrl.policy.models = [];
//  //  ctrl.policy.models.push(fakeModel);
//  //  ctrl.policy.models.push(fakeModel);
//  //  ctrl.policy.models.push(fakeModel);
//  //
//  //  expect(ctrl.isLastModel(0)).toBeFalsy();
//  //  expect(ctrl.isLastModel(1)).toBeFalsy();
//  //  expect(ctrl.isLastModel(2)).toBeTruthy();
//  //})
//});
