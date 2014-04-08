function licensesListCtrl($scope, licensesFactory, User, dealershipsFactory, usersFactory){

	$scope.view = "loading";
	$scope.displayList = [];
	$scope.dealershipUsers = [];
	$scope.allUsers = [];
	$scope.dealerships = [];
	$scope.selected = {};
	$scope.hideCheckedLicenses = true;
	$scope.search = "";
	$scope.showBottom = true;
	$scope.loadingMessage = "Loading Your Test Drives";

	$scope.order = {
		column:"",
		direction:true
	};
	$scope.licenseStatuses = [
		{name:"Active", value:1},
		{name:"Hidden", value:2}
	];

	$scope.changedStatus = function(index){
		var license = $scope.displayList[index];
		license.status = license.statusObject.value;
	}

	$scope.setOrderBy = function(column){
		console.log(column);
		if($scope.order.column == column.realColumnName)
			$scope.order.direction = !$scope.order.direction;
		else{
			$scope.order.column = column.realColumnName;
			$scope.order.direction = true;
		}
	}

	$scope.columns = [
		{name:'First Name', realColumnName:"firstName", desiredWidthInPixels:100, type:''},
		{name:'Last Name', realColumnName:"lastName", desiredWidthInPixels:100, type:''},
		{name:'Phone Number', realColumnName:"phoneNumber", desiredWidthInPixels:100, type:''},
		{name:'Stock Number', realColumnName:"stockNumber", desiredWidthInPixels:300, type:''},
		{name:'Created', realColumnName:"created", desiredWidthInPixels:100, type:'date'},
		{name:'Status', realColumnName:"statusObject", desiredWidthInPixels:100, type:'select', ngChange:$scope.changedStatus, options:$scope.licenseStatuses}
	];
	
	User.initUser().then(function(){
		if($scope.isPath("/testDrives") || User.getUser().sb.type == 1)// get them for this user
			$scope.selectedUser(User.getUser().sb.googleUserId);
		else if(User.getUser().sb.type == 2)// choose whole dealership or specific user
			$scope.selectedDealership(User.getUser().sb.dealershipId);
		else if(User.getUser().sb.type > 2){// choose all licenses, specific dealership, or specific user
			$scope.getAllDealerships();
			$scope.getAllUsers();
		}else
			console.log("invalid user type found: ", User.getUser());
	});

	$scope.hideThisLicense = function(license){
		return ($scope.hideCheckedLicenses && license.status != 1);
	}

	$scope.processLicenses = function(licenses){
		$scope.masterList = licenses;
		var list = [];
		for (var i = licenses.length - 1; i >= 0; i--) {
			var l = {};
			l.firstName = licensesFactory.getFirstNameFromLicense(licenses[i]);
			l.lastName = licensesFactory.getLastNameFromLicense(licenses[i]);
			l.phoneNumber = licensesFactory.getPhoneNumberFromLicense(licenses[i]);
			l.stockNumber = licensesFactory.getStockNumberFromLicense(licenses[i]);
			l.status = licenses[i].status || 1;
			l.statusObject = $scope.licenseStatuses[l.status - 1];
			l.created = licenses[i].created;
			list.push(l);
		};
		return list;
	}

	$scope.getAllLicensesForDealership = function(dealershipId){
		licensesFactory.getAllLicensesForDealership(dealershipId).then(function(licenses){
			$scope.displayList = $scope.processLicenses(licenses);
			$scope.view = "main";
		});
	}

	$scope.selectedDealership = function(dealership){
		usersFactory.getUsersForDealershipId(dealership.id).then(function(users){
			$scope.dealershipUsers = [];
			$scope.dealershipUsers.push({name:"All Users", googleUserId:"0"});
			for (var i = 0; i < users.length; i++) {
				$scope.dealershipUsers.push(users[i]);
			};

			for (var i = $scope.dealershipUsers.length - 1; i > 0; i--) {
				$scope.getNameForUser($scope.dealershipUsers[i]);
			};
			$scope.view = "main";
		});
	}

	$scope.selectedUser = function(user){
		$scope.showBottom = false;
		if(user.googleUserId == "-1"){
			licensesFactory.getAllLicenses().then(function(licenses){
				$scope.displayList = $scope.processLicenses(licenses);
				$scope.view = 'main';
				$scope.showBottom = true;
			});
		}else if(user.googleUserId == "0"){
			var id = null;
			if($scope.selected.dealership && $scope.selected.dealership.id)
				id = $scope.selected.dealership.id;

			licensesFactory.getAllLicensesForDealership(id).then(function(licenses){
				$scope.displayList = $scope.processLicenses(licenses);
				$scope.view = "main";
				$scope.showBottom = true;
			});
		}else{
			licensesFactory.getAllLicensesForUser(user.googleUserId).then(function(licenses){
				$scope.displayList = $scope.processLicenses(licenses);
				$scope.view = "main";
				$scope.showBottom = true;
			});
		}
	}

	$scope.getAllUsers = function(){
		usersFactory.getAllUsers().then(function(users){
			$scope.allUsers = [];
			$scope.allUsers.push({name:"All Users", googleUserId:"-1"});

			for (var i = 0; i < users.length; i++) {
				$scope.allUsers.push(users[i]);
			};

			for (var i = $scope.allUsers.length - 1; i > 0; i--) {
				$scope.getNameForUser($scope.allUsers[i]);
			};
			$scope.view = "main";
		});
	}

	$scope.getAllDealerships = function(){
		dealershipsFactory.getAllDealerships().then(function(dealerships){
			console.log(dealerships)
			$scope.dealerships = dealerships;
			$scope.view = "main";
		});
	}

	$scope.getNameForUser = function(user){
		usersFactory.getNameForUser(user.googleUserId).then(function(userData){
			console.log(userData);
			user.name = userData.name;
		});
	}

	$scope.getAdjustedColumnWidth = function(width, columns, rowWidth){
		var count = 0;
		for (var i = columns.length - 1; i >= 0; i--) {
			count += columns[i].desiredWidthInPixels;
		};
		return {'width':"" + (width * rowWidth / count) + "px"};
	}
}