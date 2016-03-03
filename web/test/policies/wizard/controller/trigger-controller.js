describe('policies.wizard.controller.policy-trigger-controller', function () {
  beforeEach(module('webApp'));
  beforeEach(module('served/policy.json'));
  beforeEach(module('served/policyTemplate.json'));
  beforeEach(module('served/trigger.json'));

  var ctrl, scope, fakePolicy, fakeTriggerTemplate, fakeTrigger, policyModelFactoryMock, fakeOutputs, fakePolicyTemplate,
    triggerModelFactoryMock, outputServiceMock, triggerServiceMock, resolvedPromise, rejectedPromise;

  // init mock modules

  beforeEach(inject(function ($controller, $q, $httpBackend, $rootScope) {
    scope = $rootScope.$new();
    $httpBackend.when('GET', 'languages/en-US.json').respond({});

    inject(function (_servedPolicy_, _servedPolicyTemplate_, _servedTrigger_) {
      fakePolicy = angular.copy(_servedPolicy_);
      fakePolicyTemplate = _servedPolicyTemplate_;
      fakeTriggerTemplate = _servedPolicyTemplate_.trigger;
      fakeTrigger = angular.copy(_servedTrigger_);
    });

    resolvedPromise = function () {
      var defer = $q.defer();
      defer.resolve();

      return defer.promise;
    };

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

    triggerServiceMock = jasmine.createSpyObj('TriggerService', ['isLastTrigger', 'isNewTrigger', 'addTrigger', 'removeTrigger', 'disableTriggerCreationPanel',  'getSqlSourceItems', 'isEnabledHelpForSql']);

    triggerModelFactoryMock = jasmine.createSpyObj('TriggerFactory', ['getTrigger', 'getError', 'getTriggerInputs', 'getContext', 'setError', 'resetTrigger', 'updateTriggerInputs', 'setError']);
    triggerModelFactoryMock.getTrigger.and.returnValue(fakeTrigger);

    ctrl = $controller('TriggerCtrl', {
      'PolicyModelFactory': policyModelFactoryMock,
      'TriggerModelFactory': triggerModelFactoryMock,
      'TriggerService': triggerServiceMock,
      'OutputService': outputServiceMock
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

      it('it should get a policy template from from policy factory', function () {
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
        'TriggerService': triggerServiceMock
      });
      expect(cleanCtrl.template).toBe(undefined);
      expect(cleanCtrl.triggerContext).toBe(undefined);
      expect(cleanCtrl.policyOutputList).toBe(undefined);
    }));
  });

  describe("should be able to add a trigger to the policy", function () {
    it("trigger is not added if view validations have not been passed", function () {
      ctrl.form = {$valid: false}; //view validations have not been passed
      ctrl.addTrigger();

      expect(triggerServiceMock.addTrigger).not.toHaveBeenCalled();
    });

    it("trigger is added if view validations have been passed", function () {
      ctrl.form = {$valid: true}; //view validations have been passed
      ctrl.addTrigger();

      expect(triggerServiceMock.addTrigger).toHaveBeenCalled();
    });
  });

  it("should be able to remove the factory trigger from the policy calling to the trigger service", function () {
    ctrl.removeTrigger();

    expect(triggerServiceMock.removeTrigger).toHaveBeenCalled();
  });


  describe("should be able to add outputs to triggers", function () {
    it("if selected output is not null and it hasn't been added to trigger yet, selected output is added", function () {
      ctrl.selectedPolicyOutput = "";

      ctrl.trigger.outputs = [];

      ctrl.addOutput(); // I try to add an empty output

      expect(ctrl.trigger.outputs.length).toBe(0);

      ctrl.selectedPolicyOutput = "fake output";

      ctrl.addOutput();  // I try to add a new output

      expect(ctrl.trigger.outputs.length).toBe(1);

      ctrl.addOutput(); // I try to add the same output

      expect(ctrl.trigger.outputs.length).toBe(1); // output is not added again
    })
  })
});
