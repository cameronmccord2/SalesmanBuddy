var app = angular.module('SALESMANBUDDYADMIN', ['ngRoute', 'AuthenticationService']);

// TODO new user managers don't get assigned to type 2

app.constant("baseUrl", "http://salesmanbuddyserver.elasticbeanstalk.com/v1/salesmanbuddy/");
// app.constant("baseUrl", "http://localhost:8080/salesmanBuddy/v1/salesmanbuddy/");
app.constant("baseUrlAWS", "http://salesmanbuddyserver.elasticbeanstalk.com/v1/salesmanbuddy/");
app.constant("baseUrlLocal", "http://localhost:8080/salesmanBuddy/v1/salesmanbuddy/");
app.constant("clientId", "38235450166-qo0e12u92l86qa0h6o93hc2pau6lqkei.apps.googleusercontent.com");
app.constant("userInfoEndpoint", "users/me");
app.constant("usersPath", "users");
app.constant("dealershipsPath", "dealerships");
app.constant("statesPath", "states");
app.constant("licensesPath", "licenses");
app.constant("questionsPath", "questions");
app.constant("saveDataPath", "savedata");
app.constant("userExistsPath", "userExists");
app.constant("licenseImagePath", "licenseimage");
app.constant("userTreePath", "userTree");
app.constant("errorPath", "error");
app.constant("stockNumbersPath", "stockNumbers");
app.constant("reportsPath", "reports");
app.constant("accessRights", {salesman:1, manager:2, sbUser:3, upperSBUser:4});// page:required type level
app.constant("rightsSBUser", 'sbUser');// same as values in accessRights
app.constant("rightsManager", 'manager');
app.constant("rightsSalesman", 'salesman');
app.constant("rightsUpperSBUser", 'upperSBUser');


app.config(['$routeProvider', '$locationProvider', 'AuthServiceProvider', function($routeProvider, $locationProvider, AuthServiceProvider) {
  $routeProvider.
	// when('/comingSoon', { templateUrl: 'templates/comingSoon.html', controller: comingSoonCtrl, resolve: AuthServiceProvider.waitForLogin}).
	when('/home', { templateUrl: 'templates/home.html', controller: homeCtrl, resolve: AuthServiceProvider.waitForLogin }).
	when('/faq', { templateUrl: 'templates/help.html', controller: helpCtrl, resolve: AuthServiceProvider.waitForLogin }).
	when('/loggedOut', { templateUrl: 'templates/loggedOut.html', controller: loggedOutCtrl, resolve: AuthServiceProvider.waitForLogin }).
	when('/contactUs', { templateUrl: 'templates/contactUs.html', controller: contactUsCtrl, resolve: AuthServiceProvider.waitForLogin }).
	// when('/allUsers', { templateUrl: 'templates/allUsers.html', controller: allUsersCtrl, resolve: AuthServiceProvider.waitForLogin }).
	when('/testDrives', { templateUrl: 'templates/licensesList.html', controller: licensesListCtrl, resolve: AuthServiceProvider.waitForLogin }).
	// when('/dealershipManager', { templateUrl: 'templates/dealershipManager.html', controller: dealershipManagerCtrl, resolve: AuthServiceProvider.waitForLogin }).
	when('/newUser/:dealershipCode/:userType', { templateUrl: 'templates/newUser.html', controller: newUserCtrl, resolve: AuthServiceProvider.waitForLogin }).
	when('/pricing', {templateUrl: 'templates/pricing.html', resolve: AuthServiceProvider.waitForLogin}).
	when('/how', {templateUrl: 'templates/how.html', resolve: AuthServiceProvider.waitForLogin}).
	// when('/reportsManager', {templateUrl:'templates/reportsManager.html', controller: reportsManagerCtrl, resolve: AuthServiceProvider.waitForLogin }).

	// new routes
	when('/sbAdmin', {redirectTo:'/sbAdmin/dealerships'}).
	when('/sbAdmin/dealerships', { templateUrl: 'templates/dealerships.html', controller: dealershipsCtrl, resolve: AuthServiceProvider.waitForLogin }).
	when('/sbAdmin/testDrives', { templateUrl: 'templates/licensesList.html', controller: licensesListCtrl, resolve: AuthServiceProvider.waitForLogin }).
	when('/sbAdmin/users', { templateUrl: 'templates/allUsers.html', controller: allUsersCtrl, resolve: AuthServiceProvider.waitForLogin }).
	when('/sbAdmin/stockNumbers', { templateUrl: 'templates/stockNumbers.html', controller: stockNumbersCtrl, resolve: AuthServiceProvider.waitForLogin }).
	when('/sbAdmin/reports', {templateUrl:'templates/reportsManager.html', controller: reportsManagerCtrl, resolve: AuthServiceProvider.waitForLogin }).
	when('/sbAdmin/dashboard', {templateUrl:'templates/dashboard.html', controller: dashboardCtrl, resolve: AuthServiceProvider.waitForLogin }).

	when('/manager', {redirectTo:'/manager/reports'}).
	when('/manager/reports', {templateUrl:'templates/reportsManager.html', controller: reportsManagerCtrl, resolve: AuthServiceProvider.waitForLogin }).
	when('/manager/stockNumbers', {templateUrl:'templates/stockNumbers.html', controller: stockNumbersCtrl, resolve: AuthServiceProvider.waitForLogin }).
	// when('/manager/users', {templateUrl:'templates/mUsers.html', controller: mUsersCtrl, resolve: AuthServiceProvider.waitForLogin }).
	// when('/manager/testDrives', {templateUrl:'templates/mTestDrives.html', controller: mTestDrivesCtrl, resolve: AuthServiceProvider.waitForLogin }).
	when('/manager/dashboard', {templateUrl:'templates/dashboard.html', controller: dashboardCtrl, resolve: AuthServiceProvider.waitForLogin }).

	otherwise({ redirectTo: '/home' });
}]);

