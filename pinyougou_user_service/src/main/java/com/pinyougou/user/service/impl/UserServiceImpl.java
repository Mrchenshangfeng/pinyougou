package com.pinyougou.user.service.impl;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.pinyougou.entity.PageResult;
import com.pinyougou.user.service.UserService;
import org.apache.activemq.Message;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbUserMapper;
import com.pinyougou.pojo.TbUser;
import com.pinyougou.pojo.TbUserExample;
import com.pinyougou.pojo.TbUserExample.Criteria;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Session;


/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
public class UserServiceImpl implements UserService {

	@Autowired
	private TbUserMapper userMapper;
	
	/**
	 * 查询全部
	 */
	@Override
	public List<TbUser> findAll() {
		return userMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbUser> page=   (Page<TbUser>) userMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(TbUser user) {
		user.setUpdated(new Date());//更新时间
		user.setCreated(new Date());//创建日期
		String password = DigestUtils.md5Hex(user.getPassword());
		user.setPassword(password);
		userMapper.insert(user);

	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(TbUser user){
		userMapper.updateByPrimaryKey(user);
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public TbUser findOne(Long id){
		return userMapper.selectByPrimaryKey(id);
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			userMapper.deleteByPrimaryKey(id);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbUser user, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbUserExample example=new TbUserExample();
		Criteria criteria = example.createCriteria();
		
		if(user!=null){			
						if(user.getPassword()!=null && user.getPassword().length()>0){
				criteria.andPasswordLike("%"+user.getPassword()+"%");
			}
			if(user.getUsername()!=null && user.getUsername().length()>0){
				criteria.andUsernameLike("%"+user.getUsername()+"%");
			}
	
		}
		
		Page<TbUser> page= (Page<TbUser>)userMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}
	@Autowired
	private JmsTemplate jmsTemplate;
	@Autowired
	private Destination smsDestination;

	@Value("${template_code}")
	private String template_code;



	@Value("${sign_name}")
	private String sign_name;


	/**
	 * 生成短信验证码
	 */
	@Autowired
	private RedisTemplate<String , Object> redisTemplate;

	@Override
	public void createSmsCode(final String phone) {

			//生成6位随机数
			 final String code =  (long) (Math.random()*1000000)+"";
			System.out.println("验证码："+code);
			//存入缓存
			redisTemplate.boundHashOps("smscode").put(phone, code);
			//发送到activeMQ	....
			jmsTemplate.send(smsDestination, new MessageCreator() {
			@Override
			public Message createMessage(Session session) throws JMSException {
				MapMessage mapMessage = session.createMapMessage();
				mapMessage.setString("mobile", phone);//手机号
				mapMessage.setString("template_code", template_code);//模板编号
				mapMessage.setString("sign_name", sign_name);//签名
				Map m=new HashMap<>();
				m.put("code", code);
				mapMessage.setString("param", JSON.toJSONString(m));//参数
				return (Message) mapMessage;
			}
		});

	}


	/**
	 * 判断验证码是否正确
	 */
	@Override
	public boolean checkSmsCode(String phone, String code) {
		String sysCode = (String) redisTemplate.boundHashOps("smscode").get(phone);
		if(sysCode==null){
			return false;
		}
		if(!sysCode.equals(code)){
			return false;
		}
		return true;
	}


}

