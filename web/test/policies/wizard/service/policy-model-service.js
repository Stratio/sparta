describe('Model service', function () {
  beforeEach(module('webApp'));
  beforeEach(module('served/policy.json'));
  beforeEach(module('served/model.json'));

  var service, ModalServiceMock, PolicyModelFactoryMock, translateMock, ModelFactoryMock, CubeServiceMock,
    AccordionStatusServiceMock, UtilsServiceMock,
    fakeModel, fakePolicy = null;
  var fakeTranslation = "fake translation";

  beforeEach(module(function ($provide) {
    ModalServiceMock = jasmine.createSpyObj('ModalService', ['openModal']);
    PolicyModelFactoryMock = jasmine.createSpyObj('PolicyModelFactory', ['getCurrentPolicy']);
    translateMock = jasmine.createSpy().and.returnValue(fakeTranslation);
    ModelFactoryMock = jasmine.createSpyObj('ModelFactory', ['getModel']);
    CubeServiceMock = jasmine.createSpyObj('CubeService', ['findCubesUsingOutputs']);
    AccordionStatusServiceMock = jasmine.createSpyObj('AccordionStatusService', ['resetAccordionStatus']);
    UtilsServiceMock = jasmine.createSpyObj('UtilsService', ['removeItemsFromArray']);
    $provide.value('ModalService', ModalServiceMock);
    $provide.value('PolicyModelFactory', PolicyModelFactoryMock);
    $provide.value('translate', translateMock);
    $provide.value('ModelFactory', ModelFactoryMock);
    $provide.value('CubeService', CubeServiceMock);
    $provide.value('AccordionStatusService', AccordionStatusServiceMock);
    $provide.value('UtilsService', UtilsServiceMock);
  }));

  beforeEach(inject(function (_servedModel_, _servedPolicy_, _ModelService_) {
    fakeModel = _servedModel_;
    fakePolicy = _servedPolicy_;
    PolicyModelFactoryMock.getCurrentPolicy.and.returnValue(fakePolicy);
    service = _ModelService_;
  }));

  describe("should be able to show a confirmation modal with the cubes names introduced as param when model is going to be removed", function () {
    var fakeCubeNames = ["fake cube 1", "fake cube 2", "fake cube 3"];
    var expectedModalResolve = {
      title: function () {
        return "_REMOVE_MODEL_CONFIRM_TITLE_"
      },
      message: function () {
        return fakeTranslation;
      }
    };

    beforeEach(function () {
      service.showConfirmRemoveModel();
    });

    it("modal should render the confirm modal template", function () {
      expect(ModalServiceMock.openModal.calls.mostRecent().args[1]).toBe('templates/modal/confirm-modal.tpl.html');
    });

    it("if cube name list is empty, message displayed is empty", function () {
      service.showConfirmRemoveModel();
      expect(ModalServiceMock.openModal.calls.mostRecent().args[2].title()).toEqual(expectedModalResolve.title());
      expect(ModalServiceMock.openModal.calls.mostRecent().args[2].message()).toEqual("");
      expect(translateMock).not.toHaveBeenCalled();
    });
    it("if cube name list is not empty, message is displayed with the cube names separated by comma", function () {
      service.showConfirmRemoveModel(fakeCubeNames);
      expect(ModalServiceMock.openModal.calls.mostRecent().args[2].title()).toEqual(expectedModalResolve.title());
      expect(ModalServiceMock.openModal.calls.mostRecent().args[2].message()).toEqual(expectedModalResolve.message);
      expect(translate).toHaveBeenCalledWith('_REMOVE_MODEL_MESSAGE_', {modelList: fakeCubeNames.toString()});
    })
  });

});
