'use strict';

/**
 * @ngdoc overview
 * @name webApp
 * @description
 * # webApp
 *
 * Main module of the application.
 */

angular
  .module('webApp', [
    'ngAnimate',
    'ngCookies',
    'ngResource',
    'ngRoute',
    'ngSanitize',
    'ngTouch',
    'ui.stratio.grid',
    'ui.router',
    'StratioUI',
    'ui.bootstrap',
    'ui.bootstrap.modal',
    'ui.bootstrap.tpls',
    'pascalprecht.translate',
    'ngTruncateNumber'
  ])

  /*** TRANSLATING ***/
  .config(['$translateProvider', function($translateProvider) {
/*
    var getLanguage = function() {
      var language = (navigator.language) ? navigator.language : navigator.browserLanguage;
      return (language.indexOf('es') === 0) ? 'es-ES' : 'en-US';
    };
    var language = getLanguage();
*/
    var language = 'en-US';

    $translateProvider.useStaticFilesLoader({
      prefix: 'languages/',
      suffix: '.json'
    });
    $translateProvider.preferredLanguage(language);
  }])

  /*ROUTER*/
  .config(['$stateProvider','$urlRouterProvider', function ($stateProvider, $urlRouterProvider) {
    // For any unmatched url, redirect to /dashboard/inputs
    $urlRouterProvider.otherwise('/dashboard/inputs');

    $stateProvider
    /*******  DASHBOARD *******/
    .state('dashboard', {
        url: '/dashboard',
        views: {
            'menu': {
                templateUrl:  'views/dashboard/dashboard_menu.html'
            },
            'content': {
                templateUrl:  'views/dashboard/dashboard_content.html'
            }
        }
    })
    .state('dashboard.inputs', {
        url: '/inputs',
        controller: 'InputsCtrl',
        controllerAs: 'inputs',
        templateUrl: 'views/inputs.html'
    })
    .state('dashboard.outputs', {
        url: '/outputs',
        controller: 'OutputsCtrl',
        controllerAs: 'outputs',
        templateUrl: 'views/outputs.html'
    })
    .state('dashboard.policies', {
        url: '/policies',
        controller: 'PoliciesCtrl',
        controllerAs: 'policies',
        templateUrl: 'views/policies.html'
    })
      .state('dashboard.newPolicy', {
        url: '/policies/new',
        controller: 'NewPolicyCtrl',
        controllerAs: 'wizard',
        templateUrl: 'views/policies/wizard-panel.html'
      })
      .state('dashboard.editPolicy', {
        url: '/policies/edit/:id',
        params: { id: null },
        controller: 'EditPolicyCtrl',
        controllerAs: 'wizard',
        templateUrl: 'views/policies/wizard-panel.html'
      })
    /*******  SETINGS *******/
    .state('settings', {
        url: '/settings',
        views: {
            'content': {
                controller:   'SettingsCtrl',
                controllerAs: 'settings',
                templateUrl:  'views/settings/settings_content.html'
            }
        }
    });


  }]);
