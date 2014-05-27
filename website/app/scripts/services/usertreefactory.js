'use strict';

angular.module('salesmanBuddyApp')
.factory('userTreeFactory', ['userTreePath', 'genericFactory', '$q', function (userTreePath, genericFactory, $q) {

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

}]);
