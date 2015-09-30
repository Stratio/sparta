module.exports = function (config) {
  config.set({

    basePath: '',

    files: [
      'bower_components/angular/angular.js',
      'bower_components/angular-mocks/angular-mocks.js',
      'bower_components/angular-resource/angular-resource.js',
      'bower_components/angular-route/angular-route.js',
      'bower_components/angular-ui-router/release/angular-ui-router.js',
      'src/scripts/filters/angular-truncate-number.js',

      'src/stratio-ui/script/ui.stratio.js',
      'src/stratio-ui/script/helper/*.js',
      'src/stratio-ui/script/layout/*.js',
      'src/stratio-ui/script/components/*.js',

      'src/scripts/app.js',
      'src/scripts/vendors/*.js',
      'src/scripts/constants/**/*.js',
      'src/scripts/controllers/**/*.js',
      'src/scripts/services/**/*.js',
      'src/languages/en-US.json',
      // fixtures
      'test/mock/*.json',
      'test/**/*.js'
    ],

    autoWatch: false,

    frameworks: ['jasmine-jquery', 'jasmine'],

    browsers: ['PhantomJS'],

    plugins: [
      'karma-ng-json2js-preprocessor',
      'karma-phantomjs-launcher',
      'karma-jasmine',
      'karma-jasmine-jquery'
    ],

    preprocessors: {
      '**/*.html': ['ng-html2js'],
      '**/*.json': ['ng-json2js']
    },

    junitReporter: {
      outputFile: 'test_out/unit.xml',
      suite: 'unit'
    },

    ngJson2JsPreprocessor: {
      // strip this from the file path
      stripPrefix: 'test/mock/',
      // prepend this to the
      prependPrefix: 'served/'
    }

  });
};
