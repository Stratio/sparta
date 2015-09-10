(function () {
  'use strict';

  angular
    .module('webApp')
    .factory('ModelFactory', ModelFactory);


  ModelFactory.$inject = ['ModelStaticDataFactory', 'PolicyModelFactory'];

  function ModelFactory(ModelStaticDataFactory, PolicyModelFactory) {
    var model = {};
    var models = [];

    function initNewModel() {
      model.inputField = ModelStaticDataFactory.defaultInput;

      model.outputFields = [];
      model.type = "";
      model.configuration = "";
    };

    function getModelInputs() {
      var models = PolicyModelFactory.GetCurrentPolicy().models;
      var result = [];
      var index = models.length;
      if (index >= 0) {
        if (index == 0)
          result = ModelStaticDataFactory.defaultInput;
        else {
          var model = models[--index];
          console.log(model.outputFields);
          console.log(model.inputField);
          result = [model.inputField].concat(model.outputFields);
          console.log(result)
        }
      }
      return result;
    }

    return {
      GetModelList: function () {
        return models;
      },
      ResetNewModel: function () {
        initNewModel();
      },
      GetNewModel: function (index) {
        if (Object.keys(model).length == 0) initNewModel();
        return model;
      },
      GetModelInputs: getModelInputs
    }
  }

})
();


