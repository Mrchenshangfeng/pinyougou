 //控制层 
app.controller('goodsController' ,function($scope,$controller,$location,goodsService,uploadService,itemCatService,typeTemplateService){
	
	$controller('baseController',{$scope:$scope});//继承
	
    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		goodsService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	};
	
	//分页
	$scope.findPage=function(page,rows){			
		goodsService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
	
	//查询实体 
	$scope.findOne=function(){
		var id = $location.search().id; //获取参数
		if (id == null) {
			return;
		}
		goodsService.findOne(id).success(
			function(response){
				$scope.entity= response;
				//向富文本编辑器添加商品介绍
				editor.html($scope.entity.goodsDesc.introduction);
				//图片属性列表
				$scope.entity.goodsDesc.itemImages=JSON.parse($scope.entity.goodsDesc.itemImages);
				//扩展属性列表 要解决与//根据三级分类的id改变  来获取模板对象中的同种方法的冲突
				$scope.entity.goodsDesc.customAttributeItems=JSON.parse($scope.entity.goodsDesc.customAttributeItems);
				//规格属性列表
				$scope.entity.goodsDesc.specificationItems=JSON.parse($scope.entity.goodsDesc.specificationItems);
				//item列表规格列转换
				for (var i = 0; i < $scope.entity.itemList.length; i++) {
					$scope.entity.itemList[i].spec=JSON.parse($scope.entity.itemList[i].spec);
				}

			}
		);				
	};
	
	//保存 
	$scope.save=function(){
		$scope.entity.goodsDesc.introduction=editor.html();
		var serviceObject;//服务层对象  				
		if($scope.entity.goods.id!=null){//如果有ID
			serviceObject=goodsService.update( $scope.entity );//修改
		}else{
			serviceObject=goodsService.add( $scope.entity );//增加
		}				
		serviceObject.success(
			function(response){
				if(response.success){
					//重新查询
					alert("保存成功");
					location.href="goods_edit.html";
				}else{
					alert(response.message);
				}
			}		
		);				
	};
	
	 
	//批量删除 
	$scope.dele=function(){			
		//获取选中的复选框			
		goodsService.dele( $scope.selectIds ).success(
			function(response){
				if(response.success){
					$scope.reloadList();//刷新列表
					$scope.selectIds=[];
				}						
			}		
		);				
	}
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		goodsService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	};
	$scope.add=function () {
		$scope.entity.goodsDesc.introduction=editor.html();
		goodsService.add($scope.entity).success(function (response) {
			if (response.success) {
				alert("保存成功");
				$scope.entity={};
				editor.html(" ");
			}else{
				alert(response.message);
			}
		})
	};
	/**
	 * 上传图片
	 */
		$scope.uploadFile=function () {
			uploadService.uploadFile().success(function (response) {
			if (response.success){//如果上传成功  就取出url
				$scope.image_entity.url=response.message;//设置文件地址

			}else{
				alert(response.message);
			}
			}).error(function () {
				alert("上传发生错误")
			})
		};
		$scope.entity={goods:{},goodsDesc:{itemImages:[],specificationItems:[]}};//义页面实体结构
			//添加图片列表
		$scope.add_image_entity=function () {
		$scope.entity.goodsDesc.itemImages.push($scope.image_entity)
	};
	//列表中移除图片
	$scope.remove_image_entity=function(index){
		$scope.entity.goodsDesc.itemImages.splice(index,1);
	}
	//读取一级分类
	$scope.selectItemsList=function () {
		itemCatService.findByParentId(0).success(function (response) {
			$scope.itemCat1List=response;
		})
	}
	//读取二级分类
	$scope.$watch('entity.goods.category1Id',function (newValue, oldValue) {
		//根据选择的值，查询二级分类
		itemCatService.findByParentId(newValue).success(function (response) {
			$scope.itemCat2List=response;
		})
	});
	//查询三级分类
	$scope.$watch('entity.goods.category2Id',function (newValue, oldValue) {
		//根据选择的值，查询三级分类
		itemCatService.findByParentId(newValue).success(function (response) {
			$scope.itemCat3List=response;
		})
	});
	//查询模板id
	$scope.$watch('entity.goods.category3Id',function (newValue, oldValue) {
		//根据选择的值 查询模板的id
		itemCatService.findOne(newValue).success(function (response) {
			$scope.entity.goods.typeTemplateId=response.typeId;
		})
	});
	//查询扩展属性
	$scope.$watch('entity.goods.typeTemplateId',function (newValue, oldValue) {
		//根据三级分类的id改变  来获取模板对象
		typeTemplateService.findOne(newValue).success(function (response) {
			$scope.typeTemplate=response;//获取分类模板
			$scope.typeTemplate.brandIds=JSON.parse($scope.typeTemplate.brandIds);//品牌列表
			if ($location.search().id==null){
				$scope.entity.goodsDesc.customAttributeItems=JSON.parse(response.customAttributeItems);//扩展属性
			}
		});
		//查询规格列表
		typeTemplateService.findSpecList(newValue).success(function (response) {
			$scope.specList=response;
		})
	});
	$scope.updateSpecAttribute=function (event,specName,optionName) {

		var object = $scope.searchObjectByKey($scope.entity.goodsDesc.specificationItems,'attributeName',specName);
		if (event.target.checked){
			if (object==null){
				$scope.entity.goodsDesc.specificationItems.push({attributeName:specName,attributeValue:[optionName]})
			}else{
				object.attributeValue.push(optionName);

			}
		}else{
		var index=object.attributeValue.indexOf(optionName);
		object.attributeValue.splice(index,1);
			if (object.attributeValue.length==0){
			var objectIndex=$scope.entity.goodsDesc.specificationItems.indexOf(object);
			$scope.entity.goodsDesc.specificationItems.splice(objectIndex,1);
			}
		}
	};
	//生成sku列表
	$scope.createSKUItemList=function () {
		//初始化SKU列表：
		$scope.entity.itemList=[{spec:{},price:0,num:0,status:0,isDefault:0}];
		//获取选中规格 规格选项数组
		var specificationItems=$scope.entity.goodsDesc.specificationItems;
		//遍历选中规格，规格选项数组
		for (var i = 0; i < specificationItems.length; i++) {
			//{"attributeName":"网络","attributeValue":["移动3g","移动4g"]}
			var specificationItem=specificationItems[i];
			//获取选中规格名称
			var specName = specificationItem.attributeName;
			//获取规格选项的数组
			var options = specificationItem.attributeValue;
			//调用另一个方法 构建SKU列表
			$scope.entity.itemList=$scope.initSKUList($scope.entity.itemList,specName,options);
		}
	};
	$scope.initSKUList=function (list,specName,options) {
		var newSkuItemList=[];
		for (var i = 0; i < list.length; i++) {//循环SKU列表
			var skuItem=list[i];//获取SKU对象
			for (var j = 0; j < options.length; j++) {
				var option=options[j];//规格选项名称
				var newSkuItem=JSON.parse(JSON.stringify(skuItem));//克隆SKU对象
				newSkuItem.spec[specName]=option;//向克隆的SKU对象中添加规格名称及选项名称
				newSkuItemList.push(newSkuItem);
			}

		}
		return newSkuItemList;
	}
	$scope.status=['未审核','已审核','审核未通过','关闭'];
	//定义商品分类的
	$scope.itemCatList=[];//商品分类列表;
	//加载商品分类列表
	$scope.findItemCatList=function () {
		itemCatService.findAll().success(function (response) {
			for (var i = 0; i < response.length; i++) {
				$scope.itemCatList[response[i].id]=response[i].name;
			} 
		})
		
	};
	//根据规格名称和选项名称返回是否被勾选
	$scope.checkAttributeValue=function (specName, optionName) {
		var  specificationItems=$scope.entity.goodsDesc.specificationItems;
		var object = $scope.searchObjectByKey(specificationItems,'attributeName',specName);
		if (object == null) {
		return false;
		}else{
			return object.attributeValue.indexOf(optionName)>=0;
		}
	};
	$scope.marketableStatus=['未上架','已上架'];
	//设置商品上下架
	$scope.updateMarketable=function (status) {
		goodsService.updateMarketable($scope.selectIds,status).success(function (response) {
			if (response.success){
				$scope.reloadList();
				$scope.selectIds=[];
			} else{
				alert(response.message);
			}
		})
	}
});	
