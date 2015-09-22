module.exports = function (config) {
  config.set({

    basePath: '',

    files: [
      'bower_components/jquery/jquery.js',
      'bower_components/angular/angular.js',
      'bower_components/angular-mocks/angular-mocks.js',
      'bower_components/angular-animate/angular-animate.js',
      'bower_components/angular-cookies/angular-cookies.js',
      'bower_components/angular-resource/angular-resource.js',
      'bower_components/angular-route/angular-route.js',
      'bower_components/angular-ui-router/release/angular-ui-router.js',
      'bower_components/angular-sanitize/angular-sanitize.js',
      'bower_components/angular-touch/angular-touch.js',
      'src/scripts/filters/angular-truncate-number.js',

      'src/stratio-ui/script/ui.stratio.js',
      'src/stratio-ui/script/helper/*.js',
      'src/stratio-ui/script/layout/*.js',
      'src/stratio-ui/script/components/*.js',

      'src/scripts/app.js',
      'src/scripts/vendors/*.js',
      'src/scripts/constants/**/*.js',
      'src/scripts/controllers/**/*.js',
      'src/languages/en-US.json',
      'test/**/*.js'
    ],

    autoWatch: false,

    frameworks: ['jasmine'],

    browsers: ['Chrome'],

    plugins: [
      'karma-chrome-launcher',
      'karma-jasmine',
      'karma-html2js-preprocessor',
      'karma-ng-html2js-preprocessor'
    ],

    preprocessors: {
      '**/*.html': ['html2js'],
      '**/*.json': ['html2js']
    },

    junitReporter: {
      outputFile: 'test_out/unit.xml',
      suite: 'unit'
    }

  });
};
