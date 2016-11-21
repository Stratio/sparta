describe('policies.wizard.controller.policy-trigger-controller', function () {
  beforeEach(module('webApp'));
  beforeEach(module('model/policy.json'));
  beforeEach(module('template/policy.json'));
  beforeEach(module('model/trigger.json'));

  var ctrl, scope, fakePolicy, fakeTriggerTemplate, fakeTrigger, policyModelFactoryMock, fakeOutputs, fakePolicyTemplate,
    triggerModelFactoryMock, outputServiceMock, triggerServiceMock, rejectedPromise;

  // init mock modules

  beforeEach(inject(function ($controller, $q, $httpBackend, $rootScope) {
    scope = $rootScope.$new();
    $httpBackend.when('GET', 'languages/en-US.json').respond({});

    inject(function (_modelPolicy_, _templatePolicy_, _modelTrigger_) {
      fakePolicy = angular.copy(_modelPolicy_);
      fakePolicyTemplate = _templatePolicy_;
      fakeTriggerTemplate = _templatePolicy_.trigger;
      fakeTrigger = angular.copy(_modelTrigger_);
    });

    fakeOutputs = [{label: "output1", value: "output1"}, {label: "output2", value: "output2"}, {
      label: "output3",
      value: "output3"
    }, {label: "output4", value: "output4"}, {label: "output5", value: "output5"}];

    outputServiceMock = jasmine.createSpyObj('OutputService', ['generateOutputNameList']);
    outputServiceMock.generateOutputNameList.and.callFake(function () {
      var defer = $q.defer();
      defer.resolve(fakeOutputs);

      return defer.promise;
    });

    policyModelFactoryMock = jasmine.createSpyObj('PolicyModelFactory', ['getCurrentPolicy', 'getTemplate', 'getAllModelOutputs']);
    policyModelFactoryMock.getCurrentPolicy.and.callFake(function () {
      return fakePolicy;
    });

    policyModelFactoryMock.getTemplate.and.callFake(function () {
      return fakePolicyTemplate;
    });

    triggerServiceMock = jasmine.createSpyObj('TriggerService', ['isLastTrigger', 'isNewTrigger', 'addTrigger', 'removeTrigger', 'getTriggerTemplate','disableTriggerCreationPanel',  'getSqlSourceItems', 'isEnabledHelpForSql']);
    triggerServiceMock.getTriggerTemplate.and.returnValue(fakeTriggerTemplate);
    triggerModelFactoryMock = jasmine.createSpyObj('TriggerFactory', ['getTrigger', 'getError', 'getTriggerInputs', 'getContext', 'setError', 'resetTrigger', 'updateTriggerInputs', 'setError']);
    triggerModelFactoryMock.getTrigger.and.returnValue(fakeTrigger);

    ctrl = $controller('TriggerCtrl', {
      'PolicyModelFactory': policyModelFactoryMock,
      'TriggerModelFactory': triggerModelFactoryMock,
      'TriggerService': triggerServiceMock,
      'OutputService': outputServiceMock,
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

    scope.$apply();
  }));

  describe("when it is initialized", function () {
    describe("if factory trigger is not null", function () {

      it('it should get the trigger template from the trigger service', function () {
        expect(ctrl.template).toBe(fakeTriggerTemplate);
      });

      it("it should load the trigger from the trigger factory", function () {
        expect(ctrl.trigger).toBe(fakeTrigger);
      });

      it("it should load an output list with all outputs of the models", function () {
        expect(ctrl.policyOutputList).toBe(fakeOutputs);
      });
    });

    it("if factory trigger is null, no changes are executed", inject(function ($controller) {
      triggerModelFactoryMock.getTrigger.and.returnValue(null);
      var cleanCtrl = $controller('TriggerCtrl', {
        'PolicyModelFactory': policyModelFactoryMock,
        'TriggerModelFactory': triggerModelFactoryMock,
        'TriggerService': triggerServiceMock,
        '$scope': scope
      });
      expect(cleanCtrl.template).toBe(undefined);
      expect(cleanCtrl.triggerContext).toBe(undefined);
      expect(cleanCtrl.policyOutputList).toBe(undefined);
    }));
  });

  describe("should be able to add a trigger to the policy", function () {

  });

  it("should be able to remove the factory trigger from the policy calling to the trigger service", function () {
    ctrl.removeTrigger();

    expect(triggerServiceMock.removeTrigger).toHaveBeenCalled();
  });


  describe("should be able to add outputs to triggers", function () {
    it("if selected output is not null and it hasn't been added to trigger yet, selected output is added", function () {
      ctrl.selectedPolicyOutput = "";

      ctrl.trigger.writer.outputs = [];

      ctrl.addOutput(); // I try to add an empty output

      expect(ctrl.trigger.writer.outputs.length).toBe(0);

      ctrl.selectedPolicyOutput = "fake output";

      ctrl.addOutput();  // I try to add a new output

      expect(ctrl.trigger.writer.outputs.length).toBe(1);

      ctrl.addOutput(); // I try to add the same output

      expect(ctrl.trigger.writer.outputs.length).toBe(1); // output is not added again
    })
  })
});
