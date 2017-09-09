app.controller('AppPushNotificationConfigCtrl', function($scope, localStorageService, fileUpload, $cookies, $http, $window) {
	$scope.certificateType = "development";
	$scope.ostype = "ios";
	$scope.saveIOSCertificate = function() {
		var user =  localStorageService.get("currentuser");
		var ostype = "ios";

		var fileUrl = "/AppifyCartAdmin/rest/admin/fileupload.json" + "/" + user + "/" + ostype + "/" + "pushNotification" + "/" + 0;
		$scope.uploadIOSPushCertifcate(fileUrl);
	};

	$scope.uploadIOSPushCertifcate = function(fileUrl){
	    if(typeof($scope.iosCertificate) == "undefined" || $scope.iosCertificate == null){
	        $scope.statusMessage = "Please choose valid Certificate.";
            $window.alert("Please choose iOS Certificate !");
            return;
    	}
		fileUpload.uploadFileToUrl($scope.iosCertificate, $scope.doSaveIOSCertificte, fileUrl);
	};

	$scope.doSaveIOSCertificte = function(fileUrl, certificateFile){
		var url = "/AppifyCartAdmin/rest/admin/pushNotificationConfig.json";

		var postObject = new Object();
		postObject.accountEmail = localStorageService.get("currentuser");
		postObject.osType = "ios";
		postObject.certificateType = $scope.certificateType;
		postObject.certificate = certificateFile;
		postObject.passphrase = $scope.passphrase;

		var config = {
				headers: { 'Content-Type': 'application/json; charset=UTF-8'
				}
		};
		$http.post(url, postObject, config).then(function(response){
		        $scope.statusMessage = "Saved Successfully.";
		});
	};
	
	$scope.pushMessage = function(){
		var url = "/AppifyCartAdmin/rest/admin/pushNotification.json";

		var postObject = new Object();
		postObject.accountEmail = localStorageService.get("currentuser");
		postObject.osType = $scope.ostype;
		postObject.pushTitle = $scope.pushTitle;
		postObject.pushText = $scope.pushText;
		
		var config = {
				headers: { 'Content-Type': 'application/json; charset=UTF-8'
				}
		};
		$http.post(url, postObject, config).then(function(response){
		    //Empty
		});
	};
});

app.directive('fileModel', ['$parse', function ($parse) {
	return {
		restrict: 'A',
		link: function(scope, element, attrs) {
			var model = $parse(attrs.fileModel);
			var modelSetter = model.assign;

			element.bind('change', function(){
				scope.$apply(function(){
					modelSetter(scope, element[0].files[0]);
				});
			});
		}
	};
}]);
app.service('fileUpload', ['$http', function ($http) {
	this.uploadFileToUrl = function(file, callback, uploadUrl){
		var fd = new FormData();
		fd.append('file', file);
		var config = {
				withCredentials: true,
				headers: {'Content-Type': undefined},
				transformRequest: angular.identity
		};
		$http.post(uploadUrl, fd, config).success(function(response){
			if (callback != null){
				callback(uploadUrl, response.file);
			}
		});
	}
}]);