app.config(['AuthServiceProvider', 'clientId', 'baseUrlAWS', 'baseUrlLocal', function(AuthServiceProvider, clientId, baseUrlAWS, baseUrlLocal){
	function determineBaseUrl() {// allows us to not have to change the client id every time we move environments
		if(window.location.host == 'localhost:8080')
			return baseUrlLocal;
		else if(window.location.host == 'salesmanbuddy.com')
			return baseUrlAWS;

		throw new Error("App is running in an unknown environment. Please check URL in browser");
	}
		AuthServiceProvider.setRefreshUrl(determineBaseUrl() + 'refreshToken');
		AuthServiceProvider.setCodeForTokenUrl(determineBaseUrl() + 'codeForToken');
		AuthServiceProvider.setClientID(clientId);
		AuthServiceProvider.pushScope('https://www.googleapis.com/auth/plus.me');
		AuthServiceProvider.pushScope('email');
		AuthServiceProvider.pushScope('profile');
		// AuthServiceProvider.setRedirectURI('http://localhost:8080/salesmanBuddyAdmin');

		// this only supports matching the first part of the path without any /
		AuthServiceProvider.pushNonAuthenticatedPath("comingSoon");
		AuthServiceProvider.pushNonAuthenticatedPath("home");
		AuthServiceProvider.pushNonAuthenticatedPath("faq");
		AuthServiceProvider.pushNonAuthenticatedPath("contactUs");
		AuthServiceProvider.pushNonAuthenticatedPath("loggedOut");
		AuthServiceProvider.pushNonAuthenticatedPath("pricing");
		AuthServiceProvider.pushNonAuthenticatedPath("how");
}]);

app.config(function($sceProvider) {
	// Completely disable SCE.  For demonstration purposes only!
	// Do not use in new projects.
	$sceProvider.enabled(false);
});


