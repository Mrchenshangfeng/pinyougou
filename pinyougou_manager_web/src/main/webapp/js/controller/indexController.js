app.controller('indexController',function ($scope,loginService) {
    //读取当前登陆人
    $scope.showLoginName=function () {
        loginService.loginName().success(function (response) {
            $scope.loginName=response.loginName;
        })
    }

})