describe('policies.wizard.controller.policy-wizard-header-controller', function () {
  beforeEach(module('webApp'));
  beforeEach(module('served/policy.json'));
  beforeEach(module('served/policyTemplate.json'));

  var ctrl, scope, fakePolicy, fakeTemplate, policyModelFactoryMock, fakeWizardStatus,  modalServiceMock,  wizardStatusServiceMock;

  // init mock modules

  beforeEach(inject(function ($controller, $q, $httpBackend, $rootScope, _servedPolicy_, _servedPolicyTemplate_) {
    scope = $rootScope.$new();

    fakePolicy = angular.copy(_servedPolicy_);
    fakeTemplate = _servedPolicyTemplate_;
    fakeWizardStatus={currentStep: -1};

    $httpBackend.when('GET', 'languages/en-US.json')
      .respond({});

    policyModelFactoryMock = jasmine.createSpyObj('PolicyModelFactory', ['getCurrentPolicy', 'getFinalJSON', 'setPolicy', 'setTemplate', 'getTemplate', 'getProcessStatus', 'resetPolicy']);
    policyModelFactoryMock.getCurrentPolicy.and.callFake(function () {
      return fakePolicy;
    });

    policyModelFactoryMock.getTemplate.and.returnValue(fakeTemplate);

    wizardStatusServiceMock =  jasmine.createSpyObj('WizardStatusService', ['getStatus']);
    wizardStatusServiceMock.getStatus.and.returnValue(fakeWizardStatus);

    modalServiceMock = jasmine.createSpyObj('ModalService', ['openModal']);

    modalServiceMock.openModal.and.callFake(function () {
      var defer = $q.defer();
      defer.resolve();
      return {"result": defer.promise};
    });

    ctrl = $controller('PolicyWizardHeaderCtrl as header', {
      'WizardStatusService': wizardStatusServiceMock,
      'PolicyModelFactory': policyModelFactoryMock,
      'ModalService': modalServiceMock,
      '$scope': scope
    });

    scope.$digest();
  }));

  it ("when it is initialized, policy template is retrieved", function(){
    expect(policyModelFactoryMock.getTemplate).toHaveBeenCalled();
  });

  it ("should be able to open the policy description modal", function(){
    ctrl.showPolicyData();


    expect(modalServiceMock.openModal).toHaveBeenCalled();
    var callParams = modalServiceMock.openModal.calls.mostRecent().args;
    expect(callParams[0]).toBe('PolicyCreationModalCtrl');
    expect(callParams[1]).toBe('templates/modal/policy-creation-modal.tpl.html');
    expect(callParams[2].title()).toBe("_POLICY_._MODAL_SETTINGS_TITLE_");
    expect(callParams[3]).toEqual('');
    expect(callParams[4]).toEqual('lg');

  });

  describe ("should be any change in wizard status and update the help link according to the current step", function(){
    it("if current step is major than -1, help link is updated to the right link", function(){
      fakeWizardStatus.currentStep = 0;

      scope.$digest();

      expect(ctrl.helpLink).toBe(fakeTemplate.helpLinks[fakeWizardStatus.currentStep + 1]);
    });

    it("if current step is -1, it must not do anything", function(){
      var oldHelpLink = "fake help link";
      fakeWizardStatus.currentStep = -1;
      ctrl.helpLink = oldHelpLink;

      scope.$digest();

      expect(ctrl.helpLink).toBe(oldHelpLink);
    });
  });


});
