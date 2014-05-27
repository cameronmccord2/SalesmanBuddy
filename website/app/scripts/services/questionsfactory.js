'use strict';

angular.module('salesmanBuddyApp')
.factory('questionsFactory', ['questionsPath', 'genericFactory', 'usersFactory', function (questionsPath, genericFactory, usersFactory) {

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
	
}]);