app.factory('genericFactory', function($http, $q, baseUrlLocal, baseUrlAWS){
	var factory = {};
	factory.request = function(verb, url, errorMessage, object, options, dontAddBaseUrl){
		var defer = $q.defer();
		verb = verb.toLowerCase();
		if(!dontAddBaseUrl)
			url = factory.getBaseUrl() + url;

		if(verb == 'get' || verb == 'delete' || verb == 'head' || verb == 'jsonp'){
			$http[verb](url, options).success(function(data){
				defer.resolve(data);
			}).error(function(data, status, headers, config){
				console.log(errorMessage || "", data, status, headers, config);
				defer.reject(data);
			});
			return defer.promise;

		}else if(verb == 'put' || verb == 'post'){
			
			$http[verb](url, object, options).success(function(data){
				defer.resolve(data);
			}).error(function(data, status, headers, config){
				console.log(errorMessage || "", data, status, headers, config);
				defer.reject(data);
			});
			return defer.promise;
		}
		else
			console.log("you need to specify a valid restful verb");
	}

	factory.getBaseUrl = function(){
		if(window.location.host == 'localhost:8080')
			return baseUrlLocal;
		else if(window.location.host == 'salesmanbuddy.com')
			return baseUrlAWS;

		throw new Error("App is running in an unknown environment. Please check URL in browser");
	}
	return factory;
});

// app.factory('genericFactory', ['$http', '$q', 'User', 'mtc.api.config', function($http, $q, User, apiconfig){
// 	var factory = {};
// 	factory.request = function(verb, url, errorMessage, object, options){
// 		var defer = $q.defer();
// 		verb = verb.toLowerCase();

// 		if(verb == 'get' || verb == 'delete' || verb == 'head' || verb == 'jsonp'){
// 			$http[verb](url, options).success(function(data){
// 				defer.resolve(data);
// 			}).error(function(data, status, headers, config){
// 				console.log(errorMessage || "", data, status, headers, config);
// 				defer.reject(data);
// 			});
// 			return defer.promise;

// 		}else if(verb == 'put' || verb == 'post'){
			
// 			$http[verb](url, object, options).success(function(data){
// 				defer.resolve(data);
// 			}).error(function(data, status, headers, config){
// 				console.log(errorMessage || "", data, status, headers, config);
// 				defer.reject(data);
// 			});
// 			return defer.promise;
// 		}
// 		else
// 			console.log("you need to specify a valid restful verb");
// 	}

// 	factory.getIndicesOf = function(searchStr, str, caseSensitive) {
// 	    var startIndex = 0, searchStrLen = searchStr.length;
// 	    var index, indices = [];
// 	    if (!caseSensitive) {
// 	        str = str.toLowerCase();
// 	        searchStr = searchStr.toLowerCase();
// 	    }
// 	    while ((index = str.indexOf(searchStr, startIndex)) > -1) {
// 	        indices.push(index);
// 	        startIndex = index + searchStrLen;
// 	    }
// 	    return indices;
// 	}

// 	factory.allPromisesRequest = function(promises, verb, url, errorMessage, object, options){
// 		var defer = $q.defer();

// 		$q.all(promises).then(function(resolutions) {

// 			var variables = url.match(/:[^/]*;/g);// :api;/users/:user;/:users; - would find 3 variables to replace: api, user, users
// 			for (var i = variables.length - 1; i >= 0; i--) {

// 				var resolutionVariable = variables[i].split(":")[1].split(";")[0];// strip off : and ;
// 				var replaceString = variables[i];
// 				var indicies = factory.getIndicesOf(replaceString, url, true);

// 				for (var j = indicies.length - 1; j >= 0; j--) {// number of replacements need to do
// 					url = url.replace(replaceString, resolutions[resolutionVariable]);
// 				};
// 			};
			
// 			factory.request(verb, url, errorMessage).then(function(data){
// 				defer.resolve(data);
// 			}, function(data){
// 				defer.reject(data);// the error has already been logged to the console so no need to do it here too, just pass on the reject if they want it
// 			});
// 		}, function(data){
// 			console.log("the $q.all(promises) responded with a reject, somewhere your promises rejected somehow, you specified error message: " + errorMessage);
// 			defer.reject(data);
// 		});
// 		return defer.promise;
// 	}

// 	factory.getUserId = function(){
// 		var defer = $q.defer();
// 		User.initUser().then(function(){
// 			defer.resolve(User.getUser().user.id);
// 		}, function(){
// 			defer.reject('init user failed');
// 		});
// 		return defer.promise;
// 	}

