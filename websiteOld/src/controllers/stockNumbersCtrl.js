function stockNumbersCtrl($scope, $q, $location, $timeout, $routeParams, stockNumbersFactory, dealershipsFactory, usersFactory, User, rightsUpperSBUser, rightsSalesman, rightsManager, rightsSBUser){

	$scope.dealerships = [];
	$scope.stockNumbers = [];
	$scope.view = 'loading';
	$scope.errorMessage = '';
	$scope.dealershipUsers = [{name:'None', googleUserId:0}];
	$scope.order = {};
	$scope.selects = {};
	$scope.modals = {};
	$scope.newStockNumber = {};
	$scope.rawStockNumbers = [];
	$scope.usersFinishedDefer = $q.defer();
	$scope.stockNumbersFinishedDefer = $q.defer();
	$scope.savedTimeouts = [];
	// $q.all([$scope.stockNumbersFinishedDefer, $scope.usersFinishedDefer]).then(function(){
	// 	$scope.fixStockNumbers();
	// });
	
	User.initUser().then(function(){
		if($scope.doesUserHaveAccessTo([rightsSBUser])){
			console.log("hereasdf")
			$scope.getAllDealerships();
		}
		else if($scope.doesUserHaveAccessTo([rightsManager])){
			$scope.getUsersForDealershipId(User.getUser().sb.dealershipId);
			$scope.getStockNumbers();
		}else{
			$scope.view = 'noRights';
			$scope.errorMessage = "You dont have rights to see all the stock numbers for your dealership";
		}
	});

	$scope.stockNumberStatuses = [
		{name:'On Lot', value:0},
		{name:'Sold', value:1}
	];

	$scope.changedSoldBy = function(index, user, stockNumber){
		console.log("changedSoldBy");
		stockNumber.soldBy = user.id;
		stockNumber.soldOn = new Date();
		stockNumber.soldOnString = stockNumber.soldOn.toISOString().substr(0, 10);
		$scope.saveUpdatedStockNumber(stockNumber, 'Updating Sold By');
	}

	$scope.changedSoldOn = function(stockNumber, when){
		console.log("changedSoldOn");
		console.log(stockNumber, when);
		stockNumber.soldOn = new Date(when);
		$scope.saveUpdatedStockNumber(stockNumber, 'Update Sold On');
	}

	$scope.changedStatus = function(index, statusObject, stockNumber){
		console.log("changedStatus");
		stockNumber.status = stockNumber.statusObject.value;
		$scope.saveUpdatedStockNumber(stockNumber, 'Updating status');
	}

	$scope.disableDatePicker = function(stockNumber){
		return (stockNumber.soldByObject == $scope.dealershipUsers[0])
	}

	$scope.columns = [
		{name:'Stock Number', realColumnName:"stockNumber", desiredWidthInPixels:100, type:''},
		{name:'Status', realColumnName:"statusObject", desiredWidthInPixels:100, type:'select', ngChange:$scope.changedStatus, options:$scope.stockNumberStatuses},
		{name:'Sold By', realColumnName:"soldByObject", desiredWidthInPixels:100, type:'selectSoldBy', ngChange:$scope.changedSoldBy, options:$scope.dealershipUsers},
		{name:'Sold On', realColumnName:"soldOnString", desiredWidthInPixels:100, type:'datePicker', ngChange:$scope.changedSoldOn, ngHide:$scope.disableDatePicker},
		{name:'message', realColumnName:'message', desiredWidthInPixels:100, type:"message"}
	];

	$scope.setOrderBy = function(column){
		console.log("setOrderBy");
		if($scope.order.column == column.realColumnName)
			$scope.order.direction = !$scope.order.direction;
		else{
			$scope.order.column = column.realColumnName;
			$scope.order.direction = true;
		}
	}

	$scope.getAdjustedColumnWidth = function(width, columns, rowWidth){
		// console.log("getAdjustedColumnWidth");
		var count = 0;
		for (var i = columns.length - 1; i >= 0; i--) {
			count += columns[i].desiredWidthInPixels;
		};
		return {'width':"" + (width * rowWidth / count) + "px"};
	}

	$scope.saveNewStockNumber = function(stockNumber){
		console.log("saveNewStockNumber");
		stockNumbersFactory.newStockNumber(stockNumber).then(function(newStockNumber){
			$scope.newStockNumber = {};
			$scope.stockNumbers.push($scope.fixStockNumbers([newStockNumber])[0]);
		});
	}

	$scope.saveMultipleStockNumbers = function(multiple){
		console.log("saveMultipleStockNumbers");
		newStockNumbers = multiple.split("\n");
		// TODO check to see if they already exist in our current list on the client
		// TODO show final list for confirmation
		for (var i = newStockNumbers.length - 1; i >= 0; i--) {
			$scope.saveNewStockNumber(newStockNumbers[i]);
		};
	}

	$scope.saveUpdatedStockNumber = function(stockNumber, message){
		console.log("saveUpdatedStockNumber");

		$scope.cancelSavedTimeouts();
		stockNumber.message = message || 'Updating stock number';
		stockNumber.status = stockNumber.statusObject.value;

		stockNumbersFactory.updateStockNumber(stockNumber).then(function(updatedStockNumber){
			stockNumber.message = 'Saved';
			if($scope.savedTimeouts.length == 0)
				$scope.savedTimeouts.push($timeout(function(){
					stockNumber.message = 'NONE';
					$scope.savedTimeouts = [];
				}, 2000));
		});
	}

	$scope.cancelSavedTimeouts = function(){
		for (var i = $scope.savedTimeouts.length - 1; i >= 0; i--) {
			$timeout.cancel($scope.savedTimeouts[i]);
		};
	}

	$scope.getUsersForDealershipId = function(dealershipId){
		console.log("getUsersForDealershipId");
		usersFactory.getUsersForDealershipId(dealershipId).then(function(users){
			for (var i = users.length - 1; i >= 0; i--) {
				$scope.getNameForUser(users[i]);
			};
			
			var tempList = [$scope.dealershipUsers[0]];
			$scope.dealershipUsers = tempList.concat(users);
			$scope.usersFinishedDefer.resolve();
		});
	}

	$scope.getNameForUser = function(user){
		console.log("getNameForUser");
		usersFactory.getNameForUser(user.googleUserId).then(function(name){
			user.name = name.name;
		});
	}

	$scope.getAllDealerships = function(specificId){
		console.log("getAllDealerships");
		$scope.view = 'loading';
		dealershipsFactory.getAllDealerships().then(function(dealerships){
			$scope.dealerships = dealerships;
			$scope.view = 'main';
			if(specificId && specificId != 'None'){
				$scope.selectedDealership(specificId);
			}
		});
	}

	$scope.getStockNumbers = function(){
		console.log("getStockNumbers");
		stockNumbersFactory.getStockNumbers().then(function(stockNumbers){
			$scope.rawStockNumbers = stockNumbers;
			$scope.stockNumbersFinishedDefer.resolve();
		});
	}

	$scope.getStockNumbersForDealershipId = function(dealershipId){
		console.log("getStockNumbersForDealershipId");
		stockNumbersFactory.getStockNumbersForDealershipId(dealershipId).then(function(stockNumbers){
			$scope.rawStockNumbers = stockNumbers;
			console.log(angular.toJson(stockNumbers))
			$scope.stockNumbersFinishedDefer.resolve();
		});
	}

	$scope.fixStockNumbers = function(){
		console.log("fixStockNumbers");
		var stockNumbers = $scope.rawStockNumbers;
		for (var i = stockNumbers.length - 1; i >= 0; i--) {
			stockNumbers[i].message = 'NONE';
			stockNumbers[i].soldByObject = $scope.dealershipUsers[0];
			for (var j = $scope.stockNumberStatuses.length - 1; j >= 0; j--) {
				if(stockNumbers[i].status == $scope.stockNumberStatuses[j].value){
					stockNumbers[i].statusObject = $scope.stockNumberStatuses[j];
					break;
				}
			};
			for (var j = $scope.dealershipUsers.length - 1; j >= 0; j--) {
				if($scope.dealershipUsers[j].id == stockNumbers[i].soldBy){
					stockNumbers[i].soldByObject = $scope.dealershipUsers[j];
					break;
				}
			};
		};
		$scope.stockNumbers = stockNumbers;
		$scope.view = 'main';
	}

	$scope.selectedDealership = function(dealershipId){
		console.log("selectedDealership");
		// console.log($location.path().split('/'))
		// $location.path('/' + $location.path().split('/')[1] + '/' + $location.path().split('/')[2] + '/' + dealershipId);
		// console.log(dealershipId)
		$scope.stockNumbersFinishedDefer = $q.defer();
		$scope.usersFinishedDefer = $q.defer();
		$q.all({a:$scope.stockNumbersFinishedDefer.promise, b:$scope.usersFinishedDefer.promise}).then(function(){
			$scope.fixStockNumbers();
		});
		$scope.getStockNumbersForDealershipId(dealershipId);
		$scope.getUsersForDealershipId(dealershipId);
	}
}












































