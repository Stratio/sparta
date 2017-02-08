describe('policies.wizard.factory.model-factory', function() {
  beforeEach(module('webApp'));
  beforeEach(module('model/transformation.json'));
  beforeEach(module('model/dateTime.json'));
  beforeEach(module('api/dateTime.json'));
  beforeEach(module('template/policy.json'));

  var factory, fakeModel, fakeDateTimeModel, fakeApiDateTime, fakeModelTemplate, modelConstants = null;


  beforeEach(inject(function(ModelFactory, _modelConstants_, _modelTransformation_, _modelDateTime_, _apiDateTime_, _templatePolicy_) {
    factory = ModelFactory;
    modelConstants = _modelConstants_;
    fakeModel = _modelTransformation_;
    fakeDateTimeModel = _modelDateTime_;
    fakeApiDateTime = _apiDateTime_;
    fakeModelTemplate = _templatePolicy_.model;
  }));

  describe("should be able to load a model from a json and a position", function() {

    it('a no datetime transformation is loaded with the same data of json', function() {
      var position = 0;
      factory.setModel(fakeModel, position);
      var model = factory.getModel();
      expect(model.outputFields).toBe(fakeModel.outputFields);
      expect(model.type).toBe(fakeModel.type);
      expect(model.inputField).toBe(fakeModel.inputField);
      expect(model.order).toBe(fakeModel.order);
      expect(factory.getError()).toEqual({"text": "", "duplicatedOutput": false});
    });

    describe('datetime transformation is loaded adding a value to its granularity attribute depending on json source', function() {
      var position = 0;
      beforeEach(function(){
        factory.resetModel(fakeModelTemplate);
      });
      it('if json is sent by back server, granularity attribute is divided on granularityNumber and granularityTime', function() {
        var granularity = fakeApiDateTime.configuration.granularity.split(/([0-9]+)/); // 25ms
        factory.setModel(fakeApiDateTime, position);
        var model = factory.getModel();
        expect(model.type).toBe("DateTime");
        expect(model.configuration.granularityNumber.toString()).toBe(granularity[1]); //25
        expect(model.configuration.granularityTime).toBe(granularity[2]); //ms
      });
      it('if json is sent by front server, granularityNumber and granularityTime attributes are created using granularity attribute', function() {
        factory.setModel(fakeDateTimeModel, position);
        var model = factory.getModel();
        expect(model.type).toBe("DateTime");
        expect(model.configuration.granularity).toBeUndefined();
        expect(model.configuration.granularityNumber.toString()).toBe(fakeDateTimeModel.configuration.granularityNumber);
        expect(model.configuration.granularityTime).toBe(fakeDateTimeModel.configuration.granularityTime);
      })
    })
  });
  it("should be able to update the model error", function() {
    var error = "_NEW_ERROR_KEY";
    factory.setError(error);
    expect(factory.getError()).toEqual({"text": error, duplicatedOutput: false});
  });

  it("should be able to update the position of the model", function() {
    var position = 2;
    factory.setPosition(position);
    expect(factory.getContext().position).toEqual(position);
  });

  describe("should return its current model", function() {
    var cleanFactory = null;
    beforeEach(inject(function(_ModelFactory_) {
      cleanFactory = _ModelFactory_; // inject a new factory in each test to can check the initial state of the factory when it is created
    }));

    it("if there is not any model, it initializes a new one using the introduced template a position", function() {
      var desiredOrder = 0;
      var position = 1;
      var model = cleanFactory.getModel(fakeModelTemplate, desiredOrder, position);

      expect(model.outputFields).toEqual([]);
      expect(model.type).toBe(fakeModelTemplate.types[0].name);
      expect(model.order).toBe(desiredOrder);
      expect(factory.getError()).toEqual({"text": "", "duplicatedOutput": false});
      expect(factory.getContext().position).toBe(position);
    });

    it("if there is a model, returns that model", function() {
      var desiredOrder = 0;
      factory.setModel(fakeModel, desiredOrder);

      expect(factory.getModel(fakeModelTemplate)).toEqual(fakeModel);
    });


    it("if there is not any model and no position is introduced, model is initialized with position equal to 0", function() {
      var model = cleanFactory.getModel(fakeModelTemplate);
      expect(factory.getContext().position).toBe(0);
    })
  });

  describe("should be able to update the input list according to the position of the model in the policy model list", function() {
    var model1, model2, model3, models = null;
    beforeEach(function() {
      model1 = angular.copy(fakeModel);
      model1.inputField = "fake inputfield of model 1";
      model1.outputFields = [{name: "model1 output1"}, {name: "model1output 2"}];
      model2 = angular.copy(fakeModel);
      model2.inputField = "fake inputfield of model 2";
      model2.outputFields = [{name: "model2 output1"}, {name: "model2 output 2"}];
      model3 = angular.copy(fakeModel);
      model3.inputField = "fake inputfield of model 3";
      model3.outputFields = [{name: "model3 output1"}, {name: "model3 output 2"}];

      models = [model1, model2, model3];
    });

    it("if model position is 0, returns an option array with the default input", function() {
      var position = 0;
      var order = 5;
      factory.resetModel(fakeModelTemplate, order, position);
      factory.updateModelInputs(models);

      var inputList = factory.getModelInputs();
      expect(inputList.length).toEqual(1);
      expect(inputList[0]).toEqual({
        label: fakeModelTemplate.defaultInput.label,
        value: fakeModelTemplate.defaultInput.value
      });

    });

    it("if model position is != 0, returns an option array with the outputs of the previous models", function() {
      var position = 2;
      var order = 5;
      factory.resetModel(fakeModelTemplate, order, position);
      factory.updateModelInputs(models);

      var inputList = factory.getModelInputs();

      expect(inputList.length).toEqual(model1.outputFields.length + model2.outputFields.length + 1);
      expect(inputList[0]).toEqual(
          fakeModelTemplate.defaultInput);
      expect(inputList[1]).toEqual({
        label: model1.outputFields[0].name,
        value: model1.outputFields[0].name
      });
      expect(inputList[2]).toEqual({
        label: model1.outputFields[1].name,
        value: model1.outputFields[1].name
      });
      expect(inputList[3]).toEqual({
        label: model2.outputFields[0].name,
        value: model2.outputFields[0].name
      });
      expect(inputList[4]).toEqual({
        label: model2.outputFields[1].name,
        value: model2.outputFields[1].name
      });
    });

    it("if input of model is empty, it is updated with the first input of the list", function() {
      var position = 1;
      var order = 5;
      factory.resetModel(fakeModelTemplate, order, position);
      factory.getModel().inputField = null;
      var updatedInputList = factory.updateModelInputs(models);

      expect(factory.getModel().inputField).toEqual(updatedInputList[0].value);

      factory.getModel().inputField = "non empty";
      updatedInputList = factory.updateModelInputs(models);

      expect(factory.getModel().inputField).not.toEqual(updatedInputList[0].value);
    });
  });

  describe("should be able to validate a model", function() {

    describe("all its attributes can not be empty", function() {

      it("if empty inputField and is not an auto generated date time or custom transformation, model is invalid", function() {
        var invalidModel = angular.copy(fakeModel);
        invalidModel.inputField = "";
        factory.setModel(invalidModel);
        expect(factory.isValidModel()).toBeFalsy();

        var validModel = angular.copy(fakeModel);
        validModel.type = modelConstants.CUSTOM;
        validModel.inputField = "";

        factory.setModel(validModel);
        expect(factory.isValidModel()).toBeTruthy();

        validModel.type = modelConstants.DATETIME;
        validModel.configuration.inputFormat = modelConstants.AUTOGENERATED;
        factory.setModel(validModel);
        expect(factory.isValidModel()).toBeTruthy();
      });

      it("if empty type, model is invalid", function() {
        var invalidModel = angular.copy(fakeModel);
        invalidModel.type = "";
        factory.setModel(invalidModel);
        expect(factory.isValidModel()).toBeFalsy();
      });

      it("if empty outputFields, model is invalid if it is not a custom or filter transformation", function() {
        var invalidModel = angular.copy(fakeModel);
        invalidModel.outputFields = [];
        factory.setModel(invalidModel);
        expect(factory.isValidModel()).toBeFalsy();

        var validModel = angular.copy(fakeModel);
        validModel.outputFields = [];
        validModel.type = modelConstants.FILTER;
        factory.setModel(validModel);
        expect(factory.isValidModel()).toBeTruthy();

        validModel.type = modelConstants.CUSTOM;

        expect(factory.isValidModel()).toBeTruthy();
      });

      it("model is valid if all its attributes are not empty", function() {
        var position = 1;
        factory.setModel(fakeModel, position);

        expect(factory.isValidModel()).toBeTruthy();
      });


      it("model is valid if it has not got an input field and is an autogenerated datetime", function() {
        var position = 1;
        fakeModel.type = "DateTime";
        fakeModel.configuration.inputFormat = "autoGenerated";
        fakeModel.inputField = "";

        factory.setModel(fakeModel, position);

        expect(factory.isValidModel()).toBeTruthy();
      });
    });

    it("should be able to reset its model to set all attributes with default values", function() {
      var oldPosition = 2;
      factory.setModel(fakeModel, oldPosition);
      var newPosition = 5;
      var desiredOrder = 5;
      factory.resetModel(fakeModelTemplate, desiredOrder, newPosition);

      var model = factory.getModel();

      expect(model.outputFields).toEqual([]);
      expect(model.type).toBe(fakeModelTemplate.types[0].name);
      expect(model.order).toBe(desiredOrder);
      expect(factory.getError()).toEqual({"text": "", "duplicatedOutput": false});
      expect(factory.getContext().position).toBe(newPosition);
    });
  });
})
;
