describe('policies.wizard.controller.policy-trigger-controller', function() {
  beforeEach(module('webApp'));
  beforeEach(module('model/policy.json'));
  beforeEach(module('template/policy.json'));
  beforeEach(module('template/trigger/transformation.json'));
  beforeEach(module('model/trigger.json'));

  var ctrl, scope, fakePolicy, fakeTriggerTemplate, fakeTrigger, policyModelFactoryMock, fakePolicyTemplate,
      fakeTransformationTriggerTemplate, triggerModelFactoryMock, triggerServiceMock, rejectedPromise,
      templateFactoryMock, outputServiceMock;

  // init mock modules

  beforeEach(inject(function($controller, $q, $httpBackend, $rootScope) {
    scope = $rootScope.$new();
    $httpBackend.when('GET', 'languages/en-US.json').respond({});

    inject(function(_modelPolicy_, _templatePolicy_, _modelTrigger_, _templateTriggerTransformation_) {
      fakePolicy = angular.copy(_modelPolicy_);
      fakePolicyTemplate = _templatePolicy_;
      fakeTriggerTemplate = _templatePolicy_.trigger;
      fakeTrigger = angular.copy(_modelTrigger_);
      fakeTransformationTriggerTemplate = _templateTriggerTransformation_;
    });

    policyModelFactoryMock = jasmine.createSpyObj('PolicyModelFactory', ['getCurrentPolicy', 'getTemplate', 'getAllModelOutputs']);
    policyModelFactoryMock.getCurrentPolicy.and.callFake(function() {
      return fakePolicy;
    });

    policyModelFactoryMock.getTemplate.and.callFake(function() {
      return fakePolicyTemplate;
    });

    triggerServiceMock = jasmine.createSpyObj('TriggerService', ['isLastTrigger', 'isNewTrigger', 'addTrigger', 'removeTrigger', 'getTriggerTemplate', 'disableTriggerCreationPanel', 'getSqlSourceItems', 'isEnabledHelpForSql', 'getTriggerContainerType']);
    triggerServiceMock.getTriggerTemplate.and.returnValue(fakeTriggerTemplate);
    triggerModelFactoryMock = jasmine.createSpyObj('TriggerFactory', ['getTrigger', 'getError', 'getTriggerInputs', 'getContext', 'setError', 'resetTrigger', 'updateTriggerInputs', 'setError']);
    triggerModelFactoryMock.getTrigger.and.returnValue(fakeTrigger);
    templateFactoryMock = jasmine.createSpyObj('TemplateFactory', ['getTriggerTemplateByType']);
    templateFactoryMock.getTriggerTemplateByType.and.callFake(function() {
      var defer = $q.defer();
      defer.resolve(fakeTransformationTriggerTemplate);
      return defer.promise;
    });
    ctrl = $controller('TriggerCtrl', {
      'PolicyModelFactory': policyModelFactoryMock,
      'TriggerModelFactory': triggerModelFactoryMock,
      'TriggerService': triggerServiceMock,
      'OutputService': outputServiceMock,
      'TemplateFactory': templateFactoryMock,
      '$scope': scope
    });

    resolvedPromise = function() {
      var defer = $q.defer();
      defer.resolve();

      return defer.promise;
    };

    rejectedPromise = function() {
      var defer = $q.defer();
      defer.reject();

      return defer.promise;
    };

    scope.$apply();
  }));

  describe("when it is initialized", function() {
    describe("if factory trigger is not null", function() {

      it('it should get the trigger template from the trigger service', function() {
        expect(ctrl.template).toBe(fakeTransformationTriggerTemplate);
      });

      it("it should load the trigger from the trigger factory", function() {
        expect(ctrl.trigger).toBe(fakeTrigger);
      });
    });

    it("if factory trigger is null, no changes are executed", inject(function($controller) {
      triggerModelFactoryMock.getTrigger.and.returnValue(null);
      var cleanCtrl = $controller('TriggerCtrl', {
        'PolicyModelFactory': policyModelFactoryMock,
        'TriggerModelFactory': triggerModelFactoryMock,
        'TriggerService': triggerServiceMock,
        'TemplateFactory': templateFactoryMock,
        '$scope': scope
      });
      expect(cleanCtrl.template).toBe(undefined);
      expect(cleanCtrl.triggerContext).toBe(undefined);
      expect(cleanCtrl.policyOutputList).toBe(undefined);
    }));
  });

  describe("should be able to add a trigger to the policy", function() {

  });

  it("should be able to remove the factory trigger from the policy calling to the trigger service", function() {
    ctrl.removeTrigger();

    expect(triggerServiceMock.removeTrigger).toHaveBeenCalled();
  });
});
