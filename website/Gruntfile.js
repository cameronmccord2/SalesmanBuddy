// Generated on 2014-04-04 using generator-angular 0.7.1
'use strict';

// # Globbing
// for performance reasons we're only matching one level down:
// 'test/spec/{,*/}*.js'
// use this if you want to recursively match all subfolders:
// 'test/spec/**/*.js'

// INSTRUCTIONS AFTER CLONE
// MAKE SURE YOU HAVE node, yeoman, grunt, bower and generator-angular installed through npm
// 1. rm -rf .git/
// 2. npm install
// 3. bower install
// 4. Choose a new app name, and run `grunt renameapp:YOUR_NAME`
//		Following convention, an app name should be dash separated
//		like this `training-tool` or `mtc-convention-manager`
//		This will rename the app in multiple files, and name your angular module
//		`trainingToolApp` or `mtcConventionManagerApp`, following the convention
//		of the yeoman angular generator so any new pieces generated will
//		just work.  Please follow convention!
// 5. Set an initial version of your app by running `grunt bumpup:YOUR_VERSION`
//		Versions must be the semver standard (Google it :)
// 6. Rename the cloned folder to the name of your app
// 7. Run `grunt serve` and `grunt test` and `grunt build` to make sure everything is working properly
// 8. Run git init and do an initial commit
// 9. Add client id's to app/scripts/app.js for different environments

