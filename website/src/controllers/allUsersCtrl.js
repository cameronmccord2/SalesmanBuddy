function allUsersCtrl($scope, usersFactory){

	$scope.loading = true;
	$scope.cache = {};

	$scope.columns = [
		{name:'Id', realColumnName:'id', desiredWidth:100, columnType:'main', type:''},
		{name:'Dealership Id', realColumnName:'dealershipId', desiredWidth:100, columnType:'main', type:''},
		{name:'Device Type', realColumnName:'deviceType', desiredWidth:100, columnType:'main', type:''},
		{name:'Type', realColumnName:'type', desiredWidth:100, columnType:'main', type:''},
		{name:'Created', realColumnName:'created', desiredWidth:100, columnType:'main', type:'date'},
		{name:'Google User Id', realColumnName:'googleUserId', desiredWidth:100, columnType:'main', type:''}
	];

	$scope.types = [1, 2, 3, 4];

	usersFactory.getAllUsers().then(function(users){
		$scope.users = users;
		$scope.loading = false;
	}, function(data){
		console.log(data)
	});

	$scope.changeUserToType = function(index, type){
		usersFactory.updateUserToType($scope.users[index].googleUserId, type).then(function(data){
			$scope.users[index] = data;
		});
	}

	$scope.getAdjustedColumnWidth = function(width, columns, rowWidth){
		var count = 0;
		for (var i = columns.length - 1; i >= 0; i--) {
			count += columns[i].desiredWidth;
		};
		var obj = {width: width * rowWidth / count + "px"};
		console.log(obj)
		return obj;

	}

	$scope.showDetails = function(user){
		// if(user.showDetails){// close the clicked missionary
		// 	error.shownStackTraces = new Array();
		// 	error.showDetails = false;
		// 	return;
		// }
		// for (var i = $scope.errors.length - 1; i >= 0; i--) {// close all other details
		// 	$scope.errors[i].shownStackTraces = new Array();
		// 	$scope.errors[i].showDetails = false;
		// };
		// error.shownStackTraces = error.stackTraceList;// This makes it so the dom doesnt try to render all the assessments for every missionary, only have it render the ones it needs to
		// error.showDetails = true;
	}

	$scope.getColumnsForColumnType = function(type){
		if($scope.cache[type])
			return $scope.cache[type];

		$scope.cache[type] = [];
		for (var i = 0; i < $scope.columns.length; i++) {
			if($scope.columns[i].columnType == type)
				$scope.cache[type].push($scope.columns[i]);
		};
		return $scope.cache[type];
	}
}