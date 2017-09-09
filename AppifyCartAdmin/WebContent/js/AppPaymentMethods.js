app.config(function (localStorageServiceProvider) {
	localStorageServiceProvider
	.setStorageType('sessionStorage');
});

app.controller('AppPaymentMethodsCtrl', function($scope, localStorageService, $cookies, $http, $window) {
	$scope.payment_method = "PayPal";
	$scope.clientIdType = "Sandbox";
	$scope.savePaymentMethod = function(){
		var postObject = new Object();
		postObject.accountEmail = localStorageService.get("currentuser");
		postObject.paymentMethodType = $scope.payment_method;
		postObject.status = $scope.paymentMethodStatus;
		postObject.clientId = $scope.clientId;
		postObject.clientIdType = $scope.clientIdType;

		var url = "/AppifyCartAdmin/rest/admin/addAppPaymentMethod.json";
		var config = {
				headers: { 'Content-Type': 'application/json; charset=UTF-8'
				}
		};
		$http.post(url, postObject, config).then(function(response){	
			$scope.loadPaymentMethods();
		});
	};

	$scope.loadPaymentMethods = function(){
		var user = localStorageService.get("currentuser");
		var url = "/AppifyCartAdmin/rest/admin/appPaymentMethods.json/" + user;
		$http.get(url).then(function(response){
			$scope.paymentMethodTypes = response.data.paymentMethods;
			$scope.paymentMethodChanged();
		});
	};
	
	$scope.cancelPaymentMethod = function(){
		$scope.loadPaymentMethods();
	}
	
	$scope.paymentMethodChanged = function(){
		if ($scope.paymentMethodTypes != null){
			var numPayments = $scope.paymentMethodTypes.length;
			for (var i = 0; i < numPayments; i++) {
				var paymentMethod = $scope.paymentMethodTypes[i];
				if (paymentMethod.paymentMethodType == $scope.payment_method){
					$scope.paymentMethodStatus = paymentMethod.status;
					$scope.clientId = paymentMethod.clientId;
					$scope.clientIdType = paymentMethod.clientIdType;
					break;
				}
			}
		}
	}
	
	$scope.loadPaymentMethods();
});
