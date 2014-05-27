'use strict';

angular.module('salesmanBuddyApp')
.factory('usersFactory', ['usersPath', 'genericFactory', '$http', '$q', '$window', function (usersPath, genericFactory, $http, $q, $window) {
	
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
}]);
