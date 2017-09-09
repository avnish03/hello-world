app.controller('CMSCtrl', function($scope, localStorageService, $http, $window) {
   var totalNumberOfCMS = 0 ;

$scope.fetchCMSContent = function() {
          var user = localStorageService.get("currentuser");
          var loadCount = 0;
          var url = "/AppifyCartAdmin/rest/admin/fetchCMSPages.json/"+user;
          $http.get(url).then(function(response){

          console.log( "CMS FETCHING DATA" );
          console.log( JSON.stringify(response.data, null, "    ") );
          var num = response.data.data.length;

          totalNumberOfCMS = num ;

          $scope.CMSOptions = [];
            response.data.data.forEach( function (item){
                 console.log("entred in loop");
                           console.log("enterd in second loop");
                           var option = Object();
                           option.title = item.title ;
                           console.log("title  : " + item.title);
                           option.pageId = item.id;
                    //     option.pageId = item.pageId;
                           option.content = item.content;
                           option.isEnable = item.isEnable ;
                           $scope.CMSOptions.push(option);
                });
                   if (totalNumberOfCMS <= 0 ){
                       var cms_status = localStorageService.get("cms_status");
                           if(cms_status == 0 || typeof cms_status === "undefined"){
                                localStorageService.set("cms_status","1");

                           ``}else{
                                localStorageService.set("cms_status","0");
                                window.location.href = "CreateNewCMS.html";

                           }
                       }

       });
//       if (totalNumberOfCMS <= 0 ){
//       var cms_status = localStorageService.get("cms_status");
//           if(cms_status == 0 || typeof cms_status === "undefined"){
//                localStorageService.set("cms_status","1");
//                console.log('aabra ka addbra ');
//           ``}else{
//                localStorageService.set("cms_status","0");
//                window.location.href = "CreateNewCMS.html";
//                console.log('chutia banaya bada mazaa aaya ');
//           }
//       }
    };

 $scope.insertCMSContent = function() {
            var user = localStorageService.get("currentuser");
             var url = "/AppifyCartAdmin/rest/admin/updateCMSPages.json/";
                  var postObject = new Object();
                    postObject.email = user;
                    postObject.pageId =  "5912c44be2af4902d5bd83d8"
                    postObject.title = document.getElementById("newTitle").value ;
                    postObject.isEnable = true ;
                    postObject.content = document.getElementById("newTextArea").value;
                    console.log( JSON.stringify(postObject, null, "    ") );
                  var config = {
                        headers: { 'Content-Type': 'application/json; charset=UTF-8'
                        }
                  };
                  $http.post(url, postObject, config).then(function(response){
                    console.log( JSON.stringify(response.data, null, "    ") );
                     if (response.data.status == 1){
                     alert('Successfully saved');
                      window.location.href = "CMSPages.html";
                          $scope.statusMessage = "";
                     }else{
                          $scope.statusMessage = "Failed";
                     }
                  });

                 };

 $scope.updateCMSContent = function() {
         for (var i = 0 ; i <= totalNumberOfCMS ; i++ ){
             var user = localStorageService.get("currentuser");
             var url = "/AppifyCartAdmin/rest/admin/updateCMSPages.json/";
                  var postObject = new Object();
                    postObject.email = user;
                    postObject.pageId =  document.getElementById("pageId_"+i).value ;
                    postObject.title =   document.getElementById("title_"+i).value ;
                   // console.log("Page title :   " + (document.getElementById("title_"+i).getAttribute("data-value"))) ;
                    console.log("Is Enable satus : "+ (document.getElementById("isEnable_"+i).checked))
                    postObject.isEnable = document.getElementById("isEnable_"+i).checked ;
                    postObject.content = document.getElementById("textArea_"+i).value;
                    console.log( JSON.stringify(postObject, null, "    ") );
                     var config = {
                        headers: { 'Content-Type': 'application/json; charset=UTF-8'
                        }
                     };
                    $http.post(url, postObject, config).then(function(response){
                    console.log( JSON.stringify(response.data, null, "    ") );
                     if (response.data.status == 1){
                     location.reload();
                          $scope.statusMessage = "Successfully saved";
                     }else{
                          $scope.statusMessage = "Failed";
                     }
                  });
                  }
             };

 $scope.editCMS = function(event) {
               console.log("edit button clicked") ;
               console.log("Event obj   :  "+event);
               console.log(event.target.id) ;
               var event = event.target.id ;
               var Id = event.slice(-1);
               var elementId = "textArea_" + Id ;
               console.log(elementId) ;
                  var my_disply = document.getElementById(elementId).style.display;
                  if(my_disply == "block")
                        document.getElementById(elementId).style.display = "none";
                  else
                        document.getElementById(elementId).style.display = "block";

                var titleElementId = "title_" + Id
                var editable = document.getElementById(titleElementId).readOnly;
                if (editable == true){
                document.getElementById(titleElementId).readOnly = false ;
                }else{
                 document.getElementById(titleElementId).readOnly = true ;
                }

               };

 $scope.deleteCMSPage = function(event) {
      if (confirm("Are you sure want to delete!") == true) {
               console.log("delete confirmation clicked") ;
                            console.log(event.target.id) ;
                            var event = event.target.id ;
                            var Id = event.slice(-1);
                            var elementId = "pageId_" + Id ;
              var url = "/AppifyCartAdmin/rest/admin/deleteCMSPages.json/";
                      var postObject = new Object();
                     postObject.pageId =  document.getElementById(elementId).value ;
                     console.log( JSON.stringify(postObject, null, "    ") );
                      var config = {
                              headers: { 'Content-Type': 'application/json; charset=UTF-8'
                              }
                      };
                  $http.post(url, postObject, config).then(function(response){
                     console.log( JSON.stringify("this is response  "+response.data, null, "    ") );

                          if (response.data == true){
                          alert('Successfully Deleted');
                           location.reload() ;
                             $scope.statusMessage = "Successfully deleted";
                          }else{
                             $scope.statusMessage = "Failed";
                          }
                      });
         } else {
             console.log("Not deleted") ;
         }

    };

$scope.isEnableAction = function(checkboxElem) {
                 console.log(checkboxElem);
                 if (checkboxElem == true) {
                   $scope.checkStatus = true
                   alert ("hi");
                 } else {
                   $scope.checkStatus = false
                   alert ("bye");
                 }
               };

});