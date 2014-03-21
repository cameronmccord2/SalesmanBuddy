/////   MTCAuthenticationService ////////
// This will handle authentication for all apps
// To configure, include module and in app.config ...
 
//////////////////////////////////////
// IMPORTANT IMPORTANT IMPORTANT /////
//////////////////////////////////////
// YOU MUST USE angular 1.1.4 or HIGHER for this module to work properly!!!!
 
// Include in your module the dependancy ---> angular.module('someapp', ['MTCAuthenticationService'])
// Inject AuthServiceProvider in a config function
/// app.config(['AuthServiceProvider', function(AuthServiceProvider){
		// Configureable Options:
		// AuthServiceProvider.setClientID(yourClientID);
		// AuthServiceProvider.pushScope(URLYouDependOn)  Do this for each scope
 
		// NEW
		// You may now set the 'state' portion of the OAuth2 URL and the Auth Module
		// will use it as an angular route to direct your app too upon load,  you can set
		// it in the provider like so
		// MTCAuthSerivceProvider.setState("/my/special/angular/route");
		// or use it on the AuthService itself
 
		// DEPRECATED
		// The auth module will now automatically set your redirect URI and also
		// take you to angular routes in your app on redirect.  If you chooose to set
		// it manually, we will use that instead
		// AuthServiceProvider.setRedirectURI(path)
		// Only one of these 3 may be used
		// AuthServiceProvider.requireByu()  Requires a BYU login first time
		// AuthServiceProvider.encourageByu()  Asks nicely for BYU login
		// AuthServiceProvider.enforceByu()  Forces a BYU signin everytime no matter what
// }])
// NOTE:
// This module automatically requests access to the https://api.mtc.byu.edu/auth scope
// If you use this scope, it isn't necessary to push it
// Thats it.  You should be off and running
// To access the current user logged in inject the User service
// You should first call the initUser function to ensure that the token needed to
// get this user is available.  Here is a common use case
//
// User.initUser().then(function(){
//            $scope.user = User.getUser();
// })
// The User.getUser() function returns the initialized user.
// You can guaranatee that the user object will be there if you first call
// initUser and use the .then property of the returned promise

// You also have access to User.isUserInRole
// Pass it a string, and you will get a boolean telling you
// if the logged in user has that role
// Ensure the user is initialized before using...
// User.initUser().then(function(){
//	        $scope.isAdmin = User.isUserInRole("myadminrole")
// })
// You may use the built in logout function like so
// function ($scope, User) {
//            $scope.logout = User.logout;
// }
// ....
// <div ng-click="logout()"></div>

