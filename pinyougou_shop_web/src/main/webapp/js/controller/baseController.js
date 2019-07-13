app.controller('baseController',function ($scope) {

    //分页控件配置
    $scope.paginationConf={
        currentPage:1,
        totalItems:100,
        itemsPerPage:10,
        perPageOptions:[10,20,30,40],
        onChange:function () {
            $scope.reloadList();//重新加载
        }
    };
    $scope.reloadList=function () {
        //切换页面
        $scope.search($scope.paginationConf.currentPage,$scope.paginationConf.itemsPerPage);
    };
    $scope.selectIds=[];//选中的ID集合
    //更新复选
    $scope.updateSelection=function ($event,id) {
        if ($event.target.checked){//如果是没被选中，则增加到数组
            $scope.selectIds.push(id);
        }else{
            var idx=$scope.selectIds.indexOf(id);
            $scope.selectIds.splice(idx,1)//删除  取消选中的复选框
        }
    };
    //从集合中按照key查询对象
    $scope.searchObjectByKey=function (list,key,keyValue) {
        for (var i = 0; i < list.length; i++) {
        var o=list[i];
            if (o[key]==keyValue){
                return o;
            }
        }
        return null;

    }
});