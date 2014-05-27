'use strict';

angular.module('salesmanBuddyApp')
.factory('statesFactory', ['statesPath', 'genericFactory', 'usersFactory', function (statesPath, genericFactory, usersFactory) {

	var factory = {};

	factory.getAllStates = function(){
		return genericFactory.request('get', statesPath, "error getAllStates", undefined, {cache:true});
	}

	factory.getStateForId = function(id){
		return genericFactory.request('get', statesPath + '/' + id, "error getStateById: " + id, undefined, {cache:true})
	}

	return factory;
	
}]);
