describe('policies.wizard.factory.trigger-model-factory', function () {
  beforeEach(module('webApp'));
  beforeEach(module('api/trigger.json'));
  beforeEach(module('model/trigger.json'));
  beforeEach(module('model/policy.json'));

  var factory, UtilsServiceMock, PolicyModelFactoryMock, fakeApiTrigger, fakeTrigger, fakePolicy, triggerConstants = null;

  beforeEach(module(function ($provide) {
    UtilsServiceMock = jasmine.createSpyObj('UtilsService', ['removeItemsFromArray', 'findElementInJSONArray']);
    PolicyModelFactoryMock = jasmine.createSpyObj('PolicyModelFactory', ['getCurrentPolicy']);
    PolicyModelFactoryMock.getCurrentPolicy.and.returnValue(fakePolicy);
    // inject mocks
    $provide.value('UtilsService', UtilsServiceMock);
    $provide.value('PolicyModelFactory', PolicyModelFactoryMock);

  }));

  beforeEach(inject(function (TriggerModelFactory, _apiTrigger_, _modelTrigger_, _modelPolicy_, _triggerConstants_) {
    factory = TriggerModelFactory;
    fakeApiTrigger = _apiTrigger_;
    fakeTrigger = _modelTrigger_;
    fakePolicy = angular.copy(_modelPolicy_);
    triggerConstants = _triggerConstants_;
  }));

  it("should be able to load a trigger from a json and a position", function () {
    var position = 0;
    factory.setTrigger(fakeApiTrigger, position);
    var trigger = factory.getTrigger();
    expect(trigger.name).toBe(fakeApiTrigger.name);
    expect(trigger.sql).toBe(fakeApiTrigger.sql);
    expect(trigger.outputs).toBe(fakeApiTrigger.outputs);
    expect(trigger.primaryKey).toBe(fakeApiTrigger.primaryKey);
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

    describe("if there is not any trigger, it initializes a new one using the introduced position and type", function () {
      beforeEach(inject(function (_TriggerModelFactory_) {
        cleanFactory = _TriggerModelFactory_; // inject a new factory in each test to can check the initial state of the factory when it is created
      }));
      it("if type is cube", function () {
        var trigger = cleanFactory.getTrigger(desiredPosition, triggerConstants.CUBE);
        expect(trigger.name).toEqual("");
        expect(trigger.sql).toEqual("");
        expect(trigger.outputs).toEqual([]);
        expect(trigger.overLastNumber).toBe(undefined);
        expect(trigger.overLastTime).toBe(undefined);
        expect(trigger.computeEveryNumber).toBe(undefined);
        expect(trigger.computeEveryTime).toBe(undefined);
        expect(cleanFactory.getError()).toEqual({"text": ""});
        expect(factory.getContext().position).toBe(desiredPosition);
      });

      it("if type is transformation", function () {
        var trigger = cleanFactory.getTrigger(desiredPosition, triggerConstants.TRANSFORMATION);
        var overLast = fakeApiTrigger.overLast.split(/([0-9]+)/);
        var computeEvery = fakeApiTrigger.computeEvery.split(/([0-9]+)/);
        expect(trigger.name).toEqual("");
        expect(trigger.sql).toEqual("");
        expect(trigger.outputs).toEqual([]);
        expect(trigger.overLastNumber).toBe(Number(overLast[1]));
        expect(trigger.overLastTime).toBe(overLast[2]);
        expect(trigger.computeEveryNumber).toBe(Number(computeEvery[1]));
        expect(trigger.computeEveryTime).toBe(computeEvery[2]);
        expect(cleanFactory.getError()).toEqual({"text": ""});
        expect(factory.getContext().position).toBe(desiredPosition);
      });

    });

    it("if there is a trigger, returns that trigger", function () {
      factory.setTrigger(fakeApiTrigger, desiredPosition, 'cube');

      var trigger = factory.getTrigger(desiredPosition);
      expect(trigger.name).toEqual(fakeApiTrigger.name);
      expect(trigger.sql).toEqual(fakeApiTrigger.sql);
      expect(trigger.outputs).toEqual(fakeApiTrigger.outputs);
      expect(trigger.primaryKey).toBe(fakeApiTrigger.primaryKey);

      factory.setTrigger(fakeApiTrigger, desiredPosition, 'transformation');

      var trigger = factory.getTrigger(desiredPosition);
      var overLast = fakeApiTrigger.overLast.split(/([0-9]+)/);
      var computeEvery = fakeApiTrigger.computeEvery.split(/([0-9]+)/);
      expect(trigger.name).toEqual(fakeApiTrigger.name);
      expect(trigger.sql).toEqual(fakeApiTrigger.sql);
      expect(trigger.outputs).toEqual(fakeApiTrigger.outputs);
      expect(trigger.primaryKey).toEqual(fakeApiTrigger.primaryKey);
      expect(trigger.overLastNumber).toEqual(Number(overLast[1]));
      expect(trigger.overLastTime).toEqual(overLast[2]);
      expect(trigger.computeEveryNumber).toEqual(Number(computeEvery[1]));
      expect(trigger.computeEveryTime).toEqual(computeEvery[2]);
    });

    it("if there is not any trigger and no position is introduced, trigger is initialized with position equal to 0", function () {
      var trigger = cleanFactory.getTrigger();
      expect(factory.getContext().position).toBe(0);
    })
  });

  describe("should be able to validate a trigger", function () {
    beforeEach(function () {
      UtilsServiceMock.findElementInJSONArray.and.returnValue(-1); //not found in the array
    });
    describe("all its attributes can not be empty", function () {

      it("if empty name, trigger is invalid", function () {
        var invalidTrigger = angular.copy(fakeTrigger);
        invalidTrigger.name = "";

        factory.setTrigger(invalidTrigger);

        expect(factory.isValidTrigger({})).toBeFalsy();
      });

      it("if empty outputs, trigger is valid", function () {
        var invalidTrigger = angular.copy(fakeTrigger);
        invalidTrigger.outputs = [];

        factory.setTrigger(invalidTrigger);

        expect(factory.isValidTrigger({})).toBeTruthy();
      });

      it("if empty configuration, trigger is valid", function () {
        var invalidTrigger = angular.copy(fakeTrigger);
        invalidTrigger.configuration = null;

        factory.setTrigger(invalidTrigger);

        expect(factory.isValidTrigger({})).toBeTruthy();
      });

    });

    describe("trigger name can not be repeated", function () {

      it("if trigger list is empty, trigger is valid with any name", function () {
        var triggerList = [];
        var newName = "new trigger name";
        var trigger = angular.copy(fakeTrigger);
        trigger.name = newName;

        factory.setTrigger(trigger);

        expect(factory.isValidTrigger(triggerList)).toBeTruthy();
      });

      describe("if trigger list is not empty", function () {
        var triggerList = null;
        beforeEach(function () {
          var fakeTrigger2 = angular.copy(fakeTrigger);
          fakeTrigger2.name = "fake trigger 2";
          triggerList = [fakeTrigger2, fakeTrigger];

          factory.setTrigger(fakeTrigger);

        });

        it("and it has a trigger with the same name and its position is not the same that the introduced one, trigger is invalid", function () {
          UtilsServiceMock.findElementInJSONArray.and.returnValue(1);
          expect(factory.isValidTrigger(triggerList, 2)).toBeFalsy();
        });

        it("and it has a trigger with the same name and its position is the same that the introduced one, trigger is valid", function () {
          //the trigger that is being validated and the found trigger in the list are the same trigger
          UtilsServiceMock.findElementInJSONArray.and.returnValue(1);
          expect(factory.isValidTrigger(triggerList, 1)).toBeTruthy();
        });

        it("but it has not any trigger with the same name, trigger is valid", function () {
          UtilsServiceMock.findElementInJSONArray.and.returnValue(-1);
          var validTrigger = angular.copy(fakeTrigger);
          validTrigger.name = "new trigger name";

          factory.setTrigger(validTrigger);

          expect(factory.isValidTrigger(triggerList, 2)).toBeTruthy();
        });
      });

    });

    describe("should be able to valid if overLast and compute every is multiple of the policy spark streaming window", function () {
      var validTrigger = null;
      beforeEach(function () {
        validTrigger = angular.copy(fakeTrigger);
        validTrigger.name = "fake trigger name";
        validTrigger.sql = "Select * from STREAM";
        validTrigger.overLastNumber = 2 * validTrigger.sparkStreamingWindowNumber ;
         validTrigger.computeEveryNumber = 2 * validTrigger.sparkStreamingWindowNumber ;
      });

      it("if policy spark streaming window is higher than 0 and overLast is defined, it has to be multiple", function () {
        var fakeSparkStreamingWindowNumber = 4;
        fakePolicy.sparkStreamingWindowNumber = fakeSparkStreamingWindowNumber;
        PolicyModelFactoryMock.getCurrentPolicy.and.returnValue(fakePolicy);
        validTrigger.overLastNumber = 2 * fakeSparkStreamingWindowNumber + 1;

        factory.setTrigger(validTrigger, 0, triggerConstants.TRANSFORMATION);

        expect(factory.isValidTrigger([], 0)).toBeFalsy();

        validTrigger.overLastNumber = 2 * fakeSparkStreamingWindowNumber;
        factory.setTrigger(validTrigger, 0, triggerConstants.TRANSFORMATION);

        expect(factory.isValidTrigger([], 0)).toBeTruthy();
      });

      it("if policy spark streaming window is higher than 0 and computeEvery is defined, it has to be multiple", function () {
        var fakeSparkStreamingWindowNumber = 4;
        fakePolicy.sparkStreamingWindowNumber = fakeSparkStreamingWindowNumber;
        PolicyModelFactoryMock.getCurrentPolicy.and.returnValue(fakePolicy);
        validTrigger.computeEveryNumber = 2 * fakeSparkStreamingWindowNumber + 1;

        factory.setTrigger(validTrigger, 0, triggerConstants.TRANSFORMATION);

        expect(factory.isValidTrigger([], 0)).toBeFalsy();

        validTrigger.computeEveryNumber = 2 * fakeSparkStreamingWindowNumber;
        factory.setTrigger(validTrigger, 0, triggerConstants.TRANSFORMATION);

        expect(factory.isValidTrigger([], 0)).toBeTruthy();
      });

      it("if policy hasn't got policy spark streaming window, overlast is valid", function () {
        var fakeSparkStreamingWindowNumber = undefined;
        validTrigger.overLastNumber = 5;

        expect(factory.isValidTrigger([], 0, fakeSparkStreamingWindowNumber)).toBeTruthy();

        validTrigger.overLastNumber = 13;

        expect(factory.isValidTrigger([], 0, fakeSparkStreamingWindowNumber)).toBeTruthy();
      });

      it("if policy hasn't got policy spark streaming window, computeEvery is valid", function () {
        var fakeSparkStreamingWindowNumber = undefined;
        validTrigger.computeEveryNumber = 5;

        expect(factory.isValidTrigger([], 0, fakeSparkStreamingWindowNumber)).toBeTruthy();

        validTrigger.computeEveryNumber = 13;

        expect(factory.isValidTrigger([], 0, fakeSparkStreamingWindowNumber)).toBeTruthy();
      });

      it("if overLast is undefined, trigger is valid", function () {
        var fakeSparkStreamingWindowNumber = undefined;
        validTrigger.overLastNumber = undefined;

        expect(factory.isValidTrigger([], 0, fakeSparkStreamingWindowNumber)).toBeTruthy();

        validTrigger.overLastNumber = null;

        expect(factory.isValidTrigger([], 0, fakeSparkStreamingWindowNumber)).toBeTruthy();
      });

      it("if computeEvery is undefined, trigger is valid", function () {
        var fakeSparkStreamingWindowNumber = undefined;
        validTrigger.computeEveryNumber = undefined;

        expect(factory.isValidTrigger([], 0, fakeSparkStreamingWindowNumber)).toBeTruthy();

        validTrigger.computeEveryNumber = null;

        expect(factory.isValidTrigger([], 0, fakeSparkStreamingWindowNumber)).toBeTruthy();
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
