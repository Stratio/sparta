(function() {
    'use strict';

    angular
      .module('webApp')
      .controller('SettingsCtrl', SettingsCtrl);

    SettingsCtrl.$inject = ['FragmentFactory', 'PolicyFactory'];

    function SettingsCtrl(FragmentFactory, PolicyFactory) {
      /*jshint validthis: true*/
      var vm = this;
      vm.runScriptFragments = runScriptFragments;
      vm.runScriptPolicies = runScriptPolicies;

      init();

      /////////////////////////////////

      function init() {

      };

      function runScriptFragments() {
        var inputList = FragmentFactory.GetFakeFragments('create_inputs.json');

          inputList.then(function (result) {
            console.log('********Inputs list')
            console.log(result);
            for (var i=0; i<result.length; i++){
              var newFragment = FragmentFactory.CreateFragment(result[i]);

              newFragment.then(function (newInputResult) {
                console.log('********New input')
                console.log(newInputResult);
              },function (error) {
                console.log('There was an error while creating an input');
                console.log(error);
              });
            }

          },function (error) {
            console.log('There was an error while loading the inputs flist!');
            console.log(error);
          });

        var outputList = FragmentFactory.GetFakeFragments('create_outputs.json');

        outputList.then(function (result) {
          console.log('********Outputs list')
          console.log(result);
          for (var i=0; i<result.length; i++){
            var newFragment = FragmentFactory.CreateFragment(result[i]);

            newFragment.then(function (newInputResult) {
              console.log('********New output')
              console.log(newInputResult);
            },function (error) {
              console.log('There was an error while creating an output!');
              console.log(error);
            });
          }

        },function (error) {
          console.log('There was an error while loading the outputs list!');
          console.log(error);
        });
      };


      function runScriptPolicies() {
        var inputsListRequest = FragmentFactory.GetFragments('input');

        inputsListRequest.then(function (result) {
          console.log('********Inputs list')
          console.log(result);
          var inputList = result;

          var policyTemplate = PolicyFactory.GetFakePolicy();

          policyTemplate.then(function (result) {
            console.log('********Policy template')
            console.log(result);
            //var policyTemplate = result;

            for(var i=0; i<inputList.length; i++) {
              var test = {};
              angular.copy(result, test);
              test.name = 'Policy - ' + inputList[i].name + ' 1';
              test.fragments[0].name = inputList[i].name;
              test.fragments[0].id = inputList[i].id;

              var newPolicy = PolicyFactory.CreatePolicy(test);

              newPolicy.then(function (result) {
                console.log('********New Policy')
                console.log(result);
              },function (error) {
                console.log('There was an error while creating the policy!');
                console.log(error);
              });
            };

          },function (error) {
            console.log('There was an error while loading the policy template!');
            console.log(error);
          });

        },function (error) {
          console.log('There was an error while loading the policy template!');
          console.log(error);
        });

      };

    };
})();
