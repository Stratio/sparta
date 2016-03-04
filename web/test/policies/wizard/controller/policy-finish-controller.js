describe('policies.wizard.controller.policy-finish-controller', function () {
  beforeEach(module('webApp'));
  beforeEach(module('served/policy.json'));
  beforeEach(module('served/input.json'));
  beforeEach(module('served/output.json'));
  beforeEach(module('served/trigger.json'));
  beforeEach(module('served/cube.json'));
  beforeEach(module('served/policyTemplate.json'));

  var ctrl, rootScope, fakePolicy, fakeCube, policyModelFactoryMock, outputServiceMock, utilsServiceMock, fakeInput, fakeOutput, fakeTrigger = null;

  // init mock modules

  beforeEach(inject(function ($controller, $q, $httpBackend, _servedInput_, _servedOutput_, _servedPolicy_, _servedTrigger_, _servedCube_, $rootScope) {
    fakePolicy = angular.copy(_servedPolicy_);
    fakeInput = _servedInput_;
    fakeOutput = _servedOutput_;
    fakeTrigger = _servedTrigger_;
    rootScope = $rootScope;
    fakeCube = _servedCube_;
    $httpBackend.when('GET', 'languages/en-US.json')
      .respond({});

    policyModelFactoryMock = jasmine.createSpyObj('PolicyModelFactory', ['getCurrentPolicy', 'getTemplate', 'nextStep', 'setFinalJSON', 'previousStep']);
    policyModelFactoryMock.getCurrentPolicy.and.callFake(function () {
      return fakePolicy;
    });
    policyModelFactoryMock.getTemplate.and.callFake(function () {
      return fakeTemplate;
    });

    outputServiceMock = jasmine.createSpyObj('OutputService', ['getOutputList']);
    outputServiceMock.getOutputList.and.callFake(function () {
      var defer = $q.defer();
      defer.resolve([fakeOutput]);

      return defer.promise;
    });

    utilsServiceMock = jasmine.createSpyObj('UtilsService', ['getFilteredJSONByArray', 'removeDuplicatedJSONs', 'convertDottedPropertiesToJson']);
    utilsServiceMock.convertDottedPropertiesToJson.and.callFake(function (json) {
      return json;
    });

    utilsServiceMock.getFilteredJSONByArray.and.callFake(function (json) {
      return json;
    });

    utilsServiceMock.removeDuplicatedJSONs.and.callFake(function (json) {
      return json;
    });
    ctrl = $controller('PolicyFinishCtrl', {
      'PolicyModelFactory': policyModelFactoryMock,
      'OutputService': outputServiceMock,
      'UtilsService': utilsServiceMock
    });
    rootScope.$digest();
  }));

  describe("when it is initialized", function () {

    it('it should get the policy that is being created or edited from policy factory', function () {
      expect(ctrl.policy).toBe(fakePolicy);
    });

    describe("should update the final json with the current data in the policy to the policy factory", function () {
      var fakePath = "fake path";
      var fakePartitionFormat = "fake partition format";

      beforeEach(function () {
        fakePolicy.rawDataPath = fakePath;
      });

      it("if raw data is not enabled, it should remove the attribute path from rawData", inject(function ($controller) {
        fakePolicy.rawDataEnabled = false;

        ctrl = $controller('PolicyFinishCtrl', {
          'PolicyModelFactory': policyModelFactoryMock,
          'OutputService': outputServiceMock,
          'UtilsService': utilsServiceMock
        });
        rootScope.$digest();

        var resultJSON = JSON.parse(ctrl.policyJson);
        expect(resultJSON.rawData.path).toBe(undefined);
      }));


      it("if raw data is enabled, it should not remove the attribute path from rawData", inject(function ($controller) {
        fakePolicy.rawDataEnabled = true;
        fakePolicy.rawDataPath = fakePath;
        ctrl = $controller('PolicyFinishCtrl', {
          'PolicyModelFactory': policyModelFactoryMock,
          'OutputService': outputServiceMock,
          'UtilsService': utilsServiceMock
        });
        rootScope.$digest();
        var resultJSON = JSON.parse(ctrl.policyJson);
        expect(resultJSON.rawData.path).toBe(fakePath);
      }));

      it("rawData attribute is converted to the expected format", inject(function ($controller) {
        ctrl.policy.rawDataEnabled = false;
        ctrl = $controller('PolicyFinishCtrl', {
          'PolicyModelFactory': policyModelFactoryMock,
          'OutputService': outputServiceMock,
          'UtilsService': utilsServiceMock
        });
        rootScope.$digest();
        // raw data path is null if raw data is disabled
        var resultJSON = JSON.parse(ctrl.policyJson);
        expect(resultJSON.rawData.path).toBe(undefined);

        // temporal attributes are removed
        expect(ctrl.policyJson.rawDataPath).toBe(undefined);
        expect(ctrl.policyJson.rawDataEnabled).toBe(undefined);

        ctrl.policy.rawDataEnabled = true;
        var fakeRawDataPath = "fake/path";
        ctrl.policy.rawDataPath = fakeRawDataPath;
        ctrl = $controller('PolicyFinishCtrl', {
          'PolicyModelFactory': policyModelFactoryMock,
          'OutputService': outputServiceMock,
          'UtilsService': utilsServiceMock
        });
        rootScope.$digest();
        var resultJSON = JSON.parse(ctrl.policyJson);
        expect(resultJSON.rawData.path).toBe(fakeRawDataPath);
      }));

      describe("should introduce in a fragment array the input of the policy and outputs used in cubes and triggers", function () {

        it("Input is added at first place", inject(function ($controller) {
          fakePolicy.rawDataEnabled = true;
          fakePolicy.input = fakeInput;
          ctrl = $controller('PolicyFinishCtrl', {
            'PolicyModelFactory': policyModelFactoryMock,
            'OutputService': outputServiceMock,
            'UtilsService': utilsServiceMock
          });
          rootScope.$digest();
          var resultJSON = JSON.parse(ctrl.policyJson);
          expect(resultJSON.fragments[0]).toEqual(fakeInput);
        }));
      });

      it("outputs of cube triggers and transformations are added after policy input", inject(function ($controller) {
        fakePolicy.rawDataEnabled = true;
        fakePolicy.input = fakeInput;
        fakeCube.triggers = [fakeTrigger];

        fakePolicy.cubes[0] = fakeCube;


        fakePolicy.streamTriggers[0] = {outputs: ["output1", "output2", "output3"]};
        ctrl = $controller('PolicyFinishCtrl', {
          'PolicyModelFactory': policyModelFactoryMock,
          'OutputService': outputServiceMock,
          'UtilsService': utilsServiceMock
        });
        rootScope.$digest();
        var resultJSON = JSON.parse(ctrl.policyJson);

        var expectedLength = 1 + fakePolicy.cubes[0].writer.outputs.length + fakeCube.triggers[0].outputs.length + fakePolicy.streamTriggers[0].outputs.length;
//TODO Fix test
        //expect(resultJSON.fragments.length).toEqual(expectedLength);
      }));
    });

    it("should clean input key", inject(function ($controller) {
      fakePolicy.rawDataEnabled = true;
      fakePolicy.input = fakeInput;

      ctrl = $controller('PolicyFinishCtrl', {
        'PolicyModelFactory': policyModelFactoryMock,
        'OutputService': outputServiceMock,
        'UtilsService': utilsServiceMock
      });
      rootScope.$digest();
      var resultJSON = JSON.parse(ctrl.policyJson);
      expect(resultJSON.input).toEqual(undefined);
    }));
  })


})
;
