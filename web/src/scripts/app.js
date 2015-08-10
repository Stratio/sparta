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
    // For any unmatched url, redirect to /state1
    $urlRouterProvider.otherwise('/');

    $stateProvider
      .state('index', {
        url: '/',
        controller: 'MainCtrl',
        templateUrl: 'views/main.html'
      })
/*
      .state('inputs', {
        url: '/inputs',

        - Comentado para que no entre 2 veces por el mismo controlador al cargar la página
        - Referenciarlo en el view para que utilizar 'controller as'

      controller: 'NuevoCtrl',
        templateUrl: 'views/nuevo.html'
      });
*/
      .state('inputs', {
        url: '/inputs',
        views: {
/*
           'submenu': {
            controller:   'DashboardSettingCtrl',
            templateUrl:  'views/dashboard/settings/submenu_settings.html'
          },
*/
          'content': {
/*
             controller:   'NuevoCtrl',
*/
            templateUrl:  'views/nuevo.html'
          }
        }
      });


  }]);