// Karma configuration file, see link for more information
// https://karma-runner.github.io/0.13/config/configuration-file.html

console.log('******  RUNNING KARMA ******');
const path = require('path');

const args = process.env.SINGLE_TEST_RUN && process.env.SINGLE_TEST_RUN.length > 0 ? process.env.SINGLE_TEST_RUN.split(',') : [];
if (args && args.length > 0) {
   console.log('*********  You are running only tests of: ', args.slice(1).join(', '),  ' *********');
}

module.exports = function (config) {
   config.set({
      basePath: '',
      frameworks: ['jasmine', '@angular-devkit/build-angular'],
      plugins: [
         require('karma-jasmine'),
         require('karma-phantomjs-launcher'),
         require('karma-mocha-reporter'),
         require('karma-junit-reporter'),
         require('karma-jasmine-html-reporter'),
         require('karma-coverage-istanbul-reporter'),
         require('@angular-devkit/build-angular/plugins/karma')
      ],
      client: {
         clearContext: false // leave Jasmine Spec Runner output visible in browser
      },
      coverageIstanbulReporter: {
         dir: require('path').join(__dirname, 'coverage'), dir: require('path').join(__dirname, 'coverage'), reports: ['html', 'lcovonly'],
         fixWebpackSourcePaths: true
      },
      
      phantomJsLauncher: {
        exitOnResourceError: true
     },
     mochaReporter: {
        ignoreSkipped: args && args.length > 0
     },

     // the default configuration
     junitReporter: {
        outputDir: 'target/surefire-reports', // results will be saved as $outputDir/$browserName.xml
        outputFile: undefined, // if included, results will be saved as $outputDir/$browserName/$outputFile
        suite: ''
     },
      reporters: ['mocha', 'junit'],
      port: 9876,
      colors: true,
      logLevel: config.LOG_INFO,
      autoWatch: true,
      browsers: ['PhantomJS'],
      singleRun: false,
      captureTimeout: 10000,
      browserDisconnectTolerance: 3, 
      browserDisconnectTimeout : 10000,
      browserNoActivityTimeout : 10000
   })
}
