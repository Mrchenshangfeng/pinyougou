app.controller('searchController' ,function($scope,$location,searchService){

//搜索
    $scope.search=function () {
        $scope.searchMap.pageNo= parseInt($scope.searchMap.pageNo) ;
        searchService.search($scope.searchMap).success(function (response) {
            $scope.resultMap=response;
            buildPageLabel();//调用
        })
    };
    //定义前端数据结构
    $scope.searchMap={keywords:"",categoryName:"",brand:"",spec:{},price:"",pageNo:1,pageSize:40,sortField:'',sort:'' };
    //添加搜索项
    $scope.addSearchItem=function (name,value) {
        if (name=='brand'||name=='categoryName'|| name=='price'){
            $scope.searchMap[name]=value;//分类的商品
        }else{
            $scope.searchMap.spec[name]=value;//移除此属性
        }
        $scope.search();//执行搜索
    };
    //移除符合搜索条件
    $scope.removeSearchItem=function (name) {
        if (name=='categoryName'|| name=='brand'||name=='price'){//分类的商品
            $scope.searchMap[name]='';
        }else{//否则  就是规格
            delete $scope.searchMap.spec[name];//移除此属性
        }
        $scope.search();//执行搜索
    }
    //构建分页标签(totalPages为总页数)
     buildPageLabel=function () {
         $scope.pageLabel=[];//新增分页栏属性
         var maxPageNo=$scope.resultMap.totalPages;//总页数
         var firstPage=1;
         var lastPage=maxPageNo;
         $scope.firstDot=true;//前面有点
         $scope.lastDot=true;//后边有点

         if ($scope.resultMap.totalPages>5){ //如果总页数大于5页,显示部分页码
                if ($scope.resultMap.pageNo<3){//如果当前页小于等于3
                    lastPage=5;
                    $scope.firstDot=false;//前面没点
                }else if ($scope.resultMap.pageNo >= lastPage - 2) {//如果当前页大于等于最大页码-2
                    firstPage=maxPageNo-4;//后五页
                    $scope.firstDot=false;//前面没点
                }else{ //显示当前页为中心的5页
                    firstPage=$scope.searchMap.pageNo-2;
                    lastPage=$scope.searchMap.pageNo+2;

                }
         }else{
             $scope.firstDot=false;//前面有点
             $scope.lastDot=false;//后边有点

         }
         //循环产生页码标签
         for(var i=firstPage;i<=lastPage;i++) {
             $scope.pageLabel.push(i);
         }
     };
    //根据页码查询
    $scope.queryByPage=function (pageNo) {
    //页码确定
        if (pageNo<1||pageNo>$scope.resultMap.totalPages){
            return;
        }
        $scope.searchMap.pageNo=pageNo;
        $scope.search();

    }
    //判断当前是否是第一页
    $scope.isToPage=function () {
        if ($scope.searchMap.pageNo==1){
            return true;
        }else {
            return false;
        }
    };
    //判断当前是否最后一页
    $scope.isEndPage=function () {
        if ($scope.searchMap.pageNo==$scope.resultMap.totalPages){
            return true;
        }else {
            return false;
        }
    };
    //设置排序规则
    $scope.sortSearch=function (sortField, sort) {
        $scope.searchMap.sortField=sortField;
        $scope.searchMap.sort=sort;
        $scope.search();
    };
    //判断关键字是否包含品牌
    $scope.keywordsIsBrand=function () {
        for (var i = 0; i <$scope.resultMap.brandList.length; i++) {
            if ($scope.searchMap.keywords.indexOf($scope.resultMap.brandList[i].text)>=0){
                //如果包含就返回true；
                return true;

            }
        }
        return false;

    };
    //加载查询字符串
    $scope.loadkeywords=function(){
        $scope.searchMap.keywords=  $location.search()['keywords'];
        if($scope.searchMap.keywords == null) {
            $scope.searchMap.keywords = "";
        }
        $scope.search();
    }


});

