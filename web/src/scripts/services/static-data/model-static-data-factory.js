(function () {
  'use strict';

  angular
    .module('webApp')
    .service('ModelStaticDataFactory', ModelStaticDataFactory);

  function ModelStaticDataFactory() {
    var morphlinesDefaultConfiguration = {
      "morphline": {
        "id": "morphline1",
        "importCommands": [
          "org.kitesdk.**"
        ],
        "commands": [
          {
            "readJson": {}
          },
          {
            "extractJsonPaths": {
              "paths": {
                "field1": "/field-in-json1",
                "field2": "/field-in-json2"
              }
            }
          }
        ]
      }
    };
    var dateTimeDefaultConfiguration = {
      "inputFormat": "unix"
    };
    var typeDefaultConfiguration = {
      "type": "long",
      "newField": "newFieldLong"
    };

    function getTypes() {
      return [{iconName: "morphlines", name: "Morphlines"}, {iconName: "datetime", name: "DateTime"}, {
        iconName: "type",
        name: "Type"
      }]
    }

    return {
      getTypes: getTypes,
      getDefaultInput: function () {
        return [{"label": "raw", "value": "raw"}]
      },
      getConfigHelpLink: function () {
        return "http://docs.stratio.com/modules/sparkta/development/transformations.html"
      },
      getOutputPattern: function () {
        return "[a-zA-Z0-9]*"
      },
      getOutputInputPlaceholder: function () {
        return "_OUTPUT_INPUT_PLACEHOLDER_"
      },
      getDefaultConfigurations: function (type) {
        var types = getTypes();
        switch (type) {
          case types[0].name:
          {
            return morphlinesDefaultConfiguration;
          }
          case types[1].name:
          {
            return dateTimeDefaultConfiguration;
          }
          case types[2].name:
          {
            return typeDefaultConfiguration;
          }
        }
      }
    }
  }
})
();
