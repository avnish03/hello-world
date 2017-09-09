    app.config(function (localStorageServiceProvider) {
        localStorageServiceProvider
        .setStorageType('sessionStorage');
    });

app.controller('appManagementCtrl', function($scope, localStorageService, $http, $window) {
        $scope.ostype = "ios";

        var apps = localStorageService.get("apps");
		if (apps != null){
			var numApps = apps.length;
			for (var i = 0; i < numApps; i++) {
				var app = apps[i];
				if(app.ostype ==  $scope.ostype ){
				$scope.platform = app.platform;
				$scope.store_website = app.website;
				$scope.store_api_key = app.apikey;
				$scope.store_api_password = app.apipassword;
				break;
				}
			}
		}

        $scope.loadAllBuilds = function(){
            if($scope.ostype == null){
                $scope.ostype = localStorageService.get("currentos");
            }
            var build_status = localStorageService.get("build_status");
            if( build_status == "in_process"){
                $('.build-in-process-msg', $('.buildingAppMessage')).show();
                localStorageService.set("build_status",null);
            }
            $scope.categoryOptions = [];
            var user =  localStorageService.get("currentuser");
       		var url = "/AppifyCartAdmin/rest/admin/allBuildData.json/" + user +"/"+ $scope.ostype;
       		$http.get(url).then(function(response){

                $scope.categoryMaps = response.data.result.appBuilds;

                $scope.categoryMaps.forEach( function (item){
                    var option = Object();
                    option.id = item.id;
                    option.appname =  item.appname;
                    option.downloadLink =  item.downloadLink;
                    //option.creationTime =  item.creationTime;
                    item.creationTime =  "NA";
                    $scope.categoryOptions.push(option);
                });
            });
        };

        $scope.osChanged = function(osType){
             $scope.ostype = osType;
             $scope.loadAllBuilds();
        }



        $scope.loadAllBuilds();

        $scope.deleteCurrentBuild = function(event){
            var user =  localStorageService.get("currentuser");
            var docId =  $(event.currentTarget).attr("data-id") ;

            var url = "/AppifyCartAdmin/rest/admin/deleteOneBuild.json/" + user +"/"+ $scope.ostype + "/" + docId;
            $http.get(url).then(function(response){

                $scope.loadAllBuilds();
            });
        }

        $scope.publish = function(){
            localStorageService.set("currentos", $scope.ostype);
            localStorageService.set("buildType", "appStore");
            $window.location.href = '/admin/AppCreator.html';
        }



});
