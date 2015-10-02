(function () {
  'use strict';

  angular
    .module('webApp')
    .factory('ModelFactory', ModelFactory);

  ModelFactory.$inject = ['PolicyModelFactory'];

  function ModelFactory() {
    var model = {};
    var error = {text: "", duplicatedOutput: false};
    var template = null;
    var context = {"position": null};
    var inputList = [];

    function init(newTemplate, order) {
      template = newTemplate;
      model.name = "";
      model.outputFields = [];
      model.type = template.types[0].name;
      model.configuration = template.morphlinesDefaultConfiguration;
      model.order = order;
      error.text = "";
      error.duplicatedOutput = false;
    }

    function updateModelInputs(models) {
      inputList.length = 0;
      var index = context.position;
      if (index >= 0) {
        if (index == 0) {
          inputList.push(template.defaultInput);
        } else {
          var previousModel = models[--index];
          var previousOutputs = generateOutputOptions(previousModel.outputFields);
          if (previousModel.inputField != template.defaultInput.value) { // do not put the default input
            var previousInputs = generateOutputOptions([previousModel.inputField]);
            inputList.push.apply(inputList, previousInputs);
          }
          inputList.push.apply(inputList, previousOutputs);
        }
      }
      model.inputField = inputList[0].value;
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

    function isValidModel() {
      var isValid = model.inputField != "" && model.outputFields.length > 0 &&
        model.name != "" && model.type != "";
      if (!isValid) {
        error.text = "_GENERIC_FORM_ERROR_";
      } else  error.text = "";
      return isValid;
    }

    function getModel(template, order) {
      if (Object.keys(model).length == 0) init(template, order);
      return model;
    }

    function setModel(m) {
      model.name = m.name;
      model.outputFields = m.outputFields;
      model.type = m.type;
      model.configuration = m.configuration;
      model.inputList = m.inputList;
      model.inputField = m.inputField;
      model.order = m.order;
      error.text = "";
    }

    function resetModel(template, order) {
      init(template, order);
    }

    function getContext() {
      return context;
    }

    function setPosition(p) {
      context.position = p;
    }

    function getError() {
      return error;
    }

    function setError(e) {
      error.text = e;
    }

    function getModelInputs() {
      return inputList;
    }

    return {
      resetModel: resetModel,
      getModel: getModel,
      setModel: setModel,
      getContext: getContext,
      setPosition: setPosition,
      isValidModel: isValidModel,
      getError: getError,
      setError: setError,
      getModelInputs: getModelInputs,
      updateModelInputs: updateModelInputs
    }
  }
})
();