// 	factory.apiPromiseForKey = function(key){
// 		return {
// 			'api':apiconfig.getPrefix(key)
// 		};
// 	}
// 	return factory;
// }]);

app.factory('usersFactory',function(usersPath, genericFactory, $http, $q, $window){
	var factory = {};

	factory.userExists = function(){
		console.log("running user extists")

		var defer = $q.defer();

		var user = {
			deviceType:2,//1:ios, 2:web, 3:android, this is used to know what client the refresh token is associated with
			// type:1, hard coded in the server to give them 1, that can be changed in the all users view in the admin
			refreshToken: $window.sessionStorage.refreshToken || ""
		};
		if(user.refreshToken && user.refreshToken.length > 0){
			$http.put(genericFactory.getBaseUrl() + usersPath + '/userExists', user, {cache:true}).success(function(data){
				defer.resolve(data);
			}).error(function(data, status, headers, config){
				console.log('error userExists', data, status, headers, config);
				defer.reject(data);
			});
		}else{
			defer.reject();
			console.log("refresh token wasnt on the object")
		}
		return defer.promise;
	}

	factory.getAllUsers = function(){
		return genericFactory.request('get', usersPath, "error getAllUsers");
	}

	factory.updateUserToType = function(googleUserId, type){
		var options = {
			params:{
				type:type
			}
		};
		return genericFactory.request('post', usersPath + "/" + googleUserId, "error updateUserToType", null, options);
	}

	factory.updateUserToDealershipCode = function(googleUserId, dealershipCode, type){
		var options = {
			params:{
				dealershipcode:dealershipCode,
				type:type || 0
			}
		};
		// updates the specified googleUserId or "" meaning themselves
		return genericFactory.request('post', usersPath + "/" + (googleUserId || ""), "error updateUserToDealershipCode", undefined, options);
	}

	factory.getNameForUser = function(googleUserId){
		return genericFactory.request('get', usersPath + "/" + googleUserId + "/google/name", "errorgetNameForUser: " + googleUserId, undefined, {cache:true})
	}

	factory.getUsersForDealershipId = function(dealershipId){
		var options = {
			params:{
				dealershipId:dealershipId
			},
			cache:true
		};
		return genericFactory.request('get', usersPath, "error getUsersForDealershipId: " + dealershipId, undefined, options);
	}

	return factory;
});

app.factory('dealershipsFactory',function(dealershipsPath, genericFactory, usersFactory){
	var factory = {};

	factory.getAllDealerships = function(){
		return genericFactory.request('get', dealershipsPath, "error getAllDealerships");
	}

	factory.newDealership = function(dealership){
		return genericFactory.request('put', dealershipsPath, "error newDealership", dealership);
	}

	factory.updateDealership = function(dealership){
		return genericFactory.request('post', dealershipsPath, "error updateDealership", dealership);
	}

	factory.getDealershipForId = function(id){
		return genericFactory.request('get', dealershipsPath + "/" + id, "error getDealershipForId: " + id, undefined, {cache:true});
	}

	factory.getDealershipForCode = function(dealershipCode){
		var options = {
			params:{
				dealershipCode:dealershipCode
			}
		};
		return genericFactory.request('get', dealershipsPath, 'error getDealershipForCode: ' + dealershipCode, undefined, options);
	}

	return factory;
});

app.factory('errorFactory',function(errorPath, genericFactory, User, $location, $q){
	var factory = {};

	var sendError = function(message){
		return genericFactory.request("put", errorPath, 'error sendError' + message, message);
	}

	factory.sendErrorToSalesmanBuddy = function(message){
		var defer = $q.defer();
		message += "\n\nLocation Data: " + $location.path();
		User.initUser().then(function(data){
			sendError(message + "\n\nUser Data: " + angular.toJson(User.getUser())).then(function(data){
				defer.resolve(data);
			});
		}, function(data){
			sendError(message + "\n\nNo User Data").then(function(data){
				defer.resolve(data);
			});
		});
		return defer.promise;
	}

	return factory;
});

app.factory('reportsFactory',function(reportsPath, genericFactory){
	var factory = {};

	factory.requestReportNow = function(reportType, email, dealershipId){
		var options = {
			params:{
				reportType:reportType,
				email:email,
				dealershipId:dealershipId
			}
		};
		return genericFactory.request('put', reportsPath, "error sendReportNow", {}, options);
	}

	return factory;
});

app.factory('userTreeFactory',function(userTreePath, genericFactory, $q){
	var factory = {};

	factory.getAllUserTrees = function(){
		return genericFactory.request('get', userTreePath, "error getAllUserTrees");
	}

	factory.getUserTreesForGoogleUserId = function(googleUserId){
		var options = {
			params:{
				googleUserId:googleUserId
			}
		};
		return genericFactory.request('get', userTreePath, "error getUserTreesForGoogleUserId", undefined, options);
	}

	factory.getUserTreesForGoogleSupervisorId = function(googleSupervisorId){
		var options = {
			params:{
				googleSupervisorId:googleSupervisorId
			}
		};
		return genericFactory.request('get', userTreePath, "error getUserTreesForGoogleSupervisorId", undefined, options);
	}

	factory.getUserTreesForSBUserId = function(sbUserId){
		var options = {
			params:{
				sbUserId:sbUserId
			}
		};
		return genericFactory.request('get', userTreePath, "error getUserTreesForSBUserId", undefined, options);
	}

	factory.getUserTreesForDealershipId = function(dealershipId){
		var options = {
			params:{
				dealershipId:dealershipId
			}
		};
		return genericFactory.request('get', userTreePath, "error getUserTreesForDealershipId", undefined, options);
	}

	factory.deleteUserTreeById = function(id){
		var options = {
			params:{
				id:id
			}
		};
		return genericFactory.request('delete', userTreePath, 'error deleteUserTreeById: ' + id, undefined, options);
	}

	factory.newUserTree = function(userTree){
		return genericFactory.request('put', userTreePath, "error newUserTree", userTree);
	}

	factory.updateUserTree = function(userTree){
		return genericFactory.request('post', userTreePath, "error updateUserTree", userTree);
	}

	factory.saveUserTree = function(userTree){
		var defer = $q.defer();
		userTree.type = userTree.typeObject.value;
		userTree.userId = userTree.user.googleUserId;
		if(userTree.supervisor)
			userTree.supervisorId = userTree.supervisor.googleUserId;

		if(userTree.id)
			factory.updateUserTree(userTree).then(function(data){
				defer.resolve(data);
			});
		else
			factory.newUserTree(userTree).then(function(data){
				userTree.created = data.created;
				userTree.id = data.id;
				defer.resolve(data);
			});
		return defer.promise;
	}

	return factory;
});

app.factory('statesFactory',function(statesPath, genericFactory, usersFactory){
	var factory = {};

	factory.getAllStates = function(){
		return genericFactory.request('get', statesPath, "error getAllStates", undefined, {cache:true});
	}

	factory.getStateForId = function(id){
		return genericFactory.request('get', statesPath + '/' + id, "error getStateById: " + id, undefined, {cache:true})
	}

	return factory;
});

