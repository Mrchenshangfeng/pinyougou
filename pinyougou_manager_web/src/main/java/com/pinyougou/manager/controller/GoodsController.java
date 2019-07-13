package com.pinyougou.manager.controller;
import java.util.List;

import com.alibaba.fastjson.JSON;
import com.pinyougou.entity.PageResult;
import com.pinyougou.entity.Result;
import com.pinyougou.option_specification.Goods;
import com.pinyougou.pojo.TbItem;
import org.apache.activemq.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.alibaba.dubbo.config.annotation.Reference;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.sellergoods.service.GoodsService;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Session;


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
	@Autowired
	private Destination queueTextDestination;//用于发送solr导入的消息
	@Autowired
	private JmsTemplate jmsTemplate;
	@Autowired
	private Destination queueSolrDeleteDestination;
	@Autowired
	private Destination topicPageDestination;//用户在索引库中删除记录
	@Autowired
	private Destination topicPageDeleteDestination;//用于删除静态网页的消息

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
	@RequestMapping("/add")
	public Result add(@RequestBody Goods goods){
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
		//获取当前商品商家的id
		String sellerId = goods.getGoods().getSellerId();
		//获取当前登录的商家id
		String name = SecurityContextHolder.getContext().getAuthentication().getName();
		if (!sellerId.equals(name)){
			return  new Result(false,"非法操作");
		}
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
				jmsTemplate.send(queueSolrDeleteDestination, new MessageCreator() {
				@Override
				public javax.jms.Message createMessage(Session session) throws JMSException {
				return session.createObjectMessage(ids);
				}
		});
			//删除页面
			jmsTemplate.send(topicPageDeleteDestination, new MessageCreator() {
				@Override
				public Message createMessage(Session session) throws JMSException {
					return (Message) session.createObjectMessage(ids);
				}
			});
			/*itemSearchService.deleteByGoodsIds(Arrays.asList(ids));*/
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
		return goodsService.findPage(goods, page, rows);		
	}
	/**
	 * 更新状态
	 * @param ids
	 * @param status
	 * @return
	 */
	/*@Reference
	private ItemSearchService itemSearchService;*/

	@RequestMapping("/updateStatus")
	public Result updateStatus(Long [] ids,String status){
		try {
			goodsService.updateStatus(ids,status);
			if(status.equals("1")) {//审核通过
				List<TbItem> itemList = goodsService.findItemListByGoodsIdandStatus(ids, status);
				//调用搜索接口实现数据批量导入
				if (itemList.size() > 0) {
                    final String jsonString = JSON.toJSONString(itemList);
                    jmsTemplate.send(queueTextDestination, new MessageCreator() {
                        @Override
                        public Message createMessage(Session session) throws JMSException {
                            return (Message) session.createTextMessage(jsonString);
                        }
                    });
                }
				/*if (itemList.size() > 0) {
					itemSearchService.importList(itemList);
				} else {
					System.out.println("没有明细数据");
				}*/
				//静态页生成
			/*	for(Long goodsId:ids){
					itemPageService.genItemHtml(goodsId);
				}*/
			for (final  Long goodsId : ids) {
			jmsTemplate.send(topicPageDestination, new MessageCreator() {
			@Override
			public javax.jms.Message createMessage(Session session) throws JMSException {
				return session.createTextMessage(goodsId+"");
			}
		});

				}
			}
				return new Result(true,"更新成功");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new Result(false,"更新失败");
	}
	/*@Reference(timeout =60000)
	private ItemPageService itemPageService;*/
	/**
	 * 生成静态页（测试）
	 * @param goodsId
	 */
	/*@RequestMapping("/genHtml")
	public  void genHtml(Long goodsId){
	itemPageService.genItemHtml(goodsId);
	}*/

}
