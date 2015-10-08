describe('direcive.c-add-element-directive', function () {
  beforeEach(module('webApp'));
  var directive, scope, fakeModel, fakeInputToAdd = null;

  beforeEach(inject(function ($httpBackend, $rootScope, $compile) {
    $httpBackend.when('GET', 'languages/en-US.json')
      .respond({});
    var fakeIconClass = "'fake class'";
    var fakeText = "'fake text'";
    var fakeDataQA = "'fake data QA'";
    var fakePattern = "[a-zA-Z0-9]*";
    var fakeModelError = {"text": ""};
    var fakePlaceholder = "'fake placeholder'";
    fakeInputToAdd = "fake input to add";
    fakeModel = [];
    $httpBackend.when('GET', 'templates/components/c-add-element.tpl.html')
      .respond("<div></div>");

    scope = $rootScope.$new();

    scope.model = fakeModel;
    directive = angular.element(' <c-add-element model="model"' +
      ' icon-class="' + fakeIconClass + '" type="' + fakeText + '" data-qa="' + fakeDataQA + '" data-pattern="\'' + fakePattern + '\'"' +
      '  data-error="' + fakeModelError.text + "\"" +
      '  data-placeholder="' + fakePlaceholder + '">      </c-add-element>');

    directive = $compile(directive)(scope);
    scope.$digest();
    $httpBackend.flush();
  }));

  describe("should be able to add the new input to the array introduced as model", function () {
    var isolatedScope = null;

    describe("if the input to add has been fill out by user", function () {
      beforeEach(function () {
        isolatedScope = directive.isolateScope();
        isolatedScope.inputToAdd = "fake input to add";
      });
      it("it is added when user press the enter key", function () {
        var fakeEvent = {
          "keyCode": '13', preventDefault: function () {
          }
        };
        isolatedScope.addInput(fakeEvent);
        expect(isolatedScope.model.length).toBe(1);

        fakeEvent.keyCode = '15'; // key different to enter key
        isolatedScope.addInput(fakeEvent);
        expect(isolatedScope.model.length).toBe(1); // input is not added
      });

      it("it is added when user press a button which has been implemented to add the input", function () {
        var fakeEvent = {
          "type": "click", preventDefault: function () {
          }
        };
        var previousLength = isolatedScope.model.length;
        isolatedScope.addInput(fakeEvent);
        expect(isolatedScope.model.length).toBe(previousLength + 1);
      });

      it("it is not added if the model array contains the element already", inject(function ($compile) {
        var fakeEvent = {
          "type": "click", preventDefault: function () {
          }
        };
        isolatedScope = directive.isolateScope();
        isolatedScope.inputToAdd = fakeInputToAdd;

        fakeModel.push(fakeInputToAdd);
        var previousLength = fakeModel.length;

        // user tries to add the same element again
        isolatedScope.addInput(fakeEvent);

        expect(isolatedScope.model.length).toBe(previousLength); // the same length because element has not been added
      }));

      it("it is added if the model array does not contain the element already", inject(function ($compile) {
        var fakeEvent = {
          "type": "click", preventDefault: function () {
          }
        };
        var newInputToAdd = "new input";

        isolatedScope = directive.isolateScope();
        isolatedScope.inputToAdd = fakeInputToAdd;
        fakeModel.push(newInputToAdd);
        var previousLength = fakeModel.length;

        // user tries to add another element
        isolatedScope.addInput(fakeEvent);

        expect(isolatedScope.model.length).toBe(previousLength+1); // the same length because element has not been added
      }));
    });
  });

});
