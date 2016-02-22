describe('policies.wizard.service.accordion-status-service', function () {
  beforeEach(module('webApp'));

  var service = null;


  beforeEach(inject(function (_AccordionStatusService_) {
    service = _AccordionStatusService_;

  }));

  describe("should be able to reset the accordion status", function () {
    it("if the first param is null or undefined, accordion status is not reset", function () {
      var length = null;
      service.resetAccordionStatus(length);

      expect(service.getAccordionStatus()).toEqual([]);

      length = undefined;
      service.resetAccordionStatus(length);

      expect(service.getAccordionStatus()).toEqual([]);
    });

    it("if the first param is a valid length, accordion status is reset with the that length more one position", function () {
      var length = 5;
      service.resetAccordionStatus(length);

      expect(service.getAccordionStatus().length).toEqual(length + 1);
    });

    it("if the second param is introduced, that param is used to specify the position that has to be true", function () {
      var length = 5;
      var truePosition = 4;
      service.resetAccordionStatus(length, truePosition);

      expect(service.getAccordionStatus().length).toEqual(length + 1);
      for (var i = 0; i <= length; ++i) {
        if (i != truePosition)
          expect(service.getAccordionStatus()[i]).toEqual(false);
      }
      expect(service.getAccordionStatus()[4]).toEqual(true);
    });

    it("accordion status length is adjusted to the new length if the new length is minor than accordion status one", function () {
      var oldLength = 5;
      var newLength = 3;
      service.resetAccordionStatus(oldLength);
      service.resetAccordionStatus(newLength);

      expect(service.getAccordionStatus().length).toBe(newLength+1);
    })
  })

});
