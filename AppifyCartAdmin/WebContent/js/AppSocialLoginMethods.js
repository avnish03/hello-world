app.config(function (localStorageServiceProvider) {
	localStorageServiceProvider
	.setStorageType('sessionStorage');
});

app.controller('AppSocialLoginMethodsCtrl', function($scope, localStorageService, $cookies, $http, $window) {
	$scope.saveSocialLoginMethod = function(){
		var postObject = new Object();
		postObject.accountEmail =  localStorageService.get("currentuser");
		postObject.osType = $scope.ostype;
		postObject.socialLoginMethodType = $scope.social_login_method;
		postObject.developerId = $scope.developerId;
		postObject.status = $scope.socialLoginMethodStatus;

		var url = "/AppifyCartAdmin/rest/admin/addAppSocialLoginMethod.json";
		var config = {
				headers: { 'Content-Type': 'application/json; charset=UTF-8'
				}
		};
		$http.post(url, postObject, config).then(function(response){
		});
		
		$scope.loadSocialLoginMethods();
	};

	$scope.loadSocialLoginMethods = function(){
		var user =  localStorageService.get("currentuser");
		var url = "/AppifyCartAdmin/rest/admin/appSocialLoginMethods.json/" + user;
		$http.get(url).then(function(response){
			$scope.socialLoginMethodTypes = response.data.socialLoginMethods;
		});
		
		if($scope.ostype == null){
		    localStorageService.set("currentos", "ios");
			$scope.ostype = localStorageService.get("currentos");
		}
		
		$scope.osChanged($scope.ostype);
	};
	
	$scope.socialLoginMethodChanged = function(){
		$scope.socialLoginMethodStatus = null;
		$scope.developerId = null;
		if ($scope.socialLoginMethodTypes != null){
			var numSocialLogins = $scope.socialLoginMethodTypes.length;
			for (var i = 0; i < numSocialLogins; i++) {
				var socialLoginMethod = $scope.socialLoginMethodTypes[i];
				if (socialLoginMethod.socialLoginMethodType == $scope.social_login_method && $scope.ostype == socialLoginMethod.osType){
					$scope.socialLoginMethodStatus = socialLoginMethod.status;
					$scope.developerId = socialLoginMethod.developerId;
					
					if($scope.socialLoginMethodStatus == "Enabled"){
						break;
					};
				}
			}
		}
	}
	
	$scope.osChanged = function(osType){
	localStorageService.set("currentos", osType);
		$scope.socialLoginMethod = null;
		$scope.developerId = null;
		$scope.socialLoginMethodStatus = null;
		
		if ($scope.socialLoginMethodTypes != null){
			var numSocialLogins = $scope.socialLoginMethodTypes.length;
			for (var i = 0; i < numSocialLogins; i++) {
				var socialLoginMethod = $scope.socialLoginMethodTypes[i];
				if (osType == socialLoginMethod.osType){
					$scope.social_login_method = socialLoginMethod.socialLoginMethodType;
					$scope.developerId = socialLoginMethod.developerId;
					$scope.socialLoginMethodStatus = socialLoginMethod.status;
					
					if($scope.socialLoginMethodStatus == "Enabled"){
						break;
					};
				}
			}
		}
		
		$scope.socialLoginMethodChanged();
	}
	
	$scope.loadSocialLoginMethods();
});