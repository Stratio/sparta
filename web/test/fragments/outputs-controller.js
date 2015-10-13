describe('com.stratio.sparkta.fragments.outputs.controller', function () {
	beforeEach(module('webApp'));
	beforeEach(module('served/output.json'));
	beforeEach(module('served/outputDuplicated.json'));
  beforeEach(module('served/outputList.json'));
  beforeEach(module('served/outputTemplate.json'));
  beforeEach(module('served/policyList.json'));

  var ctrl, scope, filter, modalMock, fragmentFactoryMock, fakeOutputList, fakeNewOutputTemplate, fakePolicyList, fakeOutput, fakeInputDuplicated = null;

	beforeEach(inject(function ($controller, $q, $rootScope, $httpBackend, $filter, _servedOutputList_, _servedOutputTemplate_, _servedPolicyList_, _servedOutput_, _servedOutputDuplicated_){
		scope = $rootScope;
    fakeOutputList = _servedOutputList_;
    fakeNewOutputTemplate = _servedOutputTemplate_;
    fakePolicyList = _servedPolicyList_;
    fakeOutput = _servedOutput_;
    fakeOutputDuplicated = _servedOutputDuplicated_;

    resolvedOutputListPromise = function () {
      var defer = $q.defer();
      defer.resolve(fakeOutputList);

      return defer.promise;
    };

    rejectedOutputListPromise = function () {
      var defer = $q.defer();
      defer.reject({"data": {"i18nCode": "111"}});

      return defer.promise;
    };

    resolvedNewOutputTemplate = function () {
      var defer = $q.defer();
      defer.resolve(fakeNewOutputTemplate);

      return defer.promise;
    };

    resolvedNewOutput = function () {
      var defer = $q.defer();
      defer.resolve(fakeOutput);

      return {"result": defer.promise};
    };

    resolvedEditOutput = function () {
      var defer = $q.defer();
      defer.resolve({"index": 1, "data": fakeOutput});

      return {"result": defer.promise};
    };

    resolvedDeleteOutput = function () {
      var defer = $q.defer();
      defer.resolve({"index": 2});

      return {"result": defer.promise};
    };

    resolvedDuplicatedOutput = function () {
    	var defer = $q.defer();
			defer.resolve(fakeOutputDuplicated);

			return {"result": defer.promise};
    };

    resolverPolicyList = function() {
    	var defer = $q.defer();
    	defer.resolve(fakePolicyList);

      return defer.promise;
    };

    $httpBackend.when('GET', 'languages/en-US.json').respond({});

    fragmentFactoryMock = jasmine.createSpyObj('FragmentFactory', ['getFragments']);
    fragmentFactoryMock.getFragments.and.callFake(resolvedOutputListPromise);

    utilsServiceMock = jasmine.createSpyObj('UtilsService', ['getNamesJSONArray', 'autoIncrementName']);

    modalMock = jasmine.createSpyObj('$modal', ['open']);

    templateFactoryMock = jasmine.createSpyObj('TemplateFactory', ['getNewFragmentTemplate']);
    templateFactoryMock.getNewFragmentTemplate.and.callFake(resolvedNewOutputTemplate);

    policyFactoryMock = jasmine.createSpyObj('PolicyFactory', ['getPolicyByFragmentId']);
    policyFactoryMock.getPolicyByFragmentId.and.callFake(resolverPolicyList);

    filter = $filter;

    ctrl = $controller('OutputsCtrl', {
      'FragmentFactory': fragmentFactoryMock,
      '$filter': filter,
      '$modal': modalMock,
      'UtilsService': utilsServiceMock,
      'TemplateFactory': templateFactoryMock,
      'PolicyFactory': policyFactoryMock
    });

    scope.$digest();
  }));


	describe('Initialize controller', function () {
    var newFakeOutputList = null;

    beforeEach(function () {
      newFakeOutputList = angular.copy(fakeOutputList);
    });

    it('Should call getOutputs function and return a list of Outputs', inject(function ($controller) {
      fragmentFactoryMock.getFragments.and.callFake(resolvedOutputListPromise);

      ctrl = $controller('OutputsCtrl', {
        'FragmentFactory': fragmentFactoryMock,
        '$filter': filter,
        '$modal': modalMock,
        'UtilsService': utilsServiceMock,
        'TemplateFactory': templateFactoryMock,
        'PolicyFactory': policyFactoryMock
      });

      scope.$digest();

      expect(ctrl.error).toBeFalsy();
      expect(ctrl.outputsData).toEqual(newFakeOutputList);
    }));

    it('Should call getOutputs function and get an error', inject(function ($controller) {
      fragmentFactoryMock.getFragments.and.callFake(rejectedOutputListPromise);

      ctrl = $controller('OutputsCtrl', {
        'FragmentFactory': fragmentFactoryMock,
        '$filter': filter,
        '$modal': modalMock,
        'UtilsService': utilsServiceMock,
        'TemplateFactory': templateFactoryMock,
        'PolicyFactory': policyFactoryMock
      });

      scope.$digest();

      expect(ctrl.error).toBeTruthy();
      expect(ctrl.errorMessage).toBe("_INPUT_ERROR_111_");
    }));

    it('Should call getOutputTypes function and create an array of the amount of each output types', function () {
      expect(ctrl.outputTypes).toEqual([{"type": "Cassandra", "count": 2}, {"type": "ElasticSearch", "count": 2}, {"type": "MongoDb", "count": 2}]);
    });
  });

	describe('Create a new output', function () {
    var newFakeOutputList, fakeNamesList = null;

    beforeEach(function () {
      modalMock.open.and.callFake(resolvedNewOutput);
      ctrl.outputsData = angular.copy(fakeOutputList);
      fakeNamesList = ['fake_name_1', 'fake_name_2'];
    });

    it('Should call createOutput function and show new output modal', function () {
      utilsServiceMock.getNamesJSONArray.and.returnValue(fakeNamesList);

      var fakeCreateOutputData = {
        'fragmentType': 'output',
        'fragmentNamesList': fakeNamesList,
        'texts': {
          'title': '_OUTPUT_WINDOW_NEW_TITLE_',
          'button': '_OUTPUT_WINDOW_NEW_BUTTON_',
          'button_icon': 'icon-circle-plus'
        }
      };

      ctrl.createOutput();

      var params = modalMock.open.calls.mostRecent().args[0];
      expect(params.resolve.item()).toEqual(fakeCreateOutputData);

      params.resolve.fragmentTemplates();
      expect(templateFactoryMock.getNewFragmentTemplate).toHaveBeenCalledWith(fakeCreateOutputData.fragmentType);
    });

    it('Should return OK when closing the create modal', function () {
      ctrl.createOutput();
      scope.$digest();

      expect(ctrl.outputsData[6]).toBe(fakeOutput);
      expect(ctrl.outputTypes).toEqual([{"type": "Cassandra", "count": 3}, {"type": "ElasticSearch", "count": 2}, {"type": "MongoDb", "count": 2}]);
    });
  });

	describe('Edit an output', function () {
    var fakeIndex, fakeOutputName, fakeOutputId, fakeOutputSelected = null;

    beforeEach(function () {
      modalMock.open.and.callFake(resolvedEditOutput);
      ctrl.outputsData = angular.copy(fakeOutputList);
      fakeIndex = 1;
      fakeOutputName = ctrl.outputsData[fakeIndex].name;
      fakeOutputId = ctrl.outputsData[fakeIndex].id;
      fakeOutputSelected = ctrl.outputsData[fakeIndex];
    });

    it('Shuold call editOutput function and show an edit output modal', function () {
      var fakeListOfNames = ['fake_name_1', 'fake_name_2'];
      utilsServiceMock.getNamesJSONArray.and.returnValue(fakeListOfNames);

      var fakeEditOutputData = {
        'originalName': fakeOutputName,
        'fragmentType': 'output',
        'index': fakeIndex,
        'fragmentSelected': fakeOutputSelected,
        'fragmentNamesList': fakeListOfNames,
        'texts': {
          'title': '_OUTPUT_WINDOW_MODIFY_TITLE_',
          'button': '_OUTPUT_WINDOW_MODIFY_BUTTON_',
          'button_icon': 'icon-circle-check',
          'secondaryText2': '_OUTPUT_WINDOW_EDIT_MESSAGE2_',
          'policyRunningMain': '_OUTPUT_CANNOT_BE_DELETED_',
          'policyRunningSecondary': '_OUTTPUT_WINDOW_POLICY_RUNNING_MESSAGE_',
          'policyRunningSecondary2': '_OUTTPUT_WINDOW_POLICY_RUNNING_MESSAGE2_'
        }
      };

      ctrl.editOutput(fakeOutputName, fakeOutputId, fakeIndex);

      var params = modalMock.open.calls.mostRecent().args[0];
      expect(params.resolve.item()).toEqual(fakeEditOutputData);

      params.resolve.fragmentTemplates();
      expect(templateFactoryMock.getNewFragmentTemplate).toHaveBeenCalledWith(fakeEditOutputData.fragmentSelected.fragmentType);

      params.resolve.policiesAffected();
      expect(policyFactoryMock.getPolicyByFragmentId).toHaveBeenCalledWith(fakeEditOutputData.fragmentSelected.fragmentType, fakeEditOutputData.fragmentSelected.id);
    });

    it('Should return OK when closing the edit modal', function () {
      ctrl.editOutput(fakeOutputName, fakeOutputId, fakeIndex);
      scope.$digest();

      expect(ctrl.outputsData[fakeIndex]).toEqual(fakeOutput);
      expect(ctrl.outputTypes).toEqual([{"type": "Cassandra", "count": 3}, {"type": "MongoDb", "count": 2}, {"type": "ElasticSearch", "count": 1}]);
    });
  });

	describe('Delete an output', function () {
    var fragmentType, fragmentId, fragmentId = null;

    beforeEach(function () {
      modalMock.open.and.callFake(resolvedDeleteOutput);
      ctrl.outputsData = angular.copy(fakeOutputList);

      fakeFragmentType = 'output';
      fakeFragmentId = 'fake output id 3';
      fakeIndex = 2;
    });

    it('Should call deleteOutput function and show a delete output modal', function () {
      var fakeOutputToDelete = {
        'type': fakeFragmentType,
        'id': fakeFragmentId,
        'index': fakeIndex,
        'texts': {
          'title': '_OUTPUT_WINDOW_DELETE_TITLE_',
          'mainText': '_OUTPUT_CANNOT_BE_DELETED_',
          'mainTextOK': '_ARE_YOU_COMPLETELY_SURE_',
          'secondaryText1': '_OUTPUT_WINDOW_DELETE_MESSAGE_',
          'secondaryText2': '_OUTPUT_WINDOW_DELETE_MESSAGE2_',
          'policyRunningMain': '_OUTPUT_CANNOT_BE_DELETED_',
          'policyRunningSecondary': '_OUTTPUT_WINDOW_POLICY_RUNNING_MESSAGE_',
          'policyRunningSecondary2': '_OUTTPUT_WINDOW_DELETE_POLICY_RUNNING_MESSAGE2_'
        }
      };

			ctrl.deleteOutput(fakeFragmentType, fakeFragmentId, fakeIndex);

			var params = modalMock.open.calls.mostRecent().args[0]
	    expect(params.resolve.item()).toEqual(fakeOutputToDelete);

	    params.resolve.policiesAffected();
			expect(policyFactoryMock.getPolicyByFragmentId).toHaveBeenCalledWith(fakeOutputToDelete.type, fakeOutputToDelete.id);
		});

		it('Should return OK when closing the delete modal', function() {
			ctrl.deleteOutput(fakeFragmentType, fakeFragmentId, fakeIndex);
			scope.$digest();
			expect(ctrl.outputsData[fakeIndex]).toEqual(fakeOutputList[fakeIndex+1]);
		});
	});

	describe('Duplicate an output', function() {
		var fakeOutputId, fakeNewName, fakeListOfNames, fakeOutputListIndex = null;

		beforeEach(function() {
			modalMock.open.and.callFake(resolvedDuplicatedOutput);
			ctrl.outputsData = angular.copy(fakeOutputList);

			fakeOutputListIndex = 0;
			fakeOutputId = ctrl.outputsData[fakeOutputListIndex].id;
			fakeNewName = ctrl.outputsData[fakeOutputListIndex].name + '(1)';
			fakeListOfNames = ['fake_name_1', 'fake_name_2'];
			fakeInputSelected = ctrl.outputsData[fakeOutputListIndex];
		});

		it('Should call duplicateOutput function and show a duplicate output modal', function() {
			utilsServiceMock.autoIncrementName.and.returnValue(fakeNewName);
			ctrl.outputsData[fakeOutputListIndex].name = fakeNewName;
			utilsServiceMock.getNamesJSONArray.and.returnValue(fakeListOfNames);

      var fakeDuplicateOutputData = {
        'fragmentData': fakeInputSelected,
        'fragmentNamesList': fakeListOfNames,
        'texts': {
          'title': '_OUTPUT_WINDOW_DUPLICATE_TITLE_'
        }
      };

			ctrl.duplicateOutput(fakeOutputId);

			var params = modalMock.open.calls.mostRecent().args[0];
	    expect(params.resolve.item()).toEqual(fakeDuplicateOutputData);
		});

		it('Should return OK when closing the duplicate modal', function() {
			ctrl.duplicateOutput(fakeOutputId);
			scope.$digest();

			expect(ctrl.outputsData[6]).toBe(fakeOutputDuplicated);
      expect(ctrl.outputTypes).toEqual([{"type": "Cassandra", "count": 3}, {"type": "ElasticSearch", "count": 2}, {"type": "MongoDb", "count": 2}]);
		});
	});

});
