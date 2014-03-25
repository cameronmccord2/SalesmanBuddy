function allUsersCtrl($scope, usersFactory, dealershipsFactory){

	$scope.view = "loading";
	$scope.cache = {};

	$scope.changeUserToType = function(index, type){
		console.log('changeUserToType', index, type)
		usersFactory.updateUserToType($scope.users[index].googleUserId, type).then(function(data){
			$scope.users[index] = data;
		});
	}

	$scope.columns = [
		{name:'Id', realColumnName:'id', desiredWidth:10, columnType:'main', type:''},
		// {name:'Dealership Id', realColumnName:'dealershipId', desiredWidth:100, columnType:'main', type:''},
		{name:'Dealership', realColumnName:'dealershipName', desiredWidth:100, columnType:'main', type:''},
		{name:'Device Type', realColumnName:'deviceType', desiredWidth:100, columnType:'main', type:''},
		{name:'Type', realColumnName:'type', desiredWidth:40, columnType:'main', type:'select', options:[1, 2, 3, 4, 5], ngChange:$scope.changeUserToType},
		{name:'Created', realColumnName:'created', desiredWidth:100, columnType:'main', type:'date'},
		{name:'Google User Id', realColumnName:'googleUserId', desiredWidth:100, columnType:'main', type:''},
		{name:'Name', realColumnName:'name', desiredWidth:100, columnType:'main', type:''}
	];

	usersFactory.getAllUsers().then(function(users){
		$scope.users = users;
		$scope.view = "main";
		
		for (var i = $scope.users.length - 1; i >= 0; i--) {
			$scope.getNameForUser($scope.users[i]);
			$scope.getDealershipNameForUser($scope.users[i]);
		};
	}, function(data){
		$scope.view = "noRights";
		console.log(data)
	});

	$scope.getDealershipNameForUser = function(user){
		dealershipsFactory.getDealershipForId(user.dealershipId).then(function(dealership){
			user.dealershipName = dealership.name;
		}, function(){
			user.dealershipName = 'None';
		});
	}

	$scope.getNameForUser = function(user){
		usersFactory.getNameForUser(user.googleUserId).then(function(data){
			console.log(data);
			user.name = data.name;
		});
	}

	

	$scope.getAdjustedColumnWidth = function(width, columns, rowWidth){
		var count = 0;
		for (var i = columns.length - 1; i >= 0; i--) {
			count += columns[i].desiredWidth;
		};
		var obj = {width: width * rowWidth / count + "px"};
		// console.log(obj)
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