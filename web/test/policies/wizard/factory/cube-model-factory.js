describe('policies.wizard.factory.cube-model-factory', function () {
  beforeEach(module('webApp'));
  beforeEach(module('served/cube.json'));
  beforeEach(module('served/policyTemplate.json'));

  var factory, fakeCube, UtilsServiceMock, fakePolicyTemplate = null;

  beforeEach(module(function ($provide) {
    UtilsServiceMock = jasmine.createSpyObj('UtilsService', ['removeItemsFromArray', 'findElementInJSONArray']);

    // inject mocks
    $provide.value('UtilsService', UtilsServiceMock);
  }));

  beforeEach(inject(function (_CubeModelFactory_, _servedCube_, _servedPolicyTemplate_) {
    factory = _CubeModelFactory_;
    fakeCube = _servedCube_;
    fakePolicyTemplate = _servedPolicyTemplate_;
  }));

  it("should be able to load a cube from a json and a position", function () {
    var position = 0;
    factory.setCube(fakeCube, position);
    var cube = factory.getCube();
    expect(cube.name).toBe(fakeCube.name);
    expect(cube.dimensions).toBe(fakeCube.dimensions);
    expect(cube.operators).toBe(fakeCube.operators);
    expect(cube.checkpointConfig).toBe(fakeCube.checkpointConfig);
    expect(factory.getError()).toEqual({"text": ""});
    expect(factory.getContext().position).toBe(position);
  });

  describe("should be able to update the cube error", function () {
    var validCube = null;
    beforeEach(function(){
       validCube = angular.copy(fakeCube);
        validCube.operators = [{}];
        validCube.dimensions = [{}];
    });
    it ("if there is not a operator, then set a operator error ", function(){
      validCube.operators = [];
      factory.setCube(validCube, 0);

      factory.setError();

       expect(factory.getError()).toEqual({"text": "_POLICY_CUBE_OPERATOR_ERROR_"});
    });

      it ("if there is not a dimension, then set a dimension error ", function(){
      validCube.dimensions = [];
      factory.setCube(validCube, 0);

      factory.setError();

       expect(factory.getError()).toEqual({"text": "_POLICY_CUBE_DIMENSION_ERROR_"});
    });

      it ("if there is neither a dimension nor operator, then set a dimension and operator error ", function(){
       validCube.operators = [];
      validCube.dimensions = [];
      factory.setCube(validCube, 0);

      factory.setError();

       expect(factory.getError()).toEqual({"text": "_POLICY_CUBE_OPERATOR-DIMENSION_ERROR_"});
    });
  });

  it("should be able to update the position of the cube", function () {
    var position = 2;
    factory.setPosition(position);
    expect(factory.getContext().position).toEqual(position);
  });

  describe("should return its current cube", function () {
    var cleanFactory = null;
    beforeEach(inject(function (_CubeModelFactory_) {
      cleanFactory = _CubeModelFactory_; // inject a new factory in each test to can check the initial state of the factory when it is created
    }));

    it("if there is not any cube, it initializes a new one using the introduced template a position", function () {
      var desiredOrder = 0;
      var cube = cleanFactory.getCube(fakePolicyTemplate, desiredOrder);
      expect(cube.name).toEqual(fakePolicyTemplate.defaultCubeName + (desiredOrder + 1));
      expect(cube.dimensions).toEqual([]);
      expect(cube.operators).toEqual([]);
      expect(cube.checkpointConfig.timeDimension).toEqual(fakePolicyTemplate.defaultTimeDimension);
      expect(cube.checkpointConfig.interval).toEqual(fakePolicyTemplate.defaultInterval);
      expect(cube.checkpointConfig.timeAvailability).toEqual(fakePolicyTemplate.defaultTimeAvailability);
      expect(cube.checkpointConfig.granularity).toEqual(fakePolicyTemplate.defaultGranularity);
      expect(cleanFactory.getError()).toEqual({"text": ""});
      expect(factory.getContext().position).toBe(desiredOrder);
    });

    it("if there is a cube, returns that cube", function () {
      var desiredOrder = 0;
      factory.setCube(fakeCube, desiredOrder);

      expect(factory.getCube(fakePolicyTemplate)).toEqual(fakeCube);
    });


    it("if there is not any cube and no position is introduced, cube is initialized with position equal to 0", function () {
      var cube = cleanFactory.getCube(fakePolicyTemplate);
      expect(factory.getContext().position).toBe(0);
    })
  });

  describe("should be able to validate a cube", function () {

    describe("all its attributes can not be empty", function () {
      beforeEach(function () {
        UtilsServiceMock.findElementInJSONArray.and.returnValue(-1); //not found in the array
      });

      it("if empty name, cube is invalid", function () {
        var invalidCube = angular.copy(fakeCube);
        invalidCube.name = "";

        expect(factory.isValidCube(invalidCube, {})).toBeFalsy();
      });

      it("if empty dimensions, cube is invalid", function () {
        var invalidCube = angular.copy(fakeCube);
        invalidCube.dimensions = [];

        expect(factory.isValidCube(invalidCube, {})).toBeFalsy();
      });

      it("if empty operators, cube is invalid", function () {
        var invalidCube = angular.copy(fakeCube);
        invalidCube.operators = [];

        expect(factory.isValidCube(invalidCube, {})).toBeFalsy();
      });

      it("if some of the checkpointConfig attributes are empty, cube is invalid", function () {
        var invalidCube = angular.copy(fakeCube);
        invalidCube.checkpointConfig = {};
        expect(factory.isValidCube(invalidCube, {})).toBeFalsy();

        invalidCube.checkpointConfig = angular.copy(fakeCube.checkpointConfig);
        invalidCube.checkpointConfig.timeDimension = "";
        expect(factory.isValidCube(invalidCube, {})).toBeFalsy();

        invalidCube.checkpointConfig = angular.copy(fakeCube.checkpointConfig);
        invalidCube.checkpointConfig.interval = null;
        expect(factory.isValidCube(invalidCube, {})).toBeFalsy();

        invalidCube.checkpointConfig = angular.copy(fakeCube.checkpointConfig);
        invalidCube.checkpointConfig.timeAvailability = null;
        expect(factory.isValidCube(invalidCube, {})).toBeFalsy();

        invalidCube.checkpointConfig = angular.copy(fakeCube.checkpointConfig);
        invalidCube.checkpointConfig.granularity = "";
        expect(factory.isValidCube(invalidCube, {})).toBeFalsy();
      });

      it("cube is valid if all its attributes are not empty", function () {
        var desiredOrder = 0;
        factory.setCube(fakeCube, desiredOrder);

        expect(factory.isValidCube(fakeCube, {})).toBeTruthy();
      });

    });

    describe("cube name can not be repeated", function () {

      it("if cube list is empty, cube is valid with any name", function () {
        var cubeList = [];
        var newName = "new cube name";
        var cube = angular.copy(fakeCube);
        cube.name = newName;
        expect(factory.isValidCube(cube, cubeList));
      });

      describe("if cube list is not empty", function () {
        var cubeList = null;
        beforeEach(function () {
          var fakeCube2 = angular.copy(fakeCube);
          fakeCube2.name = "fake cube 2";
          cubeList = [fakeCube2, fakeCube];
        });

        it("and it has a cube with the same name and its position is not the same that the introduced one, cube is invalid", function () {
          UtilsServiceMock.findElementInJSONArray.and.returnValue(1);
          expect(factory.isValidCube(fakeCube, cubeList, 2)).toBeFalsy();
        });

        it("and it has a cube with the same name and its position is the same that the introduced one, cube is valid", function () {
          //the cube that is being validated and the found cube in the list are the same cube
          UtilsServiceMock.findElementInJSONArray.and.returnValue(1);
          expect(factory.isValidCube(fakeCube, cubeList, 1)).toBeTruthy();
        });

        it("but it has not any cube with the same name, cube is valid", function () {
          UtilsServiceMock.findElementInJSONArray.and.returnValue(-1);
          var validCube = angular.copy(fakeCube);
          validCube.name = "new cube name";
          expect(factory.isValidCube(validCube, cubeList, 2)).toBeTruthy();
        });
      });

    });

    it("should be able to reset its cube to set all attributes with default values", function () {
      var oldPosition = 2;
      factory.setCube(fakeCube, oldPosition);
      var newPosition = 5;
      var nameIndex = 15;
      factory.resetCube(fakePolicyTemplate, nameIndex, newPosition);

      var cube = factory.getCube(fakePolicyTemplate, newPosition);
      expect(cube.name).toEqual(fakePolicyTemplate.defaultCubeName + (nameIndex + 1));
      expect(cube.dimensions).toEqual([]);
      expect(cube.operators).toEqual([]);
      expect(cube.checkpointConfig.timeDimension).toEqual(fakePolicyTemplate.defaultTimeDimension);
      expect(cube.checkpointConfig.interval).toEqual(fakePolicyTemplate.defaultInterval);
      expect(cube.checkpointConfig.timeAvailability).toEqual(fakePolicyTemplate.defaultTimeAvailability);
      expect(cube.checkpointConfig.granularity).toEqual(fakePolicyTemplate.defaultGranularity);
      expect(factory.getError()).toEqual({"text": ""});
      expect(factory.getContext().position).toBe(newPosition);
    });
  });
})
;
