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
          vm.inputsList = result;

          var policyTemplate = PolicyFactory.GetFakePolicy();

          policyTemplate.then(function (result) {
            console.log('********Policy template')
            console.log(result);
            var policyTemplate = result;

            for(var i=0; i<vm.inputsList.length; i++) {

              policyTemplate.name = 'Policy - ' + vm.inputsList[i].name + ' 1';
              policyTemplate.fragments[0].name = vm.inputsList[i].name;
              policyTemplate.fragments[0].id = vm.inputsList[i].id;

              var newPolicy = PolicyFactory.CreatePolicy(policyTemplate);

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
