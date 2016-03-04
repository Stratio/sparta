describe('policies.wizard.factory.trigger-model-factory', function () {
  beforeEach(module('webApp'));
  beforeEach(module('served/trigger.json'));
  beforeEach(module('served/policy.json'));

  var factory, UtilsServiceMock,PolicyModelFactoryMock, fakeTrigger, fakePolicy = null;

  beforeEach(module(function ($provide) {
    UtilsServiceMock = jasmine.createSpyObj('UtilsService', ['removeItemsFromArray', 'findElementInJSONArray']);
    PolicyModelFactoryMock =  jasmine.createSpyObj('PolicyModelFactory', ['getCurrentPolicy']);
    PolicyModelFactoryMock.getCurrentPolicy.and.returnValue(fakePolicy);
    // inject mocks
    $provide.value('UtilsService', UtilsServiceMock);
    $provide.value('PolicyModelFactory', PolicyModelFactoryMock);

  }));

  beforeEach(inject(function (TriggerModelFactory, _servedTrigger_, _servedPolicy_) {
    factory = TriggerModelFactory;
    fakeTrigger = _servedTrigger_;
    fakePolicy = _servedPolicy_;
  }));

  it("should be able to load a trigger from a json and a position", function () {
    var position = 0;
    factory.setTrigger(fakeTrigger, position);
    var trigger = factory.getTrigger();
    expect(trigger.name).toBe(fakeTrigger.name);
    expect(trigger.sql).toBe(fakeTrigger.sql);
    expect(trigger.outputs).toBe(fakeTrigger.outputs);
    expect(trigger.configuration).toBe(fakeTrigger.configuration);
    expect(factory.getError()).toEqual({"text": ""});
    expect(factory.getContext().position).toBe(position);
  });

  it("should be able to update the position of the trigger", function () {
    var position = 2;
    factory.setPosition(position);
    expect(factory.getContext().position).toEqual(position);
  });

  describe("should return its current trigger", function () {
    var cleanFactory = null;
    var desiredPosition = 6;
    beforeEach(inject(function (_TriggerModelFactory_) {
      cleanFactory = _TriggerModelFactory_; // inject a new factory in each test to can check the initial state of the factory when it is created
    }));

    it("if there is not any trigger, it initializes a new one using the introduced position", function () {
      var trigger = cleanFactory.getTrigger(desiredPosition);
      expect(trigger.name).toEqual("");
      expect(trigger.sql).toEqual("");
      expect(trigger.outputs).toEqual([]);
      expect(trigger.configuration).toEqual(undefined);
      expect(cleanFactory.getError()).toEqual({"text": ""});
      expect(factory.getContext().position).toBe(desiredPosition);
    });

    it("if there is a trigger, returns that trigger", function () {
      factory.setTrigger(fakeTrigger, desiredPosition);

      var trigger = factory.getTrigger(desiredPosition);

      expect(trigger.name).toEqual(fakeTrigger.name);
      expect(trigger.sql).toEqual(fakeTrigger.sql);
      expect(trigger.outputs).toEqual(fakeTrigger.outputs);
      expect(trigger.configuration).toEqual(fakeTrigger.configuration);

      expect(trigger.primaryKey).toEqual(fakeTrigger.primaryKey);
      expect(trigger.overLastNumber).toEqual(fakePolicy.sparkStreamingWindowNumber);
      expect(trigger.overLastTime).toEqual(fakePolicy.sparkStreamingWindowTime);

      trigger.name = "";
      trigger.sql = "";
      trigger.outputs = [];
      trigger.primaryKey = [];
      trigger.overLastNumber =fakePolicy.sparkStreamingWindowNumber;
      trigger.overLastTime = fakePolicy.sparkStreamingWindowTime;
    });

    it("if there is not any trigger and no position is introduced, trigger is initialized with position equal to 0", function () {
      var trigger = cleanFactory.getTrigger();
      expect(factory.getContext().position).toBe(0);
    })
  });

  describe("should be able to validate a trigger", function () {
    describe("all its attributes can not be empty", function () {
      beforeEach(function () {
        UtilsServiceMock.findElementInJSONArray.and.returnValue(-1); //not found in the array
      });

      it("if empty name, trigger is invalid", function () {
        var invalidTrigger = angular.copy(fakeTrigger);
        invalidTrigger.name = "";

        expect(factory.isValidTrigger(invalidTrigger, {})).toBeFalsy();
      });

      it("if empty outputs, trigger is valid", function () {
        var invalidTrigger = angular.copy(fakeTrigger);
        invalidTrigger.outputs = [];

        expect(factory.isValidTrigger(invalidTrigger, {})).toBeTruthy();
      });

      it("if empty configuration, trigger is valid", function () {
        var invalidTrigger = angular.copy(fakeTrigger);
        invalidTrigger.configuration = null;

        expect(factory.isValidTrigger(invalidTrigger, {})).toBeTruthy();
      });

    });

    describe("trigger name can not be repeated", function () {

      it("if trigger list is empty, trigger is valid with any name", function () {
        var triggerList = [];
        var newName = "new trigger name";
        var trigger = angular.copy(fakeTrigger);
        trigger.name = newName;
        expect(factory.isValidTrigger(trigger, triggerList));
      });

      describe("if trigger list is not empty", function () {
        var triggerList = null;
        beforeEach(function () {
          var fakeTrigger2 = angular.copy(fakeTrigger);
          fakeTrigger2.name = "fake trigger 2";
          triggerList = [fakeTrigger2, fakeTrigger];
        });

        it("and it has a trigger with the same name and its position is not the same that the introduced one, trigger is invalid", function () {
          UtilsServiceMock.findElementInJSONArray.and.returnValue(1);
          expect(factory.isValidTrigger(fakeTrigger, triggerList, 2)).toBeFalsy();
        });

        it("and it has a trigger with the same name and its position is the same that the introduced one, trigger is valid", function () {
          //the trigger that is being validated and the found trigger in the list are the same trigger
          UtilsServiceMock.findElementInJSONArray.and.returnValue(1);
          expect(factory.isValidTrigger(fakeTrigger, triggerList, 1)).toBeTruthy();
        });

        it("but it has not any trigger with the same name, trigger is valid", function () {
          UtilsServiceMock.findElementInJSONArray.and.returnValue(-1);
          var validTrigger = angular.copy(fakeTrigger);
          validTrigger.name = "new trigger name";
          expect(factory.isValidTrigger(validTrigger, triggerList, 2)).toBeTruthy();
        });
      });

    });
  });
  it("should be able to reset its trigger to set all attributes with default values", function () {
    var oldPosition = 2;
    factory.setTrigger(fakeTrigger, oldPosition);
    var newPosition = 5;
    factory.resetTrigger(newPosition);

    var trigger = factory.getTrigger(newPosition);
    expect(trigger.name).toEqual("");
    expect(trigger.sql).toEqual("");
    expect(trigger.outputs).toEqual([]);
    expect(trigger.configuration).toEqual(undefined);
    expect(factory.getError()).toEqual({"text": ""});
    expect(factory.getContext().position).toBe(newPosition);
  });
})
;
