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
      model.inputList = getModelInputs();
      model.inputField = model.inputList[0];
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
          result = [model.inputField].concat(model.outputFields);
        }
      }
      return result;
    }

    function IsValidConfiguration() {
      var configuration = model.configuration;
      try {
        model.configuration = JSON.parse(configuration);
        return true;
      } catch (e) {
        model.configuration = model.configuration;
        return false;
      }
    }

    return {
      GetModelList: function () {
        return models;
      },
      ResetNewModel: function () {
        initNewModel();
      },
      GetNewModel: function () {
        if (Object.keys(model).length == 0) initNewModel();
        return model;
      },
      IsValidConfiguration: IsValidConfiguration
    }
  }

})
();


