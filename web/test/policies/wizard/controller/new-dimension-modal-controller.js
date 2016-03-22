describe('policies.wizard.controller.new-dimension-modal-controller', function () {
  beforeEach(module('webApp'));
  beforeEach(module('template/policy.json'));

  var ctrl, modalInstanceMock, UtilsServiceMock, fakeDimensionName, fakeFieldName, fakeDimensions, fakeCubeTemplate, fakeIsNewDimension = null;

  beforeEach(inject(function ($controller) {

    modalInstanceMock = jasmine.createSpyObj('$modalInstance', ['close', 'dismiss']);
    UtilsServiceMock = jasmine.createSpyObj('UtilsServiceMock', ['findElementInJSONArray']);

    fakeDimensionName = "fake dimension name";
    fakeFieldName = "fake field name";
    fakeDimensions = [];
    fakeIsNewDimension = true;

    spyOn(document, "querySelector").and.callFake(function () {
      return {"focus": jasmine.createSpy()}
    });

    inject(function (_templatePolicy_) {
      fakeCubeTemplate = _templatePolicy_.cube;
    });

    ctrl = $controller('NewDimensionModalCtrl', {
      '$modalInstance': modalInstanceMock,
      'dimensionName': fakeDimensionName,
      'fieldName': fakeFieldName,
      'dimensions': fakeDimensions,
      'UtilsService': UtilsServiceMock,
      'template': fakeCubeTemplate,
      'isTimeDimension': fakeIsNewDimension
    });

  }));

  it("when it is initialized it creates a dimension with the injected params", function () {
    expect(ctrl.dimension.name).toBe(fakeDimensionName);
    expect(ctrl.dimension.field).toBe(fakeFieldName);
    expect(ctrl.cubeTypes).toBe(fakeCubeTemplate.types);
    expect(ctrl.dimension.type).toBe(fakeCubeTemplate.types[0].value);
    expect(ctrl.precisionOptions).toBe(fakeCubeTemplate.precisionOptions);
    expect(ctrl.defaultType).toBe(fakeCubeTemplate.types[0].value);
    expect(ctrl.nameError).toBe("");
  });

  describe("should be able to return a precision list according to the dimension type", function () {
    it("if dimension type is null, should return an empty array", function () {
      ctrl.dimension.type = null;

      expect(ctrl.getPrecisionsOfType()).toEqual([]);
    });

    it("if dimension type is not null, should return a precision list of that type", function () {
      ctrl.dimension.type = fakeCubeTemplate.precisionOptions[0].type;
      expect(ctrl.getPrecisionsOfType()).toEqual(fakeCubeTemplate.precisionOptions[0].precisions);

      ctrl.dimension.type = fakeCubeTemplate.precisionOptions[1].type;
      expect(ctrl.getPrecisionsOfType()).toEqual(fakeCubeTemplate.precisionOptions[1].precisions);
    })
  });

  describe("should be able to accept the modal", function () {
    describe("if view validations have been passed", function () {
      beforeEach(function () {
        ctrl.form = {"$valid": true};
      });

      describe("precision is validated", function () {
        beforeEach(function () {
          UtilsServiceMock.findElementInJSONArray.and.returnValue(-1);
        });

        it("if type is not 'Default', precision can not be empty", function () {
          ctrl.dimension.type = "not default";
          ctrl.dimension.precision = "";
          ctrl.ok();
          expect(ctrl.nameError).toBe("_POLICY_._CUBE_._INVALID_DIMENSION_PRECISION_");
        });

        it("if type is 'Default', precision can be empty", function () {
          ctrl.dimension.type = "Default";
          ctrl.dimension.precision = "";
          ctrl.ok();
          expect(ctrl.nameError).toBe("");
        });
      });

      describe("name is validated", function () {
        beforeEach(function () {
          ctrl.dimension.type = "Default";
          ctrl.dimension.precision = "";
        });
        it("name is valid if there is not any dimension with irs name", function () {
          UtilsServiceMock.findElementInJSONArray.and.returnValue(-1);

          ctrl.ok();

          expect(ctrl.nameError).toBe("");
        });

        it("name is invalid if there is another dimension with its name", function () {
          UtilsServiceMock.findElementInJSONArray.and.returnValue(2);

          ctrl.ok();

          expect(ctrl.nameError).toBe("_POLICY_._CUBE_._DIMENSION_NAME_EXISTS_");
        })

      });

      describe("if precision and name are valid", function () {
        beforeEach(function () {
          ctrl.dimension.type = "Default";
          ctrl.dimension.precision = "";
          UtilsServiceMock.findElementInJSONArray.and.returnValue(-1);
        });
        describe("precision is clean", function () {
          it("if type is 'Default', precision is removed from dimension", function () {
            ctrl.dimension.type = "Default";
            ctrl.dimension.precision = "fake dimension";
            ctrl.ok();

            expect(ctrl.dimension.precision).toBe(undefined);
          });
        });

        it("modal is closed passing dimension and if it is time dimension", function () {
          ctrl.ok();

          expect(modalInstanceMock.close).toHaveBeenCalled();
          expect(modalInstanceMock.close.calls.mostRecent().args[0]).toEqual({dimension: ctrl.dimension, isTimeDimension: fakeIsNewDimension});
        });
      })
    });
  });

  it("should be able to close the modal", function () {
    ctrl.cancel();

    expect(modalInstanceMock.dismiss).toHaveBeenCalledWith('cancel');
  });
});
