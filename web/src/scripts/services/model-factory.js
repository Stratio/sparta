(function () {
  'use strict';

  angular
    .module('webApp')
    .factory('ModelFactory', ModelFactory);

  ModelFactory.$inject = ['PolicyModelFactory'];

  function ModelFactory(PolicyModelFactory) {
    var model = {};
    var error = {text: "", duplicatedOutput: false};
    var template = null;

    function init(newTemplate) {
      template = newTemplate;
      model.name = "";
      model.outputFields = [];
      model.type = template.types[0].name;
      model.configuration = JSON.stringify(template.morphlinesDefaultConfiguration, null, 4);
      model.inputList = getModelInputs();
      model.inputField = model.inputList[0].value;
      error.text = "";
      error.duplicatedOutput = false;
    }

    function getModelInputs() {
      var models = PolicyModelFactory.getCurrentPolicy().models;
      var result = [];
      var index = models.length;
      if (index >= 0) {
        if (index == 0)
          result = template.defaultInput;
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
      var isValid = model.inputField != "" && model.outputFields.length > 0 &&
        model.name != "" && model.type != "" && isValidConfiguration();
      if (!isValid) {
        error.text = "_GENERIC_FORM_ERROR_";
      } else  error.text = "";
      return isValid;
    }

    function getModel(template) {
      if (Object.keys(model).length == 0) init(template);
      return model;
    }

    function setModel(m) {
      model.name = m.name;
      model.outputFields = m.outputFields;
      model.type = m.type;
      if (!((typeof m.configuration) == "string")) {
        m.configuration = JSON.stringify(m.configuration, null, 4);
      }
      model.configuration = m.configuration;
      model.inputList = m.inputList;
      model.inputField = m.inputField;
      error.text = "";
    }

    function resetModel(template) {
      init(template);
    }

    function getError() {
      return error;
    }

    return {
      resetModel: resetModel,
      getModel: getModel,
      setModel: setModel,
      isValidModel: isValidModel,
      getError: getError
    }
  }

})
();


