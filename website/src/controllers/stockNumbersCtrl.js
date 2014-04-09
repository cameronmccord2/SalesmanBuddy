function stockNumbersCtrl($scope, stockNumbersFactory, dealershipsFactory, usersFactory, User, rightsUpperSBUser, rightsSalesman, rightsManager, rightsSBUser){

	$scope.dealerships = [];
	$scope.stockNumbers = [];
	$scope.view = 'loading';
	$scope.errorMessage = '';
	$scope.dealershipUsers = [{name:'None', googleUserId:0}];
	$scope.order = {};
	$scope.selects = {};
	$scope.modals = {};
	$scope.newStockNumber = {};
	
	User.initUser().then(function(){
		if($scope.doesUserHaveAccessTo([rightsSBUser]))
			$scope.getAllDealerships();
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
		console.log(index, user, stockNumber)
		stockNumber.soldBy = user.id;
		stockNumber.soldOn = new Date().toISOString().substr(0, 10);;// TODO fix this, it doesn't populate correctly in the date picker
		// TODO show save button
	}

	$scope.changedSoldOn = function(stockNumber, when){
		console.log(stockNumber, when);
	}

	$scope.changedStatus = function(index, statusObject, stockNumber){
		stockNumber.loading = true;
		stockNumber.status = stockNumber.statusObject.value;
		stockNumbersFactory.updateStockNumberStatus(stockNumber.id, stockNumber.status).then(function(updatedStockNumber){
			stockNumber.loading = false;
		});
	}

	$scope.columns = [
		{name:'Stock Number', realColumnName:"stockNumber", desiredWidthInPixels:100, type:''},
		{name:'Status', realColumnName:"statusObject", desiredWidthInPixels:100, type:'select', ngChange:$scope.changedStatus, options:$scope.stockNumberStatuses},
		{name:'Sold By', realColumnName:"soldByObject", desiredWidthInPixels:100, type:'selectSoldBy', ngChange:$scope.changedSoldBy, options:$scope.dealershipUsers},
		{name:'Sold On', realColumnName:"soldOn", desiredWidthInPixels:100, type:'datePicker', ngChange:$scope.changedSoldOn},
		{name:'', realColumnName:'loading', desiredWidthInPixels:20, type:"loading"}
	];



	$scope.setOrderBy = function(column){
		if($scope.order.column == column.realColumnName)
			$scope.order.direction = !$scope.order.direction;
		else{
			$scope.order.column = column.realColumnName;
			$scope.order.direction = true;
		}
	}

	$scope.getAdjustedColumnWidth = function(width, columns, rowWidth){
		var count = 0;
		for (var i = columns.length - 1; i >= 0; i--) {
			count += columns[i].desiredWidthInPixels;
		};
		return {'width':"" + (width * rowWidth / count) + "px"};
	}

	$scope.saveNewStockNumber = function(stockNumber){
		stockNumbersFactory.newStockNumber(stockNumber).then(function(newStockNumber){
			$scope.newStockNumber = {};
			$scope.stockNumbers.push($scope.fixStockNumbers([newStockNumber])[0]);
		});
	}

	$scope.saveMultipleStockNumbers = function(multiple){
		newStockNumbers = multiple.split("\n");
		// TODO check to see if they already exist in our current list on the client
		// TODO show final list for confirmation
		for (var i = newStockNumbers.length - 1; i >= 0; i--) {
			$scope.saveNewStockNumber(newStockNumbers[i]);
		};
	}

	

	$scope.saveUpdatedStockNumber = function(stockNumber){
		stockNumber.loading = true;
		stockNumber.status = stockNumber.statusObject.value;
		stockNumbersFactory.updateStockNumber(stockNumber).then(function(updatedStockNumber){
			stockNumber.loading = false;
		});
	}

	$scope.getUsersForDealershipId = function(dealershipId){
		usersFactory.getUsersForDealershipId(dealershipId).then(function(users){
			for (var i = users.length - 1; i >= 0; i--) {
				$scope.getNameForUser(users[i]);
			};
			
			var tempList = [$scope.dealershipUsers[0]];
			$scope.dealershipUsers = tempList.concat(users);
			$scope.stockNumbers = $scope.fixStockNumbers($scope.stockNumbers);
			console.log($scope.dealershipUsers)
		});
	}

	$scope.getNameForUser = function(user){
		usersFactory.getNameForUser(user.googleUserId).then(function(name){
			user.name = name.name;
		});
	}

	$scope.getAllDealerships = function(){
		$scope.view = 'loading';
		dealershipsFactory.getAllDealerships().then(function(dealerships){
			$scope.dealerships = dealerships;
			$scope.view = 'main';
		});
	}

	$scope.getStockNumbers = function(){
		stockNumbersFactory.getStockNumbers().then(function(stockNumbers){
			$scope.stockNumbers = $scope.fixStockNumbers(stockNumbers);
			$scope.view = 'main';
		});
	}

	$scope.getStockNumbersForDealershipId = function(dealershipId){
		stockNumbersFactory.getStockNumbersForDealershipId(dealershipId).then(function(stockNumbers){
			$scope.stockNumbers = $scope.fixStockNumbers(stockNumbers);
			$scope.view = 'main';
		});
	}

	$scope.fixStockNumbers = function(stockNumbers){
		for (var i = stockNumbers.length - 1; i >= 0; i--) {
			stockNumbers[i].loading = false;
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
		return stockNumbers;
	}

	$scope.selectedDealership = function(dealershipId){
		$scope.getStockNumbersForDealershipId(dealershipId);
		$scope.getUsersForDealershipId(dealershipId);
	}
}












