app.factory('licensesFactory',function(licensesPath, genericFactory, usersFactory){
	var factory = {};

	factory.getAllLicenses = function(){
		var options = {
			params:{
				all:true
			}
		};
		return genericFactory.request('get', licensesPath, "error getting all licenses", undefined, options);
	}

	factory.getAllLicensesForUser = function(googleUserId){
		var options = null;
		if(googleUserId){
			options = {
				params:{
					googleUserId:googleUserId
				}
			};
		}
		return genericFactory.request('get', licensesPath, "error getAllLicensesForUser", undefined, options);
	}

	factory.getAllLicensesForDealership = function(dealershipId){
		var options = null;
		if(dealershipId){
			options = {
				params:{
					dealershipId:dealershipId
				}
			};
		}
		return genericFactory.request('get', licensesPath, "error getAllLicensesForDealership", undefined, options);
	}

	factory.putNewLicense = function(license){
		// not implemented for admin
	}

	factory.updateLicense = function(license){
		// not implemented for admin
	}

	factory.deleteLicense = function(licenseId){
		// not implemented for admin
	}

	factory.getFirstNameFromLicense = function(license){
		return factory.getAnswerTextForTag(license, 3);
	}

	factory.getLastNameFromLicense = function(license){
		return factory.getAnswerTextForTag(license, 4);
	}
	
	factory.getPhoneNumberFromLicense = function(license){
		return factory.getAnswerTextForTag(license, 5);
	}
	
	factory.getStockNumberFromLicense = function(license){
		return factory.getAnswerTextForTag(license, 2);
	}

	factory.getAnswerTextForTag = function(license, tag){
		for (var i = license.qaas.length - 1; i >= 0; i--) {
			if(license.qaas[i].question.tag == tag)
				return license.qaas[i].answer.answerText;
		};
		return "";
	}

	return factory;
});

app.factory('questionsFactory',function(questionsPath, genericFactory, usersFactory){
	var factory = {};

	factory.getAllQuestions = function(){
		// usersFactory.userExists();
		return genericFactory.request('get', questionsPath, "error getAllQuestions");
	}

	factory.putNewQuestion = function(question){
		// not implemented in admin
	}

	factory.updateQuestion = function(question){
		// not implemented in admin
	}

	return factory;
});

app.factory('licenseImageFactory',function(licenseImagePath, saveDataPath, genericFactory, usersFactory){
	var factory = {};

	factory.getLicenseImageForAnswerId = function(answerId){
		// usersFactory.userExists();
		var options = {
			params:{
				answerid:answerId
			}
		};
		return genericFactory.request('get', licenseImagePath, "error getLicenseImageForAnswerId");
	}

	factory.saveLicenseImage = function(){
		// not implemented in admin
	}

	return factory;
});

app.factory('stockNumbersFactory',function(stockNumbersPath, genericFactory){
	var factory = {};

	factory.getAllStockNumbers = function(){
		var options = {
			params:{
				all:true
			}
		};
		return genericFactory.request('get', stockNumbersPath, 'error getAllStockNumbers', undefined, options);
	}

	factory.getStockNumbersForDealershipId = function(dealershipId){
		var options = {
			params:{
				dealershipId:dealershipId || 0
			}
		};
		return genericFactory.request('get', stockNumbersPath, 'error getStockNumbersForDealershipId: ' + dealershipId, undefined, options);
	}

	factory.getStockNumbers = function(){
		return genericFactory.request('get', stockNumbersPath, 'error getStockNumbers for me');
	}

	factory.getStockNumberById = function(id){
		return genericFactory.request('get', stockNumbersPath + '/' + id, 'error getStockNumberById: ' + id);
	}

	factory.newStockNumber = function(stockNumber, status){
		var options = {
			params:{
				stockNumber:stockNumber,
				status:status
			}
		};
		return genericFactory.request('put', stockNumbersPath, 'error putNewStockNumber: ' + angular.toJson(stockNumber), {}, options);
	}

	factory.updateStockNumber = function(stockNumber){
		return genericFactory.request('post', stockNumbersPath, 'error updateStockNumber: ' + angular.toJson(stockNumber), stockNumber);
	}

	factory.updateStockNumberStatus = function(){
		alert("finish this");
	}

	factory.deleteStockNumberById = function(id){
		return genericFactory.request('delete', stockNumbersPath, 'error deleteStockNumberById: ' + id);
	}

	return factory;
});

//******************************************
// Rootscope Setup
//********************************************
app.run(function ($rootScope, $http, User, AuthService, $location, usersFactory, $q, userInfoEndpoint, genericFactory, dealershipsFactory, accessRights, rightsUpperSBUser, rightsSalesman, rightsManager, rightsSBUser) {
	User.setUserInfoEndpoint(genericFactory.getBaseUrl() + userInfoEndpoint);
	$http.defaults.headers.common.authprovider = "google";

	$rootScope.needsToBeLoggedIn;
	$rootScope.userIsLoggedIn = false;
	$rootScope.user = null;
	$rootScope.rightsUpperSBUser = rightsUpperSBUser;
	$rootScope.rightsSBUser = rightsSBUser;
	$rootScope.rightsSalesman = rightsSalesman;
	$rootScope.rightsManager = rightsManager;

	$rootScope.logout = function(){
		$rootScope.user = null;
		User.logout();
	}

	$rootScope.goToPage = function(page){
		$location.path(page);
	}

	if(AuthService.isTokenValid()){
		User.initUser().then(function(){
			$rootScope.user = User.getUser();
			console.log(User.getUser())
		});
		// usersFactory.getGoogleUserObject().then(function(user){
		// 	$rootScope.user = user;
		// });
	}

	$rootScope.testRequest = function(){
		dealershipsFactory.getAllDealerships().then(function(data){
			console.log(data);
		});
	}

	$rootScope.doesUserHaveAccessTo = function(what, exactly){
		if(what == undefined || what.length == 0)
			return true;

		if(User.getUser()){
			var type = User.getUser().sb.type;
			// console.log(type, what, exactly);
			for (var i = what.length - 1; i >= 0; i--) {
				// console.log(accessRights[what[i]])
				if(exactly){
					if(type == accessRights[what[i]])
						return true;
				}else if(type >= accessRights[what[i]])
					return true;
			};
			
		}
		// if(exactly)
		// 	console.log("strict")
		return false;
	}

	$rootScope.isPath = function(path){
		// console.log($rootScope.isSelected(path).length)
		return $rootScope.isSelected(path).length;
	}

	$rootScope.isSelected = function(path, div){
		var currentPath = $location.path().substr(0, path.length);
		// if(path == '/sbAdmin')
		// 	console.log(currentPath, path, $location.path(), div)
		if(currentPath == path){
			if(div)
				return 'navDivSelected';
			return 'navTextSelected';
		}
		return '';
	}

	$rootScope.tabs = {
		main:[
			{name:"Home", path:"/home", auth:[]},
			{name:"How", path:"/how", auth:[]},
			{name:"Pricing", path:"/pricing", auth:[]},
			{name:"FAQ", path:"/faq", auth:[]},
			{name:"Contact Us", path:"/contactUs", auth:[]},
			{name:"My Test Drives", path:"/testDrives", auth:[rightsSalesman, rightsManager, rightsSBUser, rightsUpperSBUser], strictAuth:true},
			{name:"Manager", path:"/manager/dashboard", auth:[rightsManager], strictAuth:true, specificPath:"/manager/"},
			{name:"SB Admin", path:"/sbAdmin/dashboard", auth:[rightsSBUser, rightsUpperSBUser], strictAuth:true, specificPath:"/sbAdmin/"}
		],
		manager:[
			{name:'Reports', path:"/manager/reports", auth:[rightsManager], strictAuth:false},
			{name:'Stock Numbers', path:"/manager/stockNumbers", auth:[rightsManager], strictAuth:false},
			{name:'Users', path:"/manager/users", auth:[rightsManager], strictAuth:false},
			{name:'All Test Drives', path:"/manager/testDrives", auth:[rightsManager], strictAuth:false},
			{name:'Dashboard', path:"/manager/dashboard", auth:[rightsManager], strictAuth:false}
		],
		sbUser:[
			{name:'Reports', path:"/sbAdmin/reports", auth:[rightsSBUser], strictAuth:false},
			{name:'Dealerships', path:"/sbAdmin/dealerships", auth:[rightsSBUser], strictAuth:false},
			{name:'Users', path:"/sbAdmin/users", auth:[rightsSBUser], strictAuth:false},
			{name:'Test Drives', path:"/sbAdmin/testDrives", auth:[rightsSBUser], strictAuth:false},
			{name:'Dashboard', path:"/sbAdmin/dashboard", auth:[rightsSBUser], strictAuth:false},
			{name:'Stock Numbers', path:"/sbAdmin/stockNumbers", auth:[rightsSBUser], strictAuth:false}
		]
	}
});


















