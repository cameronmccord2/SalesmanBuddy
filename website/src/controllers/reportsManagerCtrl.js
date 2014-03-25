function reportsManagerCtrl($scope, User, dealershipsFactory, userTreeFactory, usersFactory){

	$scope.dealerships = [];
	$scope.userTrees = [];
	$scope.selects = {};

	$scope.types = {
		onTestDriveEmail:1,
		dailyTestDriveSummaryEmail:2,
		dailySalesmanTestDriveEmail:3,
		dailyDealershipSummaryEmail:4,
		weeklyTestDriveSummaryEmail:5,
		weeklySalesmanSummaryEmail:6,
		weeklyDealershipSummaryEmail:7,
		biMonthlyTestDriveSummaryEmail:8,
		biMonthlySalesmanSummaryEmail:9,
		biMonthlyDealershipSummaryEmail:10,
		monthlyTestDriveSummaryEmail:11,
		monthlySalesmanSummaryEmail:12,
		monthlyDealershipSummaryEmail:13
	};

	$scope.reportAboutToOptions = [
		{name:"On test drive", value:1},
		{name:"Daily Salesman Summary", value:14},// these need to be added to the server
		{name:"Weekly Salesman Summary", value:15},
		{name:"Monthly Salesman Summary", value:16}
	];

	$scope.summaryOptions = [
		{name:"Daily Test Drive", value:2},
		{name:"Daily All Salesman", value:3},
		{name:"Daily Dealership", value:4},
		{name:"Weekly Test Drive", value:5},
		{name:"Weekly All Salesman", value:6},
		{name:"Weekly Dealership", value:7},
		{name:"Monthly Test Drive", value:11},
		{name:"Monthly All Salesman", value:12},
		{name:"Monthly Dealership", value:13}
	];

	$scope.blankFunction = function(){

	}

	$scope.checkForDuplicates = function(index){
		for (var i = $scope.userTrees.length - 1; i >= 0; i--) {
			if(i == index)
				continue;
			if($scope.userTrees[i].user == $scope.userTrees[index].user && $scope.userTrees[i].supervisor == $scope.userTrees[index].supervisor && $scope.userTrees[i].typeObject == $scope.userTrees[index].typeObject)
				return true;
		};
		return false;
	}

	$scope.isSummaryUserTree = function(userTree){
		return ($scope.summaryOptions.indexOf(userTree.typeObject) != -1);
	}

	$scope.isAboutToUserTree = function(userTree){
		return ($scope.reportAboutToOptions.indexOf(userTree.typeObject) != -1);
	}

	$scope.summaryColumns = [
		{name:"Report To", desiredWidth:100, type:"select", realColumnName:"user", options:$scope.dealershipUsers, ngChange:$scope.checkForDuplicates},
		{name:"Report About", desiredWidth:100, type:"", realColumnName:"typeObject", options:$scope.summaryOptions, ngChange:$scope.checkForDuplicates}
	];

	$scope.aboutToColumns = [
		{name:"Salesman", desiredWidth:100, type:"select", realColumnName:"user", options:$scope.dealershipUsers, ngChange:$scope.checkForDuplicates},
		{name:"Supervisor", desiredWidth:100, type:"select", realColumnName:"supervisor", options:$scope.dealershipUsers, ngChange:$scope.checkForDuplicates},
		{name:"Report Type", desiredWidth:100, type:"select", realColumnName:"typeObject", options:$scope.reportAboutToOptions, ngChange:$scope.checkForDuplicates}
	];
	
	dealershipsFactory.getAllDealerships().then(function(dealerships){
		$scope.dealerships = dealerships;
		console.log(dealerships)
	});

	$scope.setupUserTrees = function(){
		// match dealership users up with the userTree
		for (var i = $scope.userTrees.length - 1; i >= 0; i--) {
			for (var j = $scope.dealershipUsers.length - 1; j >= 0; j--) {
				if($scope.userTrees[i].userId == $scope.dealershipUsers[j].googleUserId){
					$scope.userTrees[i].user = $scope.dealershipUsers[j];
					break;
				}
			};
			// get type from summaryOptions
			for (var j = $scope.summaryOptions.length - 1; j >= 0; j--) {
				if($scope.userTrees[i].type == $scope.summaryOptions[j].value){
					$scope.userTrees[i].typeObject = $scope.summaryOptions[j];
					break;
				}
			};
			// get type from reportAboutToOptions
			for (var j = $scope.reportAboutToOptions.length - 1; j >= 0; j--) {
				if($scope.userTrees[i].type == $scope.summaryOptions[j].value){
					$scope.userTrees[i].typeObject = $scope.reportAboutToOptions[j];
					break;
				}
			};
		};
	}

	User.initUser().then(function(){
		if(!$scope.doesUserHaveAccessTo('reportsManagerAnyDealership') && $scope.doesUserHaveAccessTo('reportsManager')){
			userTreeFactory.getUserTreesForDealershipId(User.getUser().sb.deaershipId).then(function(userTrees){
				$scope.userTrees = userTrees;
				$scope.dealershipUsers = usersFactory.getUsersForDealershipId(User.getUser().sb.dealershipId);
				$scope.setupUserTrees();
			});
		}
	});

	$scope.selectedDealership = function(){
		console.log($scope.selects.chosenDealership)

		userTreeFactory.getUserTreesForDealershipId($scope.selects.chosenDealership.id).then(function(userTrees){
			$scope.userTrees = userTrees;
			$scope.dealershipUsers = usersFactory.getUsersForDealershipId($scope.selects.chosenDealership.id);
			$scope.setupUserTrees();
		});
	}

	$scope.saveUserTree = function(userTree){
		userTree.type = userTree.typeObject.value;
		userTree.userId = userTree.user.googleUserId;
		userTree.supervisorId = userTree.supervisor.googleUserId;

		if(userTree.id)
			userTreeFactory.updateUserTree(userTree).then(function(data){
				console.log("check how to check success from this:", data);
			});
		else
			userTreeFactory.newUserTree(userTree).then(function(data){
				userTree.created = data.created;
				userTree.id = data.id;
			});
	}
}



































