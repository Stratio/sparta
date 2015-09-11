(function () {
  'use strict';

  angular
    .module('webApp')
    .factory('ModelFactory', ModelFactory);


  ModelFactory.$inject = ['ModelStaticDataFactory', 'PolicyModelFactory'];

  function ModelFactory(ModelStaticDataFactory, PolicyModelFactory) {
    var model = {};

    function init() {
      model.name = "";
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
        model.configuration = configuration;
        return false;
      }
    }

    return {
      ResetModel: function (index) {
        init(index);
      },
      GetModel: function () {
        if (Object.keys(model).length == 0) init();
        return model;
      },
      IsValidConfiguration: IsValidConfiguration
    }
  }

})
();