module.exports = function (grunt) {

	// Load grunt tasks automatically
	require('load-grunt-tasks')(grunt);

	// Time how long tasks take. Can help when optimizing build times
	require('time-grunt')(grunt);

	// New name for replacing in this app
	var newName = '';

	// Define the configuration for all the tasks
	grunt.initConfig({

		// Project settings
		yeoman: {
			// configurable paths
			app: require('./bower.json').appPath || 'app',
			dist: 'dist'
		},

		// Watches files for changes and runs tasks based on the changed files
		watch: {
			js: {
				files: ['<%= yeoman.app %>/scripts/{,*/}*.js', '<%= yeoman.app %>/version.json'],
				tasks: ['newer:jshint:all'],
				options: {
					livereload: true
				}
			},
			// If want to use less, un comment this
			
			css: {
				files: ['<%= yeoman.app %>/styles/**.less'],
				tasks: ['less:development']
			},
			bootStrapCSS: {
				files: '<%= yeoman.app %>/styles/bootStrap/**/*.less',
				tasks: ['less:bootStrap'] 
			},
			bower: {
				files: ['<%= yeoman.app %>/bower_components/**/*'],
				tasks: ['bowerInstall'],
				options: {
					livereload: true
				}
			},
			jsTest: {
				files: ['test/spec/{,*/}*.js'],
				tasks: ['newer:jshint:test', 'karma']
			},
			styles: {
				files: ['<%= yeoman.app %>/styles/{,*/}*.css'],
				tasks: ['newer:copy:styles', 'autoprefixer']
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
			}
		},

		// If want to use less, un comment this
		
		less: {
			development: {
				files: {
					'<%= yeoman.app %>/styles/main.css': '<%= yeoman.app %>/styles/main.less'
				}
			},
			production: {
				options: {
					cleancss: true
				},
				files:{
					'<%= yeoman.app %>/styles/main.css': '<%= yeoman.app %>/styles/main.less'
				}
			},
			bootStrap: {
				files: {
					'<%= yeoman.app %>/styles/bootStrap.css': '<%= yeoman.app %>/styles/bootStrap/bootstrap.less'
				}
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
			livereload: {
				options: {
					open: 'http://<%= connect.options.hostname %>:<%= connect.options.port %>',
					base: [
						'.tmp',
						'<%= yeoman.app %>'
					]
				}
			},
			test: {
				options: {
					port: 9001,
					base: [
						'.tmp',
						'test',
						'<%= yeoman.app %>'
					]
				}
			},
			dist: {
				options: {
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
			all: ['<%= yeoman.app %>/scripts/{,*/}*.js'],
			test: {
				options: {
					jshintrc: 'test/.jshintrc'
				},
				src: ['test/spec/{,*/}*.js']
			}
		},

		// Auto add bower
		bowerInstall: {
			target: {
				src: ['<%= yeoman.app %>/index.html']
			}
		},

		// Update app versions
		bumpup: {
			files: ['package.json', 'bower.json', '<%= yeoman.app %>/version.json']
		},

		// open : {
		// 	stage : {
		// 		path: 'http://stage-apps.mtc.byu.edu/sle',
		// 		app: 'Google Chrome'
		// 	},
		// 	dev : {
		// 		path: 'http://dev-apps.mtc.byu.edu/sle',
		// 		app: 'Google Chrome'
		// 	},
		// 	beta : {
		// 		path: 'http://beta-apps.mtc.byu.edu/sle',
		// 		app: 'Google Chrome'
		// 	},
		// 	test : {
		// 		path: 'http://test-apps.mtc.byu.edu/sle',
		// 		app: 'Google Chrome'
		// 	}
		// },

		// Empties folders to start fresh
		clean: {
			dist: {
				files: [{
					dot: true,
					src: [
						'.tmp',
						'<%= yeoman.dist %>/*',
						'!<%= yeoman.dist %>/.git*'
					]
				}]
			},
			server: '.tmp'
		},

		// Add vendor prefixed styles
		autoprefixer: {
			options: {
				browsers: ['last 1 version']
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

		// Renames files for browser caching purposes
		rev: {
			dist: {
				files: {
					src: [
						'<%= yeoman.dist %>/scripts/{,*/}*.js',
						'!<%= yeoman.dist %>/styles/{,*/}*.css',
						'!<%= yeoman.dist %>/images/{,*/}*.{png,jpg,jpeg,gif,webp,svg}'
					]
				}
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

		// Performs rewrites based on rev and the useminPrepare configuration
		usemin: {
			html: ['<%= yeoman.dist %>/{,*/}*.html'],
			css: ['<%= yeoman.dist %>/styles/{,*/}*.css'],
			options: {
				assetsDirs: ['<%= yeoman.dist %>']
			}
		},

		// The following *-min tasks produce minified files in the dist folder
		imagemin: {
			dist: {
				files: [{
					expand: true,
					cwd: '<%= yeoman.app %>/images',
					src: '{,*/}*.{png,jpg,jpeg,gif}',
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

		// Allow the use of non-minsafe AngularJS files. Automatically makes it
		// minsafe compatible so Uglify does not destroy the ng references
		ngmin: {
			dist: {
				files: [{
					expand: true,
					cwd: '.tmp/concat/scripts',
					src: '*.js',
					dest: '.tmp/concat/scripts'
				}]
			}
		},

		// Replace Google CDN references
		cdnify: {
			dist: {
				html: ['<%= yeoman.dist %>/*.html']
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
						'*.json',
						'prompts/**/*',
						'views/{,*/}*.html',
						'bower_components/**/*',
						'images/{,*/}*.{webp}',
						'fonts/{,*/}*','styles/fonts/{,*/}*',
						'styles/bootStrap.css'
					]
				},{
					expand: true,
					cwd: '.tmp/images',
					dest: '<%= yeoman.dist %>/images',
					src: ['generated/*']
				}, {
					expand: true,
					flatten: true,
					cwd: '<%= yeoman.app %>',
					dest: '<%= yeoman.dist %>/fonts',
					src: ['bower_components/components-font-awesome/fonts/*.*']
				}]
			},
			styles: {
				expand: true,
				cwd: '<%= yeoman.app %>/styles',
				dest: '.tmp/styles/',
				src: '{,*/}*.css'
			}
		},

		// Re-name app after pulling and cloning
		replace: {
			name: {
				src: ['app/version.json', 'app/index.html', 'app/scripts/**/*.js', 'test/spec/controllers/main.js', 'bower.json', 'package.json'],
				overwrite: true,
				replacements: [{
					from: 'grunt-ng-template-mtc',
					to: function (match) {
						return newName;
					}
				}, {
					from: 'gruntNgTemplateMtcApp',
					to: function (match) {
						var name = newName.replace(/-([a-z])/g, function (g) { return g[1].toUpperCase(); });
						return name + 'App';
					}
				}]
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

		// Test settings
		karma: {
			unit: {
				configFile: 'karma.conf.js',
				singleRun: true
			}
		}
	});


	grunt.registerTask('serve', function (target) {
		if (target === 'dist') {
			return grunt.task.run(['build', 'connect:dist:keepalive']);
		}

		grunt.task.run([
			'clean:server',
			'bowerInstall',
			'less:bootStrap',
			'less:development',
			'concurrent:server',
			'autoprefixer',
			'connect:livereload',
			'watch'
		]);
	});

	grunt.registerTask('server', function () {
		grunt.log.warn('The `server` task has been deprecated. Use `grunt serve` to start a server.');
		grunt.task.run(['serve']);
	});

	grunt.registerTask('test', [
		'clean:server',
		'concurrent:test',
		'autoprefixer',
		'connect:test',
		'karma'
	]);

	grunt.registerTask('build', [
		'clean:dist',
		'bowerInstall',
		'less:bootStrap',
		'less:development',
		'useminPrepare',
		'concurrent:dist',
		'autoprefixer',
		'concat',
		'ngmin',
		'copy:dist',
		'cdnify',
		'cssmin',
		'uglify',
		'rev',
		'usemin',
		'htmlmin'
	]);

	grunt.registerTask('default', [
		'newer:jshint',
		'test',
		'build'
	]);


	// To set version run
	// grunt bumpup:1.1.1 or some valid version number

	// For deploying to test grunt commands, and version control helper commands, uncomment these lines

	// grunt.registerTask('deploy:stage', ['default', 'sftp-deploy:stage']);
	// grunt.registerTask('deployopen:stage', ['default', 'sftp-deploy:stage', 'open:stage']);
	grunt.registerTask('bump:pre', ['bumpup:prerelease']);
	grunt.registerTask('bump:bug', ['bumpup:patch']);
	grunt.registerTask('bump:minor', ['bumpup:minor']);
	grunt.registerTask('bump:major', ['bumpup:major']);
};
