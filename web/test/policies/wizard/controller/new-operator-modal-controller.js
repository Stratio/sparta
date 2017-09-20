describe('policies.wizard.controller.new-operator-modal-controller', function() {
  beforeEach(module('webApp'));
  beforeEach(module('template/policy.json'));
  beforeEach(module('template/operator/default.json'));

  var ctrl, scope, _cubeConstants, modalInstanceMock, UtilsServiceMock, TemplateFactoryMock, fakeOperatorName, fakeOperatorType,
      fakeFieldName, fakeOperators, fakeCubeTemplate, fakeInputFieldList, fakeOptionList, fakeOperatorTemplate = null;

  beforeEach(inject(function($controller, $q, $rootScope, $httpBackend, cubeConstants) {

    _cubeConstants = cubeConstants;
    scope = $rootScope.$new();
    modalInstanceMock = jasmine.createSpyObj('$uibModalInstance', ['close', 'dismiss']);
    UtilsServiceMock = jasmine.createSpyObj('UtilsServiceMock', ['findElementInJSONArray', 'generateOptionListFromStringArray', 'filterByAttribute', 'convertDottedPropertiesToJson']);
    TemplateFactoryMock = jasmine.createSpyObj('TemplateFactory', ['getOperatorTemplateByType']);
    fakeOptionList = [{name: "fake option 1", value: "fake option value"}, {
      name: "fake option 2",
      value: "fake option value 2"
    }];
    $httpBackend.when('GET', "languages/en-US.json").respond({});

    UtilsServiceMock.generateOptionListFromStringArray.and.returnValue(fakeOptionList);
    TemplateFactoryMock.getOperatorTemplateByType.and.callFake(function() {
      var defer = $q.defer();
      defer.resolve(fakeOperatorTemplate);
      return defer.promise;
    });
    fakeOperatorName = "fake operator name";
    fakeOperatorType = _cubeConstants.COUNT;
    fakeFieldName = "fake field name";
    fakeOperators = [];
    fakeInputFieldList = ["input field 1", "input field 2", "input field 3"];

    inject(function(_templatePolicy_, _templateOperatorDefault_) {
      fakeCubeTemplate = _templatePolicy_.cube;
      fakeOperatorTemplate = _templateOperatorDefault_;
    });
    ctrl = $controller('NewOperatorModalCtrl', {
      '$uibModalInstance': modalInstanceMock,
      'operatorName': fakeOperatorName,
      'operatorType': fakeOperatorType,
      'operators': fakeOperators,
      'UtilsService': UtilsServiceMock,
      'template': fakeCubeTemplate,
      'inputFieldList': fakeInputFieldList,
      'TemplateFactory': TemplateFactoryMock,
      'scope': scope
    });

    spyOn(document, "querySelector").and.callFake(function() {
      return {"focus": jasmine.createSpy()}
    });
  }));

  describe("when it is initialized", function() {
    it('it retrieves its template according to its type', function() {
      scope.$apply();
      expect(TemplateFactoryMock.getOperatorTemplateByType).toHaveBeenCalledWith(fakeOperatorType);
      expect(ctrl.template).toBe(fakeOperatorTemplate);
    });
    it("it creates a operator with the injected params", function() {
      expect(ctrl.operator.name).toBe(fakeOperatorName);
      expect(ctrl.operator.configuration).toEqual({});
      expect(ctrl.operator.type).toBe(fakeOperatorType);
      expect(ctrl.configHelpLink).toBe(fakeCubeTemplate.configurationHelpLink);
      expect(ctrl.error).toBeFalsy();
      expect(ctrl.nameError).toBe("");
    });
  });

  describe("should be able to accept the modal", function() {
    describe("if view validations have been passed", function() {
      beforeEach(function() {
        ctrl.form = {"$valid": true};
      });

      describe("name is validated", function() {
        it("name is valid if there is not any operator with its name", function() {
          UtilsServiceMock.findElementInJSONArray.and.returnValue(-1);

          ctrl.ok();

          expect(ctrl.nameError).toBe("");
        });

        it("name is invalid if there is another operator with its name", function() {
          UtilsServiceMock.findElementInJSONArray.and.returnValue(2);

          ctrl.ok();

          expect(ctrl.nameError).toBe("_POLICY_._CUBE_._OPERATOR_NAME_EXISTS_");
        })

      });

      it("input field is cleaned if it is empty", function() {
        ctrl.operator['configuration.inputField'] = fakeInputFieldList[0].value;
        ctrl.ok();

        expect(ctrl.operator['configuration.inputField']).toEqual(fakeInputFieldList[0].value);

        ctrl.operator['configuration.inputField'] = "";
        ctrl.ok();

        expect(ctrl.operator['configuration.inputField']).toBeUndefined();
      });

      it ("dotted configuration properties are parsed when modal is closed", inject(function($controller, UtilsService) {
        ctrl = $controller('NewOperatorModalCtrl', {
          '$uibModalInstance': modalInstanceMock,
          'operatorName': fakeOperatorName,
          'operatorType': fakeOperatorType,
          'operators': fakeOperators,
          'template': fakeCubeTemplate,
          'inputFieldList': fakeInputFieldList,
          'TemplateFactory': TemplateFactoryMock,
          'scope': scope
        });
        ctrl.form = {"$valid": true};
        ctrl.operator['configuration.inputField'] = fakeInputFieldList[0].value;

        spyOn(UtilsService, "findElementInJSONArray").and.returnValue(-1); // no repeated
        spyOn(UtilsService, "convertDottedPropertiesToJson").and.callThrough();

        ctrl.ok();

        var returnedOperator = UtilsService.convertDottedPropertiesToJson.calls.mostRecent().args[0];

        expect(UtilsService.convertDottedPropertiesToJson).toHaveBeenCalledWith(ctrl.operator);
        expect(returnedOperator.configuration).toEqual({"inputField": fakeInputFieldList[0].value});
      }));
    });
  });

  it("should be able to close the modal", function() {
    ctrl.cancel();

    expect(modalInstanceMock.dismiss).toHaveBeenCalledWith('cancel');
  });

  it("should return if a operator is a count", function() {
    ctrl.operator.type = 'avg';
    expect(ctrl.isCount()).toBeFalsy();

    ctrl.operator.type = 'Count';
    expect(ctrl.isCount()).toBeTruthy();
  })
});
