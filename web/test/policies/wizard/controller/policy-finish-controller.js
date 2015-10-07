describe('policies.wizard.controller.policy-finish-controller', function () {
  beforeEach(module('webApp'));
  beforeEach(module('served/policy.json'));
  beforeEach(module('served/input.json'));
  beforeEach(module('served/output.json'));
  beforeEach(module('served/policyTemplate.json'));

  var ctrl, fakePolicy, policyModelFactoryMock, fakeInput, fakeOutput = null;

  // init mock modules

  beforeEach(inject(function ($controller, $q, $httpBackend, _servedInput_, _servedOutput_, _servedPolicy_) {
    fakePolicy = angular.copy(_servedPolicy_);
    fakeInput = _servedInput_;
    fakeOutput = _servedOutput_;

    $httpBackend.when('GET', 'languages/en-US.json')
      .respond({});

    policyModelFactoryMock = jasmine.createSpyObj('PolicyModelFactory', ['getCurrentPolicy', 'getTemplate', 'nextStep', 'setFinalJSON']);
    policyModelFactoryMock.getCurrentPolicy.and.callFake(function () {
      return fakePolicy;
    });
    policyModelFactoryMock.getTemplate.and.callFake(function () {
      return fakeTemplate;
    });

    ctrl = $controller('PolicyFinishCtrl', {
      'PolicyModelFactory': policyModelFactoryMock
    });
  }));

  describe("when it is initialized", function () {

    it('it should get the policy that is being created or edited from policy factory', function () {
      expect(ctrl.policy).toBe(fakePolicy);
    });

    describe("should update the final json with the current data in the policy to the policy factory", function () {
      var fakePath = "fake path";
      var fakePartitionFormat = "fake partition format";

      beforeEach(function () {
        fakePolicy.rawData.path = fakePath;
        fakePolicy.rawData.partitionFormat = fakePartitionFormat;
      });

      it("if raw data is not enabled, it should remove the attributes path and partitionFormat from rawData", inject(function ($controller) {
        fakePolicy.rawData.enabled = "false";

        ctrl = $controller('PolicyFinishCtrl', {
          'PolicyModelFactory': policyModelFactoryMock
        });
        var resultJSON = JSON.parse(ctrl.testingpolcyData);
        expect(resultJSON.rawData.path).toBe(undefined);
        expect(resultJSON.rawData.partitionFormat).toBe(undefined);
      }));


      it("if raw data is enabled, it should not remove the attributes path and partitionFormat from rawData", inject(function ($controller) {
        fakePolicy.rawData.enabled = "true";
        ctrl = $controller('PolicyFinishCtrl', {
          'PolicyModelFactory': policyModelFactoryMock
        });

        var resultJSON = JSON.parse(ctrl.testingpolcyData);
        expect(resultJSON.rawData.path).toBe(fakePath);
        expect(resultJSON.rawData.partitionFormat).toBe(fakePartitionFormat);
      }));


      it("should introduce in a fragment array the output list and the input of the policy", inject(function ($controller) {
        fakePolicy.rawData.enabled = "true";
        fakePolicy.input = fakeInput;
        var fakeOutput2 = angular.copy(fakeOutput);
        fakeOutput2.name = "fake output 2";
        fakePolicy.outputs = [fakeOutput, fakeOutput2];

        var expectedFragments = [fakeInput, fakeOutput, fakeOutput2];
        ctrl = $controller('PolicyFinishCtrl', {
          'PolicyModelFactory': policyModelFactoryMock
        });

        var resultJSON = JSON.parse(ctrl.testingpolcyData);
        expect(resultJSON.fragments).toEqual(expectedFragments);
      }));


    })
  });


});
