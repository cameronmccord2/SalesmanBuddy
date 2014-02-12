function dealershipManagerCtrl($scope, dealershipsFactory, statesFactory){

	$scope.loading = true;
	$scope.cache = {};
	$scope.states = [];
	$scope.newDealership = {};


	$scope.columns = [
		{name:'Id', realColumnName:'id', desiredWidth:100, columnType:'main', type:'', editable:false},
		{name:'Name', realColumnName:'name', desiredWidth:100, columnType:'main', type:'', editable:true},
		{name:'City', realColumnName:'city', desiredWidth:100, columnType:'main', type:'', editable:true},
		{name:'State Id', realColumnName:'stateId', desiredWidth:100, columnType:'main', type:'', editable:false},
		{name:'Created', realColumnName:'created', desiredWidth:100, columnType:'main', type:'date', editable:false},
		{name:'Dealership Code', realColumnName:'dealershipCode', desiredWidth:140, columnType:'main', type:'', editable:false},
		{type:'saveChanges', desiredWidth:50, columnType:'main'}
	];

	

	$scope.types = [1, 2, 3, 4];

	statesFactory.getAllStates().then(function(states){
		$scope.states = states;
		$scope.newDealershipColumns = [
			{name:'Name', realColumnName:'name', desiredWidth:200, type:''},
			{name:'City', realColumnName:'city', desiredWidth:200, type:''},
			{name:'State', realColumnName:'state', desiredWidth:200, type:'select', options:$scope.states},
			{name:'Notes', realColumnName:'notes', desiredWidth:400, type:''}
		];
	});

	dealershipsFactory.getAllDealerships().then(function(dealerships){
		for (var i = dealerships.length - 1; i >= 0; i--) {
			dealerships[i].newUserLink = 'http://salesmanbuddy.com/#/newUser/' + dealerships[i].dealershipCode;
		};
		$scope.dealerships = dealerships;
		$scope.loading = false;
	}, function(data){
		console.log("error: ", data);
	});

	$scope.saveUpdatedDealership = function(dealership){
		dealershipsFactory.updateDealership(dealership).then(function(updatedDealership){
			console.log("updated");
		});
	}

	$scope.saveNewDealership = function(newDealership){
		console.log(newDealership)
		newDealership.stateId = newDealership.state.id;
		dealershipsFactory.newDealership(newDealership).then(function(data){
			$scope.dealerships.push(data);
		});
	}

	$scope.getInputStyleForColumn = function(column){
		return {width:column.desiredWidth + 'px'};
	}

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
		return obj;

	}

	$scope.showDetails = function(dealership){
		if(dealership.showDetails){// close the clicked missionary
			dealership.showDetails = false;
			return;
		}
		for (var i = $scope.dealerships.length - 1; i >= 0; i--) {// close all other details
			$scope.dealerships[i].showDetails = false;
		};
		dealership.showDetails = true;
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