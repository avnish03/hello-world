app.controller('BannerLogoCtrl', function($scope, fileUpload, localStorageService,  $cookies, $http, $window) {
	$scope.getUser = function() {
		return localStorageService.get("currentuser");
	};
	
	$scope.saveLogo = function(){
		var user = $scope.getUser();
		var fileUrl = "/AppifyCartAdmin/rest/admin/fileupload.json" + "/" + user + "/" + "both" + "/" + "logo" + "/" + "0" ;
		fileUpload.uploadBannerLogoFile($scope.logoFile, $scope.saveWithType, fileUrl, "logo");
	};

	$scope.saveBanners = function(){
		var user = $scope.getUser();
		var i = 0;
		var j = 0;
		var banner = document.getElementsByName("group-a[".concat(i, "][bannerFile]"));
        var temp = banner ;
		while(banner != null && banner.length > 0){
			var files = banner[0].files;
			if (files != null && files.length > 0){
				var file = files[0];
				if (file != null){
					var fileUrl = "/AppifyCartAdmin/rest/admin/fileupload.json" + "/" + user + "/" + "both" + "/" + "banner" + "/" + 0;
					fileUpload.uploadBannerLogoFile(file, $scope.saveWithType, fileUrl, "banner");
					j++;
				}
			    $scope.statusMessage = "saved successfully." ;
			}
			i++;
			banner = document.getElementsByName("group-a[".concat(i, "][bannerFile]"));

		}


	};

	$scope.saveWithType = function(fileType, bannerFile){
		var postObject = new Object();
		postObject.accountEmail = localStorageService.get("currentuser");
		postObject.file = bannerFile;
		postObject.fileType = fileType;

		var url = "/AppifyCartAdmin/rest/admin/bannerLogo.json";
		var config = {
				headers: { 'Content-Type': 'application/json; charset=UTF-8'
				}
		};
		$http.post(url, postObject, config).then(function(response){

        		$window.location.href = '/admin/BannerLogo.html';
		});
	};

	$scope.loadBannerLogos = function(){
		var user = localStorageService.get("currentuser");
		var url = "/AppifyCartAdmin/rest/admin/appBannerLogos.json/" + user;
		$http.get(url).then(function(response){
			$scope.bannerLogos = response.data.bannerLogos;
			$scope.showImages();
		});
	}
    $scope.bannerImages = [];
	$scope.showImages = function(){
		var user = localStorageService.get("currentuser");
			if ($scope.bannerLogos != null){
				var numImages = $scope.bannerLogos.length;
				for (var i = 0; i < numImages; i++) {
					var bannerLogo = $scope.bannerLogos[i];
					// logic for image sort to get latest updated logo remains
					var imageURL = "/ShopifyConnect/rest/admin/image/" + user + "/" + bannerLogo.file;
					if (bannerLogo.fileType == "logo"){
						$scope.logoFile = imageURL;
					}else {
                        $scope.bannerImages.push(bannerLogo);
                	}
				}
				 $scope.bannerImages.forEach( function (item){
		            var option = Object();
					if (item.fileType == "banner"){
					var imageURL = "/ShopifyConnect/rest/admin/image/" + user + "/" +item.file;
				    item.file = imageURL ;
					}

                 });
			}
	}
	$scope.loadBannerLogos();

	 $scope.deleteImage = function(event){
                var user =  localStorageService.get("currentuser");
                var image =  event.target.id ;
                var fields = image.split('/');
                image = fields[6];

                var url = "/AppifyCartAdmin/rest/admin/deleteImage.json/" + user + "/" + image;
                $http.get(url).then(function(response){
                  //  $scope.loadBannerLogos();
                  $window.location.href = '/admin/BannerLogo.html';
                });
     }
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
	this.uploadBannerLogoFile = function(file, callback, uploadUrl, fileType){
		var fd = new FormData();
		fd.append('file', file);
		var config = {
				withCredentials: true,
				headers: {'Content-Type': undefined},
				transformRequest: angular.identity
		};
		$http.post(uploadUrl, fd, config).success(function(response){
			if (callback != null){
				callback(fileType, response.file);
			}
		});
	}
}]);