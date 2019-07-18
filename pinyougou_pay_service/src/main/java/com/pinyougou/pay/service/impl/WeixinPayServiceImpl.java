package com.pinyougou.pay.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.github.wxpay.sdk.WXPayUtil;
import com.pinyougou.pay.service.WeixinPayService;
import org.springframework.beans.factory.annotation.Value;
import utils.HttpClient;

import java.util.HashMap;
import java.util.Map;
@Service
public class WeixinPayServiceImpl  implements WeixinPayService {
    @Value("${appid}")
    private String appid;

    @Value("${partner}")
    private String partner;

    @Value("${partnerkey}")
    private String partnerkey;

    /**
     * 生成二维码
     * @return
     */

    @Override
    public Map createNative(String out_trade_no, String total_fee) {
        //1.创建参数
        Map<String, String> param = new HashMap<>();
        //公众号
        param.put("appid",appid);
        //商户号
        param.put("mch_id",partner);
        //随机字符串
        param.put("nonce_str", WXPayUtil.generateNonceStr());
        //商品描述
        param.put("body","品优购");
        //商户订单号
        param.put("out_trade_no",out_trade_no);
        //总金额（分）
        param.put("total_fee",total_fee);
        //IP
        param.put("spbill_create_ip","127.0.0.1");
        //回调地址(随便写)
        param.put("notify_url", "http://test.itcast.cn");
        //交易类型
        param.put("trade_type","NATIVE");

        try {
            //2.生成要发送的xml请求
            String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);
            System.out.println("请求的参数："+xmlParam);//打印生成得xml
            HttpClient httpClient = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            httpClient.setHttps(true);
            httpClient.setXmlParam(xmlParam);
            httpClient.post();
            //3.获得结果
            String xmlResult = httpClient.getContent();
            System.out.println("Response XML: " + xmlResult);
            Map<String, String> mapResult = WXPayUtil.xmlToMap(xmlResult);
            System.out.println("微信返回结果"+mapResult);

            Map map = new HashMap<>();
            map.put("code_url", mapResult.get("code_url"));//生成支付二维码的链接
            map.put("out_trade_no", out_trade_no);
            map.put("total_fee", total_fee);
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap();
        }

    }
    //查询支付状态
    @Override
    public Map queryPayStatus(String out_trade_no) {
        Map param=new HashMap();
        param.put("appid",appid);//公众账号ID
        param.put("out_trade_no",out_trade_no);///订单号
        param.put("mch_id",partner);//商户号
        param.put("nonce_str", WXPayUtil.generateNonceStr());//随机字符串

        try {
            String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);
            HttpClient httpClient=new HttpClient("https://api.mch.weixin.qq.com/pay/orderquery");
            httpClient.setHttps(true);
            httpClient.setXmlParam(xmlParam);
            httpClient.post();
            String xmlResult = httpClient.getContent();
            Map<String, String> map = WXPayUtil.xmlToMap(xmlResult);
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    /**
     * 关闭支付
     * @param out_trade_no
     * @return
     */

    @Override
    public Map closePay(String out_trade_no) {
        Map param=new HashMap();
        param.put("appid", appid);//公众账号ID
        param.put("mch_id", partner);//商户号
        param.put("out_trade_no", out_trade_no);//订单号
        param.put("nonce_str", WXPayUtil.generateNonceStr());//随机字符串
        String url="https://api.mch.weixin.qq.com/pay/closeorder";
        try {
            String xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);
            HttpClient client=new HttpClient(url);
            client.setHttps(true);
            client.setXmlParam(xmlParam);
            client.post();
            String result = client.getContent();
            Map<String, String> map = WXPayUtil.xmlToMap(result);
            System.out.println(map);
            return map;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

    }


}