// You may also get the list of Locations that a user has rights in
// User.initUser().then(function(){
//	        var locations = User.getUserLocations();
// })
// This will be an array of mtc location ids (2010852, 2012659, etc)
// Most users will only have one, but some may have rights to multiple MTCs
var auth = angular.module('AuthenticationService', []);
// Configure service that will handle redirectURIs and ClientIds
auth.provider('AuthService', function($httpProvider){
	// Oauth Configuration object
	var oauth = {};
	oauth.url = "https://accounts.google.com/o/oauth2/auth";
	oauth.refreshTokenUrl = null;
	oauth.state = "";
	oauth.response_type = 'token';
	oauth.scope = []; // We Required Auth scope for User Object
	oauth.codeUrl = 'http://salesmanbuddyserver.elasticbeanstalk.com/v1/salesmanbuddy/codeForToken';
	oauth.byuRequired = false;
	oauth.byuEncouraged = false;
	oauth.byuEnforcedEachTime = false;
	oauth.redirect_uri = "";
	oauth.unauthenticatedPaths = [];
	var inTheMiddleOfAuthorization = function(){
		// console.log("runing")
		var path = String(window.location);
		var regex = regex = /([^&=]+)=([^&]*)/g;
		while (m = regex.exec(path.split("?")[1])) {
			var stringToCheck = decodeURIComponent(m[1]);
			if(stringToCheck == 'code' || stringToCheck == 'access_token')
				return true;
		}
		return false;
	}
	return {
		setRefreshUrl: function(url){
			oauth.refreshTokenUrl = url;
		},
		setClientID: function(id) {
			oauth.client_id = id;
		},
		setServerUrl: function(url){
			oauth.authServerUrl = url;
		},
		pushScope: function(scope) {
			oauth.scope.push(scope);
		},
		pushNonAuthenticatedPath: function(path) {
			oauth.unauthenticatedPaths.push(path)
		},
		
		waitForLogin:{
			resolveThing: function($q, $timeout){
				// console.log("herer")
				var defer = $q.defer();
				if(!inTheMiddleOfAuthorization())
					defer.resolve();
				return defer.promise;
			}
		},
		setRedirectURI: function(path) {
			oauth.redirect_uri = path;
		},
		requireByu: function() {
			oauth.byuRequired = true;
		},
		encourageByu: function() {
			oauth.byuEncouraged = true;
		},
		enforceByu: function() {
			oauth.byuEnforced = true;
		},
		setState: function(state) {
			oauth.state = state;
		},

		$get: function($window, $location, $q) {
			var token = null;
			var code = null;
			var user = {};

			var configObject = {
				isTokenValid: function() {
					// console.log(new Date(parseInt($window.sessionStorage.expiresAt)), $window.sessionStorage.accessToken)
					if ((parseInt($window.sessionStorage.expiresAt) > new Date().getTime()) && $window.sessionStorage.accessToken) {
						token = $window.sessionStorage.accessToken;
						$httpProvider.defaults.headers.common.Authorization = 'Bearer ' + token;
						return true;
					}
					return false;
				},
				needsToBeLoggedIn: function(){
					var firstPartOfCurrentPath = $location.path().split("/")[1];
					if(firstPartOfCurrentPath == undefined)// for when the path hasnt gotten anything in it yet
						return false;

					if(oauth.unauthenticatedPaths.indexOf(firstPartOfCurrentPath) == -1)// not found, require authentication
						return true;
					return false;
				},
				setToken : function(t) {
					// Save token
					token = t.access_token;
					$window.sessionStorage.sbUserId = t.user_id;
					$window.sessionStorage.accessToken = token;
					$window.sessionStorage.expiresAt = new Date(new Date().getTime() + (parseInt(t.expires_in) * 1000) - (1000 * 60 * 5)).getTime(); // Remove 5 minutes, to ensure service updates token before expiration
					// console.log(t.expires_in)
					// alert(new Date(parseInt($window.sessionStorage.expiresAt)));
					$httpProvider.defaults.headers.common.Authorization = 'Bearer ' + token;
				},
				inTheMiddleOfAuthorization: function(){
					var path = String(window.location);
					var regex = regex = /([^&=]+)=([^&]*)/g;
					while (m = regex.exec(path.split("?")[1])) {
						var stringToCheck = decodeURIComponent(m[1]);
						if(stringToCheck == 'code' || stringToCheck == 'access_token')
							return true;
					}
					return false;
				},
				setCode: function(c){
					code = c;
				},
				getToken: function() {
					return token;
				},
				getOAuthURL: function() {
					return oauth.url;
				},
				setState: function(state) {
					oauth.state = state;
				},
				setRedirectURI: function(uri) {
					oauth.redirect_uri = uri;
				},

				// retrieveToken: function() {
 
				// 	// Try to get the token from 3 different places
				// 	// First: Check route params and see if we have it there
				// 	// Second: Check sessionStorage for token
				// 	// Third: Redirect to signin and get token
				// 	// Look on route params
				// 	var params = {}, queryString = '', path = String(window.location), regex = /([^&=]+)=([^&]*)/g, m;
				// 	// remove the # added to the front of the URL
				// 	var pathChunks = path.split("#");
 
				// 	if(pathChunks.length > 1) {
 
				// 		if(pathChunks[1].charAt(0) === "/")// if hash was found, this shouldnt be undefined
				// 			pathChunks[1] = pathChunks[1].substring(1);
 
				// 		queryString = pathChunks[1];
							   
 
				// 		while (m = regex.exec(queryString)) {
				// 			params[decodeURIComponent(m[1])] = decodeURIComponent(m[2]);// save out each query param
				// 		}
			   
	 
				// 		if(params && params.access_token && params.expires_in && params.state) {
				// 			// Got token from a redirect query string
				// 			this.setToken(params);
				// 			if(params.state && params.state !== "initial")
				// 				setTimeout(function LEAVEANGULAR() {
				// 					window.location.href = pathChunks[0] + "#" + params.state;
				// 				}, 0);
				// 		}
				// 	}
 
				// 	// Look in sessionStorage, verify token in there is good
				// 	if($window.sessionStorage.accessToken && $window.sessionStorage.expiresAt && (parseInt($window.sessionStorage.expiresAt) > new Date().getTime())){
				// 		token = $window.sessionStorage.accessToken;
				// 		$httpProvider.defaults.headers.common.Authorization = 'Bearer ' + token;
				// 	}
				// 	// If checkToken is still false, and there is nothing in sessionStorage
				// 	// redirect to sign in
				// 	if (!this.checkToken()) {
				// 		var url = this.buildUrl();
				// 		$window.open(url, '_self');
				// 	}
 
				// 	return this.getToken();
				// },
				// buildUrl: function() {
				// 	function spaceDelimitScope(scopes) {
				// 		var string = '';
				// 		for (var i = scopes.length - 1; i >= 0; i--) {
				// 			var scope = scopes[i];
				// 			string += scope;
				// 			if (i != 0) { // Last one, no space
				// 				string += ' ';
				// 			}
				// 		};
				// 		return string;
				// 	}
 
				// 	var locationSplit = window.location.href.split("#");
				// 	var redirect = locationSplit[0];

				// 	if(!oauth.state)
				// 		oauth.state = encodeURIComponent(locationSplit.length > 1 ? locationSplit[1] : oauth.state);
 
				// 	// If user set the redirect URI manually, ignore the implicit angular path for redirect
				// 	if (oauth.redirect_uri === "")
				// 		oauth.redirect_uri = redirect;
				// 	var url = oauth.url;
				// 	url += '?client_id=' + oauth.client_id;
				// 	url += '&response_type=' + oauth.response_type;
				// 	url += '&redirect_uri=' + oauth.redirect_uri;
				// 	url += '&scope=' + spaceDelimitScope(oauth.scope);
				// 	url += '&state=' + oauth.state;
				// 	if (oauth.byuRequired || oauth.byuEncouraged || oauth.byuEnforced) {
								  
				// 		url += '&request_auths=';
				// 		if (oauth.byuRequired)
				// 			url += 'byurequired';
				// 		if (oauth.byuEncouraged)
				// 			url += 'byu';
				// 		if (oauth.byuEnforced)
				// 			url += 'byulogin';
				// 	}
				// 	return url;
				// },
				retrieveToken: function() {
					//http://localhost:8080/salesmanBuddyAdmin/?state=initial&code=4/cbQCkbZQQIEf5XdYVhTTG7GUcSa6.kvELHd3p2poTmmS0T3UFEsNcBBR8iQI#/comingSoon
 
					// Try to get the token from 3 different places
					// First: Check route params and see if we have it there
					// Second: Check sessionStorage for token
					// Third: Redirect to signin and get token
					// Look on route params
					var params = {}, queryString = '', path = String(window.location), regex = /([^&=]+)=([^&]*)/g, m;
					// split into redirecturi and params
					var pathChunks = path.split("?");

					if(pathChunks.length > 1) {
 
						// if(pathChunks[1].charAt(0) === "/")// if hash was found, this shouldnt be undefined
						if(pathChunks[1].charAt(0) == "#")// if hash was found, this shouldnt be undefined, getting rid of angular hash?
							pathChunks[1] = pathChunks[1].substring(1);
 
						queryString = pathChunks[1];
							   
 
						while (m = regex.exec(queryString)) {
							//					key							value
							params[decodeURIComponent(m[1])] = decodeURIComponent(m[2]).split('#')[0];// save out each query param
						}

						if(params && params.code && params.state) {
							// Got token from a redirect query string
							this.setCode(params.code);

							// if(params.state && params.state !== "initial")
								setTimeout(function LEAVEANGULAR() {// go to my server
									var url = "http://localhost:8080/salesmanBuddy/v1/salesmanbuddy/" + 'codeForToken';
									url += "?code=" + params.code + '&deviceType=2&state=' + params.state + '&redirect_uri=' + pathChunks[0].split('#')[0];
									$window.open(url, '_self');
								}, 0);
						}else
							console.log("didnt find code")

						if(params && params.access_token && params.expires_in && params.state && params.user_id) {// response from my server
							// Got token from a redirect query string
							this.setToken(params);// save it into session storage

							if(params.state){
								setTimeout(function LEAVEANGULAR() {
									window.location.href = pathChunks[0] + "#" + params.state;
								}, 0);
							}else
								alert("couldnt find state: " + params);
						}

						if(params && params.message && params.state && params.state == "error"){
							alert(params.message);
							return;
						}
					}

					// Look in sessionStorage, verify token in there is good
					if($window.sessionStorage.accessToken && $window.sessionStorage.expiresAt && (parseInt($window.sessionStorage.expiresAt) > new Date().getTime())){
						token = $window.sessionStorage.accessToken;
						$httpProvider.defaults.headers.common.Authorization = 'Bearer ' + token;
					}
					// If checkToken is still false, and there is nothing in sessionStorage
					// redirect to sign in
					if (!this.isTokenValid()) {
						var url = this.buildUrl();
						$window.open(url, '_self');
					}
 
					return this.getToken();
				},
				buildUrl: function() {
					function spaceDelimitScope(scopes) {
						var string = '';
						for (var i = scopes.length - 1; i >= 0; i--) {
							var scope = scopes[i];
							string += scope;
							if (i != 0) { // Last one, no space
								string += ' ';
							}
						};
						return string;
					}
 
					var locationSplit = window.location.href.split("#");
					var redirect = locationSplit[0];

					if(!oauth.state)
						oauth.state = encodeURIComponent(locationSplit.length > 1 ? locationSplit[1] : oauth.state);
 
					// If user set the redirect URI manually, ignore the implicit angular path for redirect
					if (oauth.redirect_uri === "")
						oauth.redirect_uri = redirect;
					var url = oauth.url;
					url += '?client_id=' + oauth.client_id;
					url += '&response_type=' + 'code';
					// url += '&redirect_uri=' + oauth.redirect_uri;
					// url += '&redirect_uri=' + location.host + "/salesmanBuddyAdmin";
					url += '&redirect_uri=' + locationSplit[0].split('?')[0];// everything before #
					url += '&scope=' + spaceDelimitScope(oauth.scope);
					url += '&state=' + oauth.state;// everything after #
					url += '&access_type=offline';
					return url;
				},
				buildRefreshTokenUrl: function(){
					var userId = $window.sessionStorage.sbUserId;
					if(userId == null || userId.length < 1)
						alert("there is a problem, the userid doesnt exist");
					return oauth.refreshTokenUrl + "?userId=" + userId;
				},
				// UTILITY FUNCTIONS FOR GETTING USER LOGGED IN, LOGGING OUT, ETC
				// DEPRECATED
				logout: function() {
					// Clear session storage
					$window.sessionStorage.accessToken = '';
					$window.sessionStorage.expiresAt = '';
					$window.open('https://auth.mtc.byu.edu/oauth2/logout', "_self");
				}
			};
			return configObject;
		}
	}
});
// Configure User Service
auth.factory('User', function(AuthService, $http, $q, $window, $location){
	var user = null; // DEPRECATED
	var googleUser = null;
	var myUser = null;
	var newUser = null;
	var confirmUserDefer = null;
	var userInfoEndpoint = "";
	var config = {
		setUserInfoEndpoint: function(endpoint){
			userInfoEndpoint = endpoint;
		},
		// Ensure user is available
		initUser: function() {
			// Return a promise that will guarantee user object is set
			var d = $q.defer();
			var t = AuthService.getToken();
			if (user == null || AUTH.needsReset) {
				// Go get the user object
				// DEPRECATED ----
				AuthService.retrieveToken();
				this.needsReset = false;
				// $http.get("https://auth.mtc.byu.edu/oauth2/tokeninfo?access_token=" + AuthService.getToken()).success(function(data,status,headers,config){
				// 	user = data;
				// 	user.id = AuthService.getToken();
				// })
				// .error(function() { d.reject() } ).then(function(){ // ---- END DEPRECATED
					// console.log("getting user")
					$http.get("https://www.googleapis.com/oauth2/v3/userinfo").success(function(data){

						googleUser = data;
						$http.get(userInfoEndpoint).success(function(data){
							myUser = data;

							user = { google:googleUser, sb:myUser };
							d.resolve(user);
						}).error(function(){
							d.reject();
						});
					}).error(function(){
						d.reject();
					});
				// });
						  
			} else {
				d.resolve(user); // We already have user
			}
			return d.promise;
		},
		
		getUser: function() {
			return user;
		},
		user: function() {
			return newUser;
		},
		needsReset: false,
		isUserInRole: function(role) {
			if (user) {
				for (var i = user.user.roles.length - 1; i >= 0; i--) {
					if (user.user.roles[i] == role)
						return true;
				};
				return false;
			}
			console.error("User is undefined, init user first");
			throw "User undefined Error";
		},
		logout: function() {
			// Clear session storage
			$window.sessionStorage.accessToken = '';
			$window.sessionStorage.expiresAt = '';
			$location.path('/home');
			// $window.open('https://auth.mtc.byu.edu/oauth2/logout', "_self");
		},
		getUserLocations: function() {
			if (user) {
				var locations = [];
				for (var i = user.user.roles.length - 1; i >= 0; i--) {
					var role = user.user.roles[i];
					if (role.indexOf("mtc-locations-") == 0) { // Role is prefixed with this
						locations.push(role.substring("mtc-locations-".length));
					}
				};
				return locations;
			}
			console.error("User is undefined, init user first");
			throw "User undefined Error";
		}
	};
	return config;
});
// Config the async function
var AUTH = {};
AUTH.async = false;
AUTH.needsReset = false;
auth.factory('Async', function($q, AuthService, $rootScope, $window){
	return {
		getTokenAsync: function(config) {
			// Go get token asynchronously
			// Update $httpProvider with new token
			// Allow config request to continue
			// Need to use OLD SCHOOL ajax request
			var deferred = $q.defer();
			var xmlhttp; // Config object
			if (window.XMLHttpRequest)
			{              // code for IE7+, Firefox, Chrome, Opera, Safari
				xmlhttp = new XMLHttpRequest();
			}
			else
			{              // code for IE6, IE5
				xmlhttp = new ActiveXObject("Microsoft.XMLHTTP");
			}
			xmlhttp.onreadystatechange = function() {
				if (xmlhttp.readyState == 4 && (xmlhttp.status == 200 || xmlhttp.status == 302))
				{
					// console.log(xmlhttp.response)
					// params.access_token && params.expires_in && params.state && params.user_id

					// var obj = eval('(' + xmlhttp.response + ')');
					var obj = JSON.parse(xmlhttp.response);
					var tokenResponse = {
						access_token: obj.accessToken,
						expires_in: obj.expiresIn
					};
					AuthService.setToken(tokenResponse);
					// Modify this header
					config.headers.Authorization = "Bearer " + AuthService.getToken();
					deferred.resolve(config);
					AUTH.needsReset = true;
					$rootScope.$apply();
				}
				else {
					// // This will error if the session is invalidated on the back end
					// // We need to show the user a message, or just redirect to login
					// // For now redirect to login
					AUTH.async = false;
					// alert("There was an error refreshing your token.  Most likely you have been inactive for over 2 hours.  You are being redirected to the authentication page");
					$window.open(AuthService.buildUrl(), "_self");
					$rootScope.$apply();
				}
			}
			xmlhttp.withCredentials = true;
			if (!AUTH.async) {
				AUTH.async = true;
				xmlhttp.open("GET", AuthService.buildRefreshTokenUrl(), true);
				xmlhttp.setRequestHeader("Accept", "application/json");
				xmlhttp.send();
			}

			return deferred.promise;
		}
	}
});
// Configure headers and interceptor middleware
auth.config(['$httpProvider', function($httpProvider){
	// Set up OAUTH headers
	$httpProvider.defaults.useXDomain = true;
	$httpProvider.defaults.headers.common['Accept'] = 'application/json, text/plain, text/html';
	// delete $httpProvider.defaults.headers.common["X-Requested-With"];
	// Intercept API reqeuests and check token
	// Only do this if we are on a valid mtc domain
	// if (location.host.indexOf("mtc.byu.edu") != -1) {
		$httpProvider.interceptors.push(function(Async, AuthService) {
			return {
				'request': function (config) {
							  
					// Check config object.  We don't want an infinite loop if we
					// are getting our new token
					// The only request that "bypasses" our interceptor is when we go to authenticate or request static content
					// Bad code, TODO: Figure out how to bypass APIs that don't require auth
					// console.log('hereasdf', config)
					if ((config.url.indexOf('https://') == -1 && config.url.indexOf('http://') == -1) || (config && config.url == AuthService.getOAuthURL()) || (config && config.url.indexOf('://api.mtc.byu.edu/mtc/') > 0)){
						// console.log("inside of here")
						return config;
					}
					else {
						// console.log("actually went here")
						// Check Token.  If all is well, proceed
						// Otherwise, suspend the request, get a new token, modify the request header
						// before sending it out, and let it fly
						// Don't stop from retrieving partials, or other assets with relative URLs
						// TODO: Figure out better way to determine if URLs are relative or not
						if (AuthService.isTokenValid() || config.url.indexOf("https://auth.mtc.byu.edu/oauth2/tokeninfo?access_token=") != -1) {// something needs to be fixed here
							// console.log("and then here", AuthService.isTokenValid())
							return config;
						}
						else {
							// console.log("bad token, getting a new one");
							if (!AuthService.isTokenValid()) {
								// console.log("it for sure isnt valid")
								return Async.getTokenAsync(config);
							}
						}
					}
				}
			}
		});
	// }
}]);
 
 
// On app Run, listen for route changes and check token
auth.run(function($rootScope, AuthService, $http, Async, $window, User){
	$http.defaults.headers.common.authprovider = "google";
	
	$rootScope.$on("$routeChangeStart",function(event, next, current){
		// console.log("start", AuthService.needsToBeLoggedIn(), !AuthService.isTokenValid())
		if(AuthService.inTheMiddleOfAuthorization()){
			// console.log("there")
			AuthService.retrieveToken();
		}else if (AuthService.needsToBeLoggedIn() && !AuthService.isTokenValid()) {// If its bad, go get it asynchronously
			// console.log("here")
			// if (location.host.indexOf("mtc.byu.edu") != -1 && $window.sessionStorage.accessToken) {
			// 	Async.getTokenAsync().then(function(){
			// 		$http.defaults.headers.common.Authorization = "Bearer " + AuthService.getToken();
			// 	});
			// }
			// else {
				// Get it refresh style
 
				AuthService.retrieveToken();
			// }
		}
	});
});























