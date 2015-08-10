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
    'ui.bootstrap'
  ])

  /*ROUTER*/
  /*.config(function ($routeProvider) {
    $routeProvider
      .when('/', {
        templateUrl: 'views/main.html',
        controller: 'MainCtrl',
        controllerAs: 'main'
      })
      .when('/about', {
        templateUrl: 'views/about.html',
        controller: 'AboutCtrl',
        controllerAs: 'about'
      })
      .otherwise({
        redirectTo: '/'
      });
  });*/


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