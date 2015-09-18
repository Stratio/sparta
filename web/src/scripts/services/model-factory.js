(function () {
  'use strict';

  angular
    .module('webApp')
    .factory('ModelFactory', ModelFactory);


  ModelFactory.$inject = ['ModelStaticDataFactory', 'PolicyModelFactory'];

  function ModelFactory(ModelStaticDataFactory, PolicyModelFactory) {
    var model = {};
    var error = {text: ""};

    function init() {
      model.name = "";
      model.outputFields = [];
      model.type = ModelStaticDataFactory.getTypes()[0].name;
      model.configuration = "";
      model.inputList = getModelInputs();
      model.inputField = model.inputList[0].value;
      error.text = "";
    };

    function getModelInputs() {
      var models = PolicyModelFactory.getCurrentPolicy().models;
      var result = [];
      var index = models.length;
      if (index >= 0) {
        if (index == 0)
          result = ModelStaticDataFactory.getDefaultInput();
        else {
          var model = models[--index];
          var options = generateOutputOptions(model.outputFields);
          var defaultOption = generateOutputOptions([model.inputField]);
          result = defaultOption.concat(options);
        }
      }
      return result;
    }

    function generateOutputOptions(outputs) {
      var options = [];
      var output, option = "";
      for (var i = 0; i < outputs.length; ++i) {
        output = outputs[i];
        option = {label: output, value: output};
        options.push(option);
      }
      return options;
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
      var isValid = model.inputField != "" && model.outputFields.length > 0 && model.configuration != "" &&
        model.name != "" && model.type != "" && isValidConfiguration();
      if (!isValid) {
        error.text = "_GENERIC_FORM_ERROR_";
      }else  error.text = "";
      return isValid;
    }

    function getModel() {
      if (Object.keys(model).length == 0) init();
      return model;
    }

    function resetModel() {
      init();
    }

    function getError() {
      return error;
    }

    return {
      resetModel: resetModel,
      getModel: getModel,
      isValidModel: isValidModel,
      getError: getError
    }
  }

})
();


