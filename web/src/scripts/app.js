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
    'pascalprecht.translate'
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
      prefix: '/languages/',
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
            'submenu': {
                templateUrl:  'views/dashboard/submenu_dashboard.html'
            },
            'content': {
                templateUrl:  'views/dashboard/submenu_dashboard_index.html'
            }
        }
    })
    .state('dashboard.inputs', {
        url: '/inputs',
        controller: 'NuevoCtrl',
        controllerAs: 'nuevo',
        templateUrl: 'views/nuevo.html'
    })
    .state('dashboard.outputs', {
        url: '/outputs'
/*
        controller: 'NuevoCtrl',
        controllerAs: 'nuevo',
        templateUrl: 'views/nuevo.html'
*/
    })
    .state('dashboard.policies', {
        url: '/policies'
/*
        controller: 'NuevoCtrl',
        controllerAs: 'nuevo',
        templateUrl: 'views/nuevo.html'
*/
    })

    /*******  SETINGS *******/
    .state('settings', {
        url: '/settings',
        views: {
            'submenu': {
                templateUrl:  'views/settings/submenu_settings.html'
            },
            'content': {
                controller:   'NuevoCtrl',
                controllerAs: 'nuevo',
                templateUrl:  'views/settings/submenu_settings_index.html'
            }
        }
    });


  }]);