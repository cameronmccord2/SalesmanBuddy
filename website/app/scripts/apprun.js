'use strict';

angular.module('salesmanBuddyApp')
.run(['$rootScope', '$http', 'User', 'AuthService', '$location', 'userInfoEndpoint', 'genericFactory', 'dealershipsFactory', 'accessRights', 'rightsUpperSBUser', 'rightsSalesman', 'rightsManager', 'rightsSBUser',
	 function ($rootScope, $http, User, AuthService, $location, userInfoEndpoint, genericFactory, dealershipsFactory, accessRights, rightsUpperSBUser, rightsSalesman, rightsManager, rightsSBUser) {
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
}]);






























