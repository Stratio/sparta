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

  it('should be able to find a step name by its id searching inside of substeps lists', function() {
    var steps = [
      {
        "name": "_POLICY_._STEPS_._INPUT_",
        "icon": "icon-input",
        "currentMessage": "_MESSAGE_._INPUT_STEP_"
      },
      {
        "name": "_POLICY_._STEPS_._TRANSFORMATION_",
        "icon": "icon-content-left",
        "currentMessage": "_MESSAGE_._TRANSFORMATION_STEP_",
        "availableMessage": "_MESSAGE_._TRANSFORMATION_STEP_AVAILABLE_"
      },
      {
        "subSteps": [
          {
            "name": "_POLICY_._STEPS_._TRIGGERS_",
            "icon": "icon-content-left",
            "currentMessage": "_MESSAGE_._TRIGGER_STEP_",
            "availableMessage": "_MESSAGE_._TRIGGER_STEP_AVAILABLE_"
          },
          {
            "name": "_POLICY_._STEPS_._CUBES_",
            "icon": "icon-box",
            "currentMessage": "_MESSAGE_._CUBE_STEP_",
            "availableMessage": "_MESSAGE_._CUBE_STEP_AVAILABLE_"
          }
        ]
      }
    ];

    var stepIndex = 3;
    // var stepName = service.getStepNameByIndex(steps, stepIndex);
    //
    // expect(stepName).toBe("_POLICY_._STEPS_._CUBES_");

    stepIndex = 2;

   var stepName = service.getStepNameByIndex(steps, stepIndex);

    expect(stepName).toBe("_POLICY_._STEPS_._TRIGGERS_");
    //
    // stepIndex = 2;
    //
    // stepName = service.getStepNameByIndex(steps, stepIndex);
    //
    // expect(stepName).toBe("_POLICY_._STEPS_._TRIGGERS_");
  });
});
