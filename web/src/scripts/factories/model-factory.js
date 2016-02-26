(function () {
  'use strict';

  angular
    .module('webApp')
    .factory('ModelFactory', ModelFactory);

  function ModelFactory() {
    var model = {};
    var error = {text: "", duplicatedOutput: false};
    var template = null;
    var context = {"position": null};
    var inputList = [];

    function init(newTemplate, order, position) {
      setPosition(position);
      template = newTemplate;
      model.outputFields = [];
      model.inputField = "";
      model.type = template.types[0].name;
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
      if (!model.inputField) {
        model.inputField = inputList[0].value
      }
      return inputList;
    }

    function generateOutputOptions(outputs) {
      var options = [];
      var output, option = "";
      for (var i = 0; i < outputs.length; ++i) {
        output = outputs[i].name || outputs[i];
        option = {label: output, value: output};
        options.push(option);
      }
      return options;
    }

    function isValidModel() {
      var isValid = model.inputField != "" && model.outputFields.length > 0 && model.type != "";
      if (!isValid) {
        error.text = "_GENERIC_FORM_ERROR_";
      } else  error.text = "";
      return isValid;
    }

    function getModel(template, order, position) {
      if (Object.keys(model).length == 0) init(template, order, position);
      return model;
    }

    function setModel(m, position) {
      model.outputFields = m.outputFields;
      model.type = m.type;
      model.configuration = m.configuration;
      model.inputField = m.inputField;
      model.order = m.order;
      error.text = "";
      setPosition(position);
    }

    function resetModel(template, order, position) {
      init(template, order, position);
    }

    function getContext() {
      return context;
    }

    function setPosition(p) {
      if (p === undefined) {
        p = 0;
      }
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


