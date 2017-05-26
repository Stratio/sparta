/*
 * Copyright (C) 2015 Stratio (http://stratio.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
    'ngResource',
    'ngRoute',
    'ui.stratio.grid',
    'ui.router',
    'StratioUI',
    'ui.bootstrap',
    'ui.bootstrap.modal',
    'ui.bootstrap.tpls',
    'pascalprecht.translate',
    'ngAnimate'
  ])

  /*** TRANSLATING ***/
  .config(['$translateProvider', function ($translateProvider) {
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
  .config(['$stateProvider', '$urlRouterProvider', '$httpProvider', function ($stateProvider, $urlRouterProvider, $httpProvider) {
    $httpProvider.interceptors.push('requestInterceptor');
    // For any unmatched url, redirect to /dashboard/inputs
    $urlRouterProvider.otherwise('/dashboard/policies');

    $stateProvider
      /*******  DASHBOARD *******/
      .state('dashboard', {
        url: '/dashboard',
        views: {
          'header': {
            templateUrl: 'views/dashboard/dashboard_header.html'
          },
          'content': {
            templateUrl: 'views/dashboard/dashboard_content.html'
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
        controller: 'PolicyListCtrl',
        controllerAs: 'policies',
        templateUrl: 'views/policies.html'
      })
      .state('dashboard.executions', {
        url: '/executions',
        controller: 'ExecutionsListCtrl',
        controllerAs: 'executions',
        templateUrl: 'views/executions.html'
      })
      .state('dashboard.resources', {
        abstract: true,
        controller: 'ResourcesCtrl',
        controllerAs: 'resources',
        url: '/resources',
        templateUrl: 'views/resources.html'
      })
      .state('dashboard.resources.plugins', {
        url: '/plugins',
        controller: 'PluginsListCtrl',
        controllerAs: 'plugins',
        templateUrl: 'views/plugins.html'
      })
      .state('dashboard.resources.drivers', {
        url: '/drivers',
        controller: 'DriversListCtrl',
        controllerAs: 'drivers',
        templateUrl: 'views/drivers.html'
      })
      .state('dashboard.resources.settings', {
        url: '/general-settings',
        controller: 'GeneralSettingsCtrl',
        controllerAs: 'settings',
        templateUrl: 'views/settings.html'
      })
      /******* POLICY WIZARD *******/
      .state('wizard', {
        url: '/wizard',

        views: {
          'header': {
            templateUrl: 'views/policy-wizard/header.html',
            controller: 'PolicyWizardHeaderCtrl',
            controllerAs: 'header'
          },
          'content': {
            templateUrl: 'views/dashboard/dashboard_content.html'
          }
        }
      })
      .state('wizard.newPolicy', {
        headerClass: 'c-header--small',
        containerClass: 'c-slider-content__backdrop base-content-wrapper--small-header',
        url: '/wizard/new_policy',
        controller: 'PolicyCtrl',
        controllerAs: 'wizard',
        templateUrl: 'views/policy-wizard/wizard-panel.html'
      })
      .state('wizard.editPolicy', {
        headerClass: 'c-header--small',
        containerClass: 'c-slider-content__backdrop base-content-wrapper--small-header',
        url: '/wizard/edit_policy/:id',
        params: {
          id: null
        },
        controller: 'PolicyCtrl',
        controllerAs: 'wizard',
        templateUrl: 'views/policy-wizard/wizard-panel.html'
      })
      /*******  SETINGS *******/
      .state('settings', {
        url: '/settings',
        views: {
          'content': {
            controller: 'SettingsCtrl',
            controllerAs: 'settings',
            templateUrl: 'views/settings/settings_content.html'
          }
        }
      })

  }])

  .run(['$rootScope', function ($rootScope) {
    $rootScope.$on('$stateChangeSuccess', function (event, toState) {
      $rootScope.headerClass = toState.headerClass;
      $rootScope.containerClass = toState.containerClass;
    });
  }]);
