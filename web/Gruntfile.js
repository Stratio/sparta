// Generated on 2015-07-27 using generator-angular 0.12.1
'use strict';

module.exports = function (grunt) {

  // Time how long tasks take. Can help when optimizing build times
  require('time-grunt')(grunt);

  // Automatically load required Grunt tasks
  require('jit-grunt')(grunt, {
    useminPrepare: 'grunt-usemin',
    ngtemplates: 'grunt-angular-templates'
  });

  // Configurable paths for the application
  var appConfig = {
    app: 'src',
    dist: 'target/classes/web'
  };

  var fs = require('fs');

  // Define the configuration for all the tasks
  grunt.initConfig({

    // Project settings
    stratio: appConfig,

    // Watches files for changes and runs tasks based on the changed files
    watch: {
      js: {
        files: ['<%= stratio.app %>/scripts/{,*/}*.js'],
        options: {
          livereload: '<%= connect.options.livereload %>'
        }
      },
      sass: {
        files: ['<%= stratio.app %>/styles/{,*/}*.{scss,sass}',
          '<%= stratio.app %>/styles/**/*.scss'
        ],
        tasks: ['sass', 'autoprefixer']
      },
      gruntfile: {
        files: ['Gruntfile.js']
      },
      livereload: {
        options: {
          livereload: '<%= connect.options.livereload %>'
        },
        files: [
          '<%= stratio.app %>/{,*/}*.html',
          '.tmp/styles/{,*/}*.css',
          '<%= stratio.app %>/images/{,*/}*.{png,jpg,jpeg,gif,webp,svg}'
        ]
      }
    },

    // The actual grunt server settings
    connect: {
      options: {
        port: 9000,
        // Change this to '0.0.0.0' to access the server from outside.
        hostname: 'localhost',
        livereload: 35729
      },
      proxies: [{
          context: '/policy', // the context of the data service
          host: '127.0.0.1', // wherever the data service is running
          port: 9090, // the port that the data service is running on,
          changeOrigin: true,
          ws: true
        },
        {
          context: '/template', // the context of the data service
          host: '127.0.0.1', // wherever the data service is running
          port: 9090, // the port that the data service is running on,
          changeOrigin: true,
          ws: true
        },
        {
          context: '/fragment', // the context of the data service
          host: '127.0.0.1', // wherever the data service is running
          port: 9090, // the port that the data service is running on,
          changeOrigin: true,
          ws: true
        },
        {
          context: '/plugins', // the context of the data service
          host: '127.0.0.1', // wherever the data service is running
          port: 9090, // the port that the data service is running on,
          changeOrigin: true,
          ws: true
        },
        {
          context: '/driver', // the context of the data service
          host: '127.0.0.1', // wherever the data service is running
          port: 9090, // the port that the data service is running on,
          changeOrigin: true,
          ws: true
        }, {
          context: '/executions', // the context of the data service
          host: '127.0.0.1', // wherever the data service is running
          port: 9090, // the port that the data service is running on,
          changeOrigin: true,
          ws: true
        }

      ],
      livereload: {
        options: {
          open: true,
          middleware: function (connect) {
            return [
              connect.static('.tmp'),
              connect().use(
                '/node_modules',
                connect.static('./node_modules')
              ),
              connect().use(
                '/app/styles',
                connect.static('./app/styles')
              ),
              connect.static(appConfig.app),
              require('grunt-connect-proxy/lib/utils').proxyRequest
            ];
          }
        }
      },
      test: {
        options: {
          port: 9001,
          middleware: function (connect) {
            return [
              connect.static('.tmp'),
              connect.static('test'),
              connect().use(
                '/node_modules',
                connect.static('./node_modules')
              ),
              connect.static(appConfig.app)
            ];
          }
        }
      },
      dist: {
        options: {
          open: true,
          base: '<%= stratio.dist %>'
        }
      }
    },

    // Empties folders to start fresh
    clean: {
      dist: {
        files: [{
          dot: true,
          src: [
            '.tmp',
            '<%= stratio.dist %>/{,*/}*',
            '!<%= stratio.dist %>/.git{,*/}*'
          ]
        }]
      },
      server: ['.tmp', '<%= stratio.dist %>']
    },

    // Add vendor prefixed styles
    autoprefixer: {
      options: {
        browsers: ['last 1 version']
      },
      server: {
        options: {
          map: true,
        },
        files: [{
          expand: true,
          cwd: '.tmp/styles/',
          src: '{,*/}*.css',
          dest: '.tmp/styles/'
        }]
      },
      dist: {
        files: [{
          expand: true,
          cwd: '.tmp/styles/',
          src: '{,*/}*.css',
          dest: '.tmp/styles/'
        }]
      }
    },

    // Automatically inject Bower components into the app
    wiredep: {
      app: {
        src: ['<%= stratio.app %>/index.html'],
        ignorePath: /\.\.\//
      }
    },

    sass: {
      options: {
        sourceMap: true
      },
      dist: {
        files: {
          '.tmp/styles/main.css': '<%= stratio.app %>/styles/main.scss'
        }
      },
      server: {
        options: {
          debugInfo: true
        },
        files: {
          '.tmp/styles/main.css': '<%= stratio.app %>/styles/main.scss'
        }
      }
    },

    // Reads HTML for usemin blocks to enable smart builds that automatically
    // concat, minify and revision files. Creates configurations in memory so
    // additional tasks can operate on them
    useminPrepare: {
      html: '<%= stratio.app %>/index.html',
      options: {
        dest: '<%= stratio.dist %>',
        flow: {
          html: {
            steps: {
              js: ['concat', 'uglifyjs'] //,
              //css: ['cssmin']
            },
            post: {}
          }
        }
      }
    },

    // Performs rewrites based on filerev and the useminPrepare configuration
    usemin: {
      html: ['<%= stratio.dist %>/{,*/}*.html'],
      css: ['<%= stratio.dist %>/styles/{,*/}*.css'],
      js: ['<%= stratio.dist %>/{,*/}*.js'],
      options: {
        assetsDirs: [
          '<%= stratio.dist %>',
          '<%= stratio.dist %>/images',
          '<%= stratio.dist %>/styles'
        ],
        patterns: {
          js: [
            [/(images\/[^''""]*\.(png|jpg|jpeg|gif|webp|svg))/g, 'Replacing references to images']
          ]
        }
      }
    },

    concat: {
      options: {
        sourceMap: true
      },
      dist: {}
    },
    ngtemplates: {
      dist: {
        options: {
          module: 'webApp',
          htmlmin: '<%= htmlmin.dist.options %>',
          usemin: 'scripts/scripts.js'
        },
        cwd: '<%= stratio.app %>',
        src: ['templates/**/*.html', 'views/**/*.html', 'stratio-ui/**/*.html'],
        dest: '.tmp/templateCache.js'
      }
    },
    htmlmin: {
      dist: {
        options: {
          collapseWhitespace: true,
          conservativeCollapse: true,
          collapseBooleanAttributes: true,
          removeCommentsFromCDATA: true
        },
        files: [{
          expand: true,
          cwd: '<%= stratio.dist %>',
          src: ['*.html', 'views/**/*.html', 'stratio-ui/**/*.html'],
          dest: '<%= stratio.dist %>'
        }]
      }
    },

    // ng-annotate tries to make the code safe for minification automatically
    // by using the Angular long form for dependency injection.
    ngAnnotate: {
      dist: {
        files: [{
          expand: true,
          cwd: '.tmp/concat/scripts',
          src: '*.js',
          dest: '.tmp/concat/scripts'
        }]
      }
    },

    // Copies remaining files to places other tasks can use
    copy: {
      dist: {
        files: [{
          expand: true,
          dot: true,
          cwd: '<%= stratio.app %>',
          dest: '<%= stratio.dist %>',
          src: [
            '*.{ico,png,txt}',
            '.htaccess',
            '*.html',
            'views/**/*.html',
            'stratio-ui/**/*.*',
            '!stratio-ui/**/*.js',
            '!stratio-ui/**/*.scss',
            //'images/{,*/}*.{webp}',
            'images/**.*',
            'styles/vendors/{,*/}*.*',
            '!styles/vendors/{,*/}*.scss',
            'languages/{,*/}*.*',
            'data-templates/**/*.*',
            '!data-templates/fake_data/*.*'
          ]
        }, {
          expand: true,
          cwd: '.tmp/images',
          dest: '<%= stratio.dist %>/images',
          src: ['generated/*']
        }, {
          expand: true,
          cwd: '.tmp/styles',
          dest: '<%= stratio.dist %>/styles',
          src: ['**/{,*/}*.css']
        }, {
          expand: true,
          cwd: '.tmp/concat/scripts',
          dest: '<%= stratio.dist %>/scripts',
          src: ['**/{,*/}*.js', '**/{,*/}*.js.map']
        }]
      },
      styles: {
        expand: true,
        cwd: '<%= stratio.app %>/styles',
        dest: '.tmp/styles/',
        src: '{,*/}*.css'
      },
      scripts: {
        expand: true,
        cwd: '.tmp/concat/scripts',
        dest: '<%= stratio.dist %>/scripts',
        src: '{,*/}*.js'
      }
    },

    // Run some tasks in parallel to speed up the build process
    concurrent: {
      server: [
        'sass:server'
      ],
      test: [
        'sass'
      ],
      dist: [
        'sass:dist'
      ]
    }
  });

  grunt.registerTask('serve', 'Compile then start a connect web server', function (target) {
    grunt.loadNpmTasks('grunt-connect-proxy');
    if (target === 'dist') {
      return grunt.task.run(['build', 'connect:dist:keepalive']);
    }

    grunt.task.run([
      'clean:server',
      'concurrent:server',
      'configureProxies:server',
      'autoprefixer:server',
      'connect:livereload',
      'watch'
    ]);
  });

  grunt.registerTask('server', 'DEPRECATED TASK. Use the "serve" task instead', function (target) {
    grunt.log.warn('The `server` task has been deprecated. Use `grunt serve` to start a server.');
    grunt.task.run(['serve:' + target]);
  });

  grunt.registerTask('test', [
    'concurrent:test',
    'autoprefixer',
    'connect:test'
  ]);

  grunt.registerTask('build', 'Build if target is empty', function () {
    if (fs.existsSync(appConfig.dist + '/index.html')) {
      grunt.log.warn('Target already exists. If you want to force build run task `clean:dist` first');
      return true;
    }
    grunt.loadNpmTasks('grunt-angular-templates');

    grunt.task.run([
      'clean:server',
      'useminPrepare',
      'concurrent:dist',
      'autoprefixer',
     // 'ngtemplates',
      'concat',
      'ngAnnotate',
      'copy:dist',
      'usemin',
      'htmlmin'
    ]);
  });

  grunt.registerTask('default', [
    'test',
    'build'
  ]);
};
