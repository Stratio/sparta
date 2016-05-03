describe('policies.wizard.controller.new-operator-modal-controller', function () {
  beforeEach(module('webApp'));
  beforeEach(module('template/policy.json'));

  var ctrl, modalInstanceMock, UtilsServiceMock, fakeOperatorName, fakeOperatorType, fakeFieldName, fakeOperators, fakeCubeTemplate, fakeInputFieldList, fakeOptionList = null;

  beforeEach(inject(function ($controller) {

    modalInstanceMock = jasmine.createSpyObj('$modalInstance', ['close', 'dismiss']);
    UtilsServiceMock = jasmine.createSpyObj('UtilsServiceMock', ['findElementInJSONArray', 'generateOptionListFromStringArray']);
    fakeOptionList = [{name: "fake option 1", value: "fake option value"}, {
      name: "fake option 2",
      value: "fake option value 2"
    }];
    UtilsServiceMock.generateOptionListFromStringArray.and.returnValue(fakeOptionList);
    fakeOperatorName = "fake operator name";
    fakeOperatorType = "fake operator type";
    fakeFieldName = "fake field name";
    fakeOperators = [];
    fakeInputFieldList = ["input field 1", "input field 2", "input field 3"];

    inject(function (_templatePolicy_) {
      fakeCubeTemplate = _templatePolicy_.cube;
    });

    ctrl = $controller('NewOperatorModalCtrl', {
      '$modalInstance': modalInstanceMock,
      'operatorName': fakeOperatorName,
      'operatorType': fakeOperatorType,
      'operators': fakeOperators,
      'UtilsService': UtilsServiceMock,
      'template': fakeCubeTemplate,
      'inputFieldList': fakeInputFieldList
    });

    spyOn(document, "querySelector").and.callFake(function () {
      return {"focus": jasmine.createSpy()}
    });
  }));

  describe("when it is initialized", function () {
    it("it creates a operator with the injected params", function () {
      expect(ctrl.operator.name).toBe(fakeOperatorName);
      expect(ctrl.operator.configuration).toEqual({});
      expect(ctrl.operator.type).toBe(fakeOperatorType);
      expect(ctrl.configHelpLink).toBe(fakeCubeTemplate.configurationHelpLink);
      expect(ctrl.error).toBeFalsy();
      expect(ctrl.nameError).toBe("");
    });

    it("it generates an option list of input fields", function () {
      expect(ctrl.inputFieldList).toBe(fakeOptionList);
    });
  });

  describe("should be able to accept the modal", function () {
    describe("if view validations have been passed", function () {
      beforeEach(function () {
        ctrl.form = {"$valid": true};
      });

      describe("name is validated", function () {
        it("name is valid if there is not any operator with irs name", function () {
          UtilsServiceMock.findElementInJSONArray.and.returnValue(-1);

          ctrl.ok();

          expect(ctrl.nameError).toBe("");
        });

        it("name is invalid if there is another operator with irs name", function () {
          UtilsServiceMock.findElementInJSONArray.and.returnValue(2);

          ctrl.ok();

          expect(ctrl.nameError).toBe("_POLICY_._CUBE_._OPERATOR_NAME_EXISTS_");
        })

      });

      it("input field is cleaned if it is empty", function(){
        ctrl.operator.configuration.inputField = fakeInputFieldList[0].value;
        ctrl.ok();

        expect(ctrl.operator.configuration.inputField).toEqual(fakeInputFieldList[0].value);

        ctrl.operator.configuration.inputField = "";
        ctrl.ok();

        expect(ctrl.operator.configuration.inputField).toBeUndefined();
      })
    });
  });

  it("should be able to close the modal", function () {
    ctrl.cancel();

    expect(modalInstanceMock.dismiss).toHaveBeenCalledWith('cancel');
  });

  it("should return if a operator is a count", function () {
   ctrl.operator.type = 'avg';
    expect(ctrl.isCount()).toBeFalsy();

    ctrl.operator.type = 'Count';
    expect(ctrl.isCount()).toBeTruthy();
  })
});
