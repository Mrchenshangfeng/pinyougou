package com.pinyougou.shop.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.entity.PageResult;
import com.pinyougou.entity.Result;
import com.pinyougou.option_specification.Goods;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.sellergoods.service.GoodsService;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * controller
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {

	@Reference
	private GoodsService goodsService;
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findAll")
	public List<TbGoods> findAll(){			
		return goodsService.findAll();
	}
	
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findPage")
	public PageResult findPage(int page, int rows){
		return goodsService.findPage(page, rows);
	}
	
	/**
	 * 增加
	 * @param goods
	 * @return
	 */
	/**
	 * {"goods":{"category1Id":1,"category2Id":2,"category3Id":3,"typeTemplateId":35,"goodsName":"海贼王","brandId":4,"caption":"零零零零","price":"1266","isEnableSpec":"1"},"goodsDesc":{"itemImages":[{"color":"红黑色","url":"http://192.168.25.133/group1/M00/00/00/wKgZhV0V-5iAWWTjAAE9EoOCOFA417.jpg"}],"specificationItems":[{"attributeName":"网络","attributeValue":["移动3G","移动4G"]},{"attributeName":"机身内存","attributeValue":["16G"]}],"customAttributeItems":[{"text":"内存大小","value":"50g"},{"text":"颜色","value":"褐色"}],"packageList":"无包装","saleService":"无","introduction":"好看<img src=\"http://localhost:9102/plugins/kindeditor/plugins/emoticons/images/10.gif\" alt=\"\" border=\"0\" />"},"itemList":[{"spec":{"网络":"移动3G","机身内存":"16G"},"price":"500","num":"9999","status":"1","isDefault":"1"},{"spec":{"网络":"移动4G","机身内存":"16G"},"price":"600","num":"9999","status":"1","isDefault":0}]}
	 * @param goods
	 * @return
	 */
	@RequestMapping("/add")
	public Result add(@RequestBody Goods goods){
		//获取登录名
		String name = SecurityContextHolder.getContext().getAuthentication().getName();
		goods.getGoods().setSellerId(name); //设置商家ID
		try {
			goodsService.add(goods);
			return new Result(true, "增加成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "增加失败");
		}
	}
	
	/**
	 * 修改
	 * @param goods
	 * @return
	 */
	@RequestMapping("/update")
	public Result update(@RequestBody Goods goods){
		try {
			goodsService.update(goods);
			return new Result(true, "修改成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "修改失败");
		}
	}	
	
	/**
	 * 获取实体
	 * @param id
	 * @return
	 */
	@RequestMapping("/findOne")
	public Goods findOne(Long id){
		return goodsService.findOne(id);		
	}
	
	/**
	 * 批量删除
	 * @param ids
	 * @return
	 */
	@RequestMapping("/delete")
	public Result delete(Long [] ids){
		try {
			goodsService.delete(ids);
			return new Result(true, "删除成功"); 
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "删除失败");
		}
	}
	
		/**
	 * 查询+分页
	 * @param
	 * @param page
	 * @param rows
	 * @return
	 */
	@RequestMapping("/search")
	public PageResult search(@RequestBody TbGoods goods, int page, int rows  ){
		String name = SecurityContextHolder.getContext().getAuthentication().getName();
		goods.setSellerId(name);
		return goodsService.findPage(goods, page, rows);		
	}
	@RequestMapping("/updateMarketable")
	public Result updateMarketable(Long [] ids,String status){
		try {
			goodsService.updateMarketable(ids,status);
			return new Result(true,"更改成功");
		} catch (Exception e) {
			e.printStackTrace();
			return  new Result(false,"更改失败");
		}
	}

}
