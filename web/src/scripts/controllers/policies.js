(function() {
    'use strict';

    angular
        .module('webApp')
        .controller('PoliciesCtrl', PoliciesCtrl);

    PoliciesCtrl.$inject = ['PolicyFactory'];

    function PoliciesCtrl(PolicyFactory) {
        /*jshint validthis: true*/
       var vm = this;

       vm.policiesData = {};
       vm.policiesData.list = [];

       init();

        /////////////////////////////////

        function init() {
          getPolicies();
        };

        function getPolicies() {
            var policiesList = PolicyFactory.GetAllpolicies();

            policiesList.then(function (result) {
                console.log('--> Getting policies');
                console.log('> Getting list of policies');
                console.log(result);
                vm.policiesData.list = result;
            });
        };
    };
})();
