package com.pinyougou.cart.service;

      /* 购物车服务接口
        * @author Administrator
        *
        */

import com.pinyougou.pojogroup.Cart;

import java.util.List;

public interface CartService {
    /**
     * 添加商品到购物车
     * @param cartList
     * @param itemId
     * @param num
     * @return
     */
    List<Cart> addGoodsToCartList(List<Cart> cartList,Long itemId ,Integer num);

}
