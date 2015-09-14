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
        var models = PolicyModelFactory.getCurrentPolicy().models;
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

      function isValidConfiguration() {
        var configuration = model.configuration;
        try {
          model.configuration = JSON.parse(configuration);
          return true;
        } catch (e) {
          model.configuration = configuration;
          return false;
        }
      }

      function isValidModel() {
        return model.inputField != "" && model.outputFields.length > 0 && model.configuration != "" &&
          model.name != "" && model.type != "" && isValidConfiguration();
      }

      function getModel() {
        if (Object.keys(model).length == 0) init();
        return model;
      }

      function resetModel() {
        init();
      }

      return {
        resetModel: resetModel,
        getModel: getModel,
        isValidModel: isValidModel
      }
    }

  })
();


