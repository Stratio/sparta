describe('policies.wizard.service.wizard-status-service', function() {
  beforeEach(module('webApp'));

  var service = null;

  beforeEach(inject(function(WizardStatusService) {
    service = WizardStatusService;
  }));

  describe("should be able to change the current step in the process of modify or create the current policy", function() {

    it("should be able to change to the next step", function() {
      var currentStep = service.getStatus().currentStep;
      service.previousStep();
      expect(service.getStatus().currentStep).toBe(currentStep - 1);
    });

    it("should be able to change to the next step", function() {
      var currentStep = service.getStatus().currentStep;
      service.nextStep();
      expect(service.getStatus().currentStep).toBe(currentStep + 1);
    });
  });

  it("should be able to enable next step", function() {
    service.getStatus().nextStepAvailable = false;

    service.enableNextStep();

    expect(service.getStatus().nextStepAvailable).toBeTruthy();
  });

  it("should be able to disable next step", function() {
    service.getStatus().nextStepAvailable = true;

    service.disableNextStep();

    expect(service.getStatus().nextStepAvailable).toBeFalsy();
  });

  it("should be reset the wizard status", function() {
    service.enableNextStep();
    service.nextStep();
    service.nextStep();
    service.nextStep();

    service.reset();

    expect(service.getStatus().nextStepAvailable).toBeFalsy();
    expect(service.getStatus().currentStep).toBe(-1);
  });
});
