describe('policies.wizard.controller.new-dimension-modal-controller', function () {
  beforeEach(module('webApp'));
  beforeEach(module('template/policy.json'));

  var ctrl, modalInstanceMock, UtilsServiceMock, fakeDimensionName, fakeFieldName, fakeDimensions, fakeCubeTemplate, fakeIsNewDimension, scope = null;

  beforeEach(inject(function ($controller) {

    modalInstanceMock = jasmine.createSpyObj('$uibModalInstance', ['close', 'dismiss']);
    UtilsServiceMock = jasmine.createSpyObj('UtilsServiceMock', ['findElementInJSONArray', 'convertDottedPropertiesToJson']);

    fakeDimensionName = "fake dimension name";
    fakeFieldName = "fake field name";
    fakeDimensions = [];
    fakeIsNewDimension = true;

    spyOn(document, "querySelector").and.callFake(function () {
      return {"focus": jasmine.createSpy()}
    });

    inject(function (_templatePolicy_, $rootScope) {
      fakeCubeTemplate = _templatePolicy_.cube;
      scope = $rootScope.$new();
    });

    ctrl = $controller('NewDimensionModalCtrl', {
      '$uibModalInstance': modalInstanceMock,
      'dimensionName': fakeDimensionName,
      'fieldName': fakeFieldName,
      'dimensions': fakeDimensions,
      'UtilsService': UtilsServiceMock,
      'template': fakeCubeTemplate,
      'isTimeDimension': fakeIsNewDimension,
      '$scope': scope
    });

  }));

  it("when it is initialized it creates a dimension with the injected params", function () {
    expect(ctrl.dimension.name).toBe(fakeDimensionName);
    expect(ctrl.dimension.field).toBe(fakeFieldName);
    expect(ctrl.dimension.type).toBe(fakeCubeTemplate.types[0].value);
    expect(ctrl.defaultType).toBe(fakeCubeTemplate.types[0].value);
    expect(ctrl.error).toBe("");
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
          expect(ctrl.error).toBe("_POLICY_._CUBE_._INVALID_DIMENSION_PRECISION_");
        });

        it("if type is 'Default', precision can be empty", function () {
          ctrl.dimension.type = "Default";
          ctrl.dimension.precision = "";
          ctrl.ok();
          expect(ctrl.error).toBe("");
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

          expect(ctrl.error).toBe("");
        });

        it("name is invalid if there is another dimension with its name", function () {
          UtilsServiceMock.findElementInJSONArray.and.returnValue(2);

          ctrl.ok();

          expect(ctrl.error).toBe("_POLICY_._CUBE_._DIMENSION_NAME_EXISTS_");
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

        it("modal is closed passing dimension with its dotted properties parsed and if it is time dimension", inject(function ($controller, UtilsService) {
          ctrl = $controller('NewDimensionModalCtrl', {
            '$uibModalInstance': modalInstanceMock,
            'dimensionName': fakeDimensionName,
            'fieldName': fakeFieldName,
            'dimensions': fakeDimensions,
            'template': fakeCubeTemplate,
            'isTimeDimension': fakeIsNewDimension,
            '$scope': scope
          });
          ctrl.form = {"$valid": true};
          ctrl.dimension['configuration.typOp'] = "fakeOpt";

          ctrl.dimension['configuration.fakeDottedProperty'] = "fake dotted property";
          ctrl.ok();

          expect(modalInstanceMock.close).toHaveBeenCalled();

          expect(modalInstanceMock.close.calls.mostRecent().args[0]).toEqual({
            dimension: {
              name: 'fake dimension name',
              field: 'fake field name',
              type: 'Default',
              configuration: {typOp: 'fakeOpt', fakeDottedProperty: "fake dotted property"}
            }, isTimeDimension: fakeIsNewDimension
          });
        }));
      })
    });
  });

  it("should be able to close the modal", function () {
    ctrl.cancel();

    expect(modalInstanceMock.dismiss).toHaveBeenCalledWith('cancel');
  });
});
