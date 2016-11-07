// Generated on 2015-08-04 using generator-angular-require 1.0.0
'use strict';

// # Globbing
// for performance reasons we're only matching one level down:
// 'test/spec/{,*/}*.js'
// use this if you want to recursively match all subfolders:
// 'test/spec/**/*.js'

module.exports = function (grunt) {

  // Load grunt tasks automatically
  require('load-grunt-tasks')(grunt);

  // Time how long tasks take. Can help when optimizing build times
  require('time-grunt')(grunt);
  
  grunt.loadNpmTasks('grunt-angular-templates');

  // Configurable paths for the application
  var appConfig = {
    app: require('./bower.json').appPath || 'app',
    dist: 'dist', 
    target: 'target/classes/static/resources'
  };

  // Define the configuration for all the tasks
  grunt.initConfig({

    // Project settings
    yeoman: appConfig,

    // Watches files for changes and runs tasks based on the changed files
    watch: {
      bower: {
        files: ['bower.json'],
        tasks: ['wiredep']
      },
      js: {
        files: ['<%= yeoman.app %>/scripts/{,*/}*.js'],
        tasks: ['newer:jshint:all'],
        options: {
          livereload: '<%= connect.options.livereload %>'
        }
      },
      jsTest: {
        files: ['test/spec/{,*/}*.js'],
        tasks: ['newer:jshint:test', 'karma']
      },
      styles: {
        files: ['<%= yeoman.app %>/styles/{,*/}*.css'],
        tasks: ['newer:copy:styles']
      },
      gruntfile: {
        files: ['Gruntfile.js']
      },
      livereload: {
        options: {
          livereload: '<%= connect.options.livereload %>'
        },
        files: [
          '<%= yeoman.app %>/{,*/}*.html',
          '.tmp/styles/{,*/}*.css',
          '<%= yeoman.app %>/images/{,*/}*.{png,jpg,jpeg,gif,webp,svg}'
        ]
      },
      java: {
    	  files: '<%= yeoman.app %>/**',
    	  tasks: ['sync:java']
      }
    },

    // The actual grunt server settings
    connect: {
      options: {
        port: 9000,
        // Change this to '0.0.0.0' to access the server from outside.
        hostname: '0.0.0.0',
        livereload: 35729
      },
      livereload: {
        options: {
          open: true,
          middleware: function (connect) {
            return [
              connect.static('.tmp'),
              connect.static('test'),
              connect().use(
                '/bower_components',
                connect.static('./bower_components')
              ),
              connect().use(
                '/app/styles',
                connect.static('./app/styles')
              ),
              connect.static(appConfig.app)
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
                '/bower_components',
                connect.static('./bower_components')
              ),
              connect.static(appConfig.app)
            ];
          }
        }
      },
      dist: {
        options: {
          open: true,
          base: '<%= yeoman.dist %>'
        }
      }
    },

    // Make sure code styles are up to par and there are no obvious mistakes
    jshint: {
      options: {
        jshintrc: '.jshintrc',
        reporter: require('jshint-stylish')
      },
      all: {
        src: [
          'Gruntfile.js',
          '<%= yeoman.app %>/scripts/{,*/}*.js'
        ]
      },
      test: {
        options: {
          jshintrc: 'test/.jshintrc'
        },
        src: ['test/spec/{,*/}*.js']
      }
    },

    // Empties folders to start fresh
    clean: {
   	  options: { force: true },
      dist: {
        files: [{
          dot: true,
          src: [
            '.tmp',
            '<%= yeoman.dist %>/{,*/}*',
            '!<%= yeoman.dist %>/.git{,*/}*',
            'target/classes/static/**/*'
          ]
        }]
      },
      server: 'target/classes/static/**/*'
    },

    // Automatically inject Bower components into the app
    wiredep: {
	    app: {
	        src: ['<%= yeoman.app %>/styles/main.css'],
	        includeSelf: true,
	        bowerJson: require('./bower.json'),        // default: require('./bower.json')
	        ignorePath:  /\.\.\//,
	        exclude: [
	          "components-font-awesome"
	        ],
	        fileTypes: {
	            // Custom config to output all css deps into a main css file using import statements
	            css: {
	                block: /(([ \t]*)\/\*\s*bower:*(\S*)\s*\*\/)(\n|\r|.)*?(\/\*\s*endbower\s*\*\/)/gi,
	                detect: {
	                    css: /@import.*url\(['"]([^'"]+)/gi
	                },
	                replace: {
	                    css: '@import url("{{filePath}}");'
	                }
	            }
	        }
	      }
    },

    // Renames files for browser caching purposes
    filerev: {
      dist: {
        src: [
          '<%= yeoman.dist %>/styles/{,*/}*.css',
          '<%= yeoman.dist %>/images/{,*/}*.{png,jpg,jpeg,gif,webp,svg}',
          '<%= yeoman.dist %>/styles/fonts/*'
        ]
      }
    },

    // Reads HTML for usemin blocks to enable smart builds that automatically
    // concat, minify and revision files. Creates configurations in memory so
    // additional tasks can operate on them
    useminPrepare: {
      html: '<%= yeoman.app %>/index.html',
      options: {
        dest: '<%= yeoman.dist %>'
      }
    },

    // Performs rewrites based on filerev and the useminPrepare configuration
    usemin: {
      html: ['<%= yeoman.dist %>/{,*/}*.html'],
      css: ['<%= yeoman.dist %>/styles/{,*/}*.css'],
      options: {
        assetsDirs: [
          '<%= yeoman.dist %>',
          '<%= yeoman.dist %>/images',
          '<%= yeoman.dist %>/styles'
        ]
      }
    },

    // The following *-min tasks will produce minified files in the dist folder
    // By default, your `index.html`'s <!-- Usemin block --> will take care of
    // minification. These next options are pre-configured if you do not wish
    // to use the Usemin blocks.
     cssmin: {
         dist: {
           files: {
             '<%= yeoman.dist %>/styles/main.css': [
               '.tmp/styles/{,*/}*.css'
             ]
           }
         }
     },
     uglify: {
       dist: {
         files: {
           '<%= yeoman.dist %>/bower_components/requirejs/require.js': [
             '<%= yeoman.dist %>/bower_components/requirejs/require.js'
           ]
         }
       }
     },

    // The following *-min tasks produce minified files in the dist folder
    imagemin: {
      dist: {
        files: [{
          expand: true,
          cwd: '<%= yeoman.app %>/images',
          src: '{,*/}*.{png,jpg,jpeg,gif,ico}',
          dest: '<%= yeoman.dist %>/images'
        }]
      }
    },
    svgmin: {
      dist: {
        files: [{
          expand: true,
          cwd: '<%= yeoman.app %>/images',
          src: '{,*/}*.svg',
          dest: '<%= yeoman.dist %>/images'
        }]
      }
    },
    htmlmin: {
      dist: {
        options: {
          collapseWhitespace: true,
          conservativeCollapse: true,
          collapseBooleanAttributes: true,
          removeCommentsFromCDATA: true,
          removeOptionalTags: true
        },
        files: [{
          expand: true,
          cwd: '<%= yeoman.dist %>',
          src: ['*.html', 'views/{,*/}*.html'],
          dest: '<%= yeoman.dist %>'
        }]
      }
    },

    // ng-annotate tries to make the code safe for minification automatically
    // by using the Angular long form for dependency injection.
    ngAnnotate: {
      dist: {
        files: [{
          expand: true,
          // cwd: '<%= yeoman.app %>/scripts',
          src: ['<%= yeoman.app %>/scripts/**/*.js'],
          dest: '.tmp'
        }]
      }
    },

    // Copies remaining files to places other tasks can use
    copy: {
      dist: {
        files: [{
          expand: true,
          dot: true,
          cwd: '<%= yeoman.app %>',
          dest: '<%= yeoman.dist %>',
          src: [
            '*.{ico,png,txt}',
            '.htaccess',
            '*.html',
            'images/{,*/}*.{webp}',
            'styles/fonts/{,*/}*.*', 
            'fonts/{,*/}*.*'
          ]
        }, {
          expand: true,
          cwd: '.',
          dest: '.tmp',
          src: ['bower_components/**/*']
        }, {
          expand: true,
          cwd: '.',
          dest: '<%= yeoman.dist %>',
          src: ['bower_components/requirejs/*']
        }, {
          expand: true,
          cwd: '.tmp/images',
          dest: '<%= yeoman.dist %>/images',
          src: ['generated/*']
        }, {
	      expand: true,
	      dot: true,
	      cwd: 'bower_components/ionic/release',
	      dest: '<%= yeoman.dist %>', 
	      src: ['fonts/*.*']
        }]
      },
      styles: {
        expand: true,
        cwd: '<%= yeoman.app %>/styles',
        dest: '.tmp/styles/',
        src: '{,*/}*.css'
      }
    },

    // Run some tasks in parallel to speed up the build process
    concurrent: {
      server: [
        'copy:styles'
      ],
      test: [
        'copy:styles'
      ],
      dist: [
        'copy:styles',
        'imagemin',
        'svgmin'
      ]
    },

    // Grunt-sass
    sass: {
      dist   : {
        // Takes every file that ends with .scss from the scss
        // directory and compile them into the css directory.
        // Also changes the extension from .scss into .css.
        // Note: file name that begins with _ are ignored automatically
        files: [
          {
            expand: true,
            cwd   : 'bower_components/ionic/scss',
            src   : '{,*/}*.scss',
            dest  : '<%= yeoman.app %>/styles',
            ext   : '.css'
          }
        ]
      },
      options: {
        sourceMap  : false,
        outputStyle: 'compressed',
        imagePath  : "../",
      }
    },
    
    // Test settings
    karma: {
      unit: {
        configFile: 'karma.conf.js',
        singleRun: true
      }
    },

    // Settings for grunt-bower-requirejs
    bowerRequirejs: {
      app: {
        rjsConfig: '<%= yeoman.app %>/scripts/main.js',
        options: {
          exclude: ['requirejs', 'json3', 'es5-shim']
        }
      },
      java: {
        rjsConfig: '<%= yeoman.target %>/scripts/main.js',
        options: {
          exclude: ['requirejs', 'json3', 'es5-shim'], 
          baseUrl: 'resources'
        }
      }
    },

    replace: {
      test: {
        src: '<%= yeoman.app %>/../test/test-main.js',
        overwrite: true,
        replacements: [{
          from: /paths: {[^}]+}/,
          to: function() {
            return require('fs').readFileSync(grunt.template.process('<%= yeoman.app %>') + '/scripts/main.js').toString().match(/paths: {[^}]+}/);
          }
        }]
      }
    },

    // r.js compile config
    requirejs: {
      dist: {
        options: {
          dir: '<%= yeoman.dist %>/scripts/',
          modules: [{
            name: 'main'
          }],
          preserveLicenseComments: false, // remove all comments
          removeCombined: true,
          baseUrl: '.tmp/<%= yeoman.app %>/scripts',
          mainConfigFile: '.tmp/<%= yeoman.app %>/scripts/main.js',
          optimize: 'uglify2',
          uglify2: {
            mangle: false
          }, 
          paths: {
              "digits": "empty:"
          }
        }
      }
    },

	ngtemplates:  {
		dist:        {
			cwd: '<%= yeoman.app %>',
			src: 'views/**/*.html',
			dest: '.tmp/<%= yeoman.app %>/scripts/templates.js',
			options: {
				prefix: '/resources/',
				module: 'djvbApp.templates', 
				bootstrap: function(module, script) {
					return "define(['angular'], function (angular) { angular.module('" + module + "', []).run(['$templateCache', function ($templateCache) {" + script + "}]);});";
				}
			}
		}
	},    
    
  sync: {
	java: {
		files: [
			{ cwd: '<%= yeoman.app %>', src: '**', dest: '<%= yeoman.target %>' }
		]
	}
  },
  bower: {
	  java: {
	    dest: '<%= yeoman.target %>/bower_components',
	    options: {
	        expand: true
	    }
	  }
	}
});

  grunt.registerTask('java', [
    'clean:server',
    'wiredep',
    'bower:java',
    'sync:java', 
    'bowerRequirejs:java',
    'watch:java'
  ]);


  grunt.registerTask('serve', 'Compile then start a connect web server', function (target) {
    if (target === 'dist') {
      return grunt.task.run(['build', 'connect:dist:keepalive']);
    }

    grunt.task.run([
      'clean:server',
      'wiredep',
      'concurrent:server',
      'connect:livereload',
      'watch'
    ]);
  });

  grunt.registerTask('server', 'DEPRECATED TASK. Use the "serve" task instead', function (target) {
    grunt.log.warn('The `server` task has been deprecated. Use `grunt serve` to start a server.');
    grunt.task.run(['serve:' + target]);
  });

  grunt.registerTask('test', [
    'clean:server',
    'bowerRequirejs:app',
    'replace:test',
    'wiredep',
    'concurrent:test',
    'connect:test'/*,
    'karma'*/
  ]);

  grunt.registerTask('build', [
    'clean:dist',
    'wiredep',
    'bowerRequirejs:app',
    'replace:test',
    //'replace:preuseminprepare',
    //'useminPrepare',
    'concurrent:dist',
    //'concat',
    'ngAnnotate',
    //'replace:postusemin',
    'copy:dist',
    'cssmin',
    'uglify',
    // 'filerev',
    //'usemin',
    'ngtemplates:dist',
    'requirejs:dist',
    'htmlmin'
  ]);

  grunt.registerTask('default', [
    //'newer:jshint',
    //'test',
    'build'
  ]);
};
