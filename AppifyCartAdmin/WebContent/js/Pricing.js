app.config(function (localStorageServiceProvider) {
	localStorageServiceProvider
	.setStorageType('sessionStorage');
});

app.controller('PricingCtrl', function($scope, localStorageService,  $cookies, $http, $window) {

/**
    $scope.basicURLPlan = "https://appifycart.recurly.com/subscribe/appifycart-basic-588-1y-s/?email="+$scope.getUser() ;
    $scope.smallBusinessPlanURL = "https://appifycart.recurly.com/subscribe/appifycart-smallbusiness-1188-1y-s/??email="+$scope.getUser() ;
    $scope.professionalPlanURL = "https://appifycart.recurly.com/subscribe/appifycart-professional-3588-1y-s/?email="+$scope.getUser();
*/
	$scope.getUser = function() {
		return localStorageService.get("currentuser");
	}
	$scope.getStore = function() {
		var str = localStorageService.get("shopifyshop");
		var newchar = '%2F'
//      var length =  res.length ;
//    	 if(!(res.substring(length-3,length) === "%2F")){
//            		res = res +"%2F";
//            	}
//         console.log(res);
        res = str.split('/').join(newchar);
        if(!(res[res.length -1]==='%2F')){
    		res = res +"%2F";
    	}
        return res;
	}
	$scope.getSignupMethod = function() {
		return localStorageService.get("signupmethod");
	}

	if($scope.getStore != null || $scope.getSignupMethod() === "shopifyapp"){
        $scope.basicURLPlan = "/AppifyCartAdmin/rest/shopifyapp/recurringCharge/?plan=BasicSubscription&user="+$scope.getUser()+"&myshopify_url="+$scope.getStore();
        $scope.smallBusinessPlanURL = "/AppifyCartAdmin/rest/shopifyapp/recurringCharge/?plan=SmallBusiness&user="+$scope.getUser()+"&myshopify_url="+$scope.getStore();
        $scope.professionalPlanURL = "/AppifyCartAdmin/rest/shopifyapp/recurringCharge/?plan=ProfessionalSubscription&user="+$scope.getUser()+"&myshopify_url="+$scope.getStore();
    } else {
        $scope.basicURLPlan = "https://appifycart.recurly.com/subscribe/appifycart-basic-588-1y-s/?email="+$scope.getUser() ;
        $scope.smallBusinessPlanURL = "https://appifycart.recurly.com/subscribe/appifycart-smallbusiness-1188-1y-s/??email="+$scope.getUser() ;
        $scope.professionalPlanURL = "https://appifycart.recurly.com/subscribe/appifycart-professional-3588-1y-s/?email="+$scope.getUser();
    }
	$scope.getPayment = function() {
		var payments = localStorageService.get("payments");
		$scope.payment = null;

		if (payments != null){
			var numPayments = payments.length;
			if (numPayments > 0){
				$scope.payment = payments[0].productID;
			}	
		}
		
		$scope.paymentTitle = null
		
		if ($scope.payment == "1001"){
			$scope.paymentTitle = "AppifyCart Basic";
		}else if ($scope.payment == "1002"){
			$scope.paymentTitle = "AppifyCart Small Business";
		}else if ($scope.payment == "1003"){
			$scope.paymentTitle = "AppifyCart Enterprise";
		}
		
		return ($scope.payment == null);
	}
	$scope.hit101 = function() {
        $window.location.href = $scope.basicURLPlan;
	}
	$scope.hit102 = function() {
        $window.location.href = $scope.smallBusinessPlanURL;
	}
	$scope.hit103 = function() {
        $window.location.href = $scope.professionalPlanURL;
	}
});
