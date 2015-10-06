describe('policies.wizard.controller.new-operator-modal-controller', function () {
  beforeEach(module('webApp'));
  beforeEach(module('served/policyTemplate.json'));

  var ctrl, modalInstanceMock, UtilsServiceMock, fakeOperatorName, fakeOperatorType, fakeFieldName, fakeOperators, fakePolicyTemplate = null;

  beforeEach(inject(function ($controller) {

    modalInstanceMock = jasmine.createSpyObj('$modalInstance', ['close', 'dismiss']);
    UtilsServiceMock = jasmine.createSpyObj('UtilsServiceMock', ['findElementInJSONArray']);

    fakeOperatorName = "fake operator name";
    fakeOperatorType = "fake operator type";
    fakeFieldName = "fake field name";
    fakeOperators = [];

    inject(function (_servedPolicyTemplate_) {
      fakePolicyTemplate = _servedPolicyTemplate_;
    });

    ctrl = $controller('NewOperatorModalCtrl', {
      '$modalInstance': modalInstanceMock,
      'operatorName': fakeOperatorName,
      'operatorType': fakeOperatorType,
      'operators': fakeOperators,
      'UtilsService': UtilsServiceMock,
      'template': fakePolicyTemplate
    });

  }));

  it("when it is initialized it creates a operator with the injected params and a default configuration", function () {
    expect(ctrl.operator.name).toBe(fakeOperatorName);
    expect(ctrl.operator.configuration).toEqual(JSON.stringify(fakePolicyTemplate.defaultOperatorConfiguration, null, 4));
    expect(ctrl.operator.type).toBe(fakeOperatorType);
    expect(ctrl.configHelpLink).toBe(fakePolicyTemplate.configurationHelpLink);
    expect(ctrl.error).toBeFalsy();
    expect(ctrl.errorText).toBe("");
  });

  describe("should be able to accept the modal", function () {
    it("if view validations have not been passed, generic form error is shown", function () {
      ctrl.form = {"$valid": false};
      ctrl.ok();

      expect(ctrl.errorText).toBe("_GENERIC_FORM_ERROR_");
    });

    describe("if view validations have been passed", function () {
      beforeEach(function () {
        ctrl.form = {"$valid": true};
      });

      describe("name is validated", function () {
        it("name is valid if there is not any operator with irs name", function () {
          UtilsServiceMock.findElementInJSONArray.and.returnValue(-1);

          ctrl.ok();

          expect(ctrl.errorText).toBe("");
        });

        it("name is invalid if there is another operator with irs name", function () {
          UtilsServiceMock.findElementInJSONArray.and.returnValue(2);

          ctrl.ok();

          expect(ctrl.errorText).toBe("_POLICY_._CUBE_._OPERATOR_NAME_EXISTS_");
        })

      });

      describe("configuration is validated", function () {
        beforeEach(function () {
          ctrl.operator.name = "fake operator name";
          UtilsServiceMock.findElementInJSONArray.and.returnValue(-1);
        });

        it("configuration is valid if it can be parsed", function () {
          ctrl.operator.configuration = '{"valid_key": "valid value"}';
          ctrl.ok();

          expect(modalInstanceMock.close).toHaveBeenCalledWith(ctrl.operator);
        });

        it("configuration is invalid if it can not be parsed", function () {
          ctrl.operator.configuration = '{invalid json}';

          ctrl.ok();

          expect(ctrl.errorText).toBe("_POLICY_._CUBE_._INVALID_CONFIG_");
          expect(modalInstanceMock.close).not.toHaveBeenCalledWith(ctrl.operator);

        });
      });
    });
  });

  it("should be able to close the modal", function () {
    ctrl.cancel();

    expect(modalInstanceMock.dismiss).toHaveBeenCalledWith('cancel');
  });
});
