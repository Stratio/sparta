describe('policies.wizard.service.wizard-status-service', function () {
  beforeEach(module('webApp'));

  var service = null;

  beforeEach(inject(function (WizardStatusService) {
    service = WizardStatusService;
  }));

  describe("should be able to change the current step in the process of modify or create the current policy", function () {

    it("should be able to change to the next step", function () {
      var currentStep = service.getStatus().currentStep;
      service.previousStep();
      expect(service.getStatus().currentStep).toBe(currentStep - 1);
    });

    it("should be able to change to the next step", function () {
      var currentStep = service.getStatus().currentStep;
      service.nextStep();
      expect(service.getStatus().currentStep).toBe(currentStep + 1);
    });
  });
});
