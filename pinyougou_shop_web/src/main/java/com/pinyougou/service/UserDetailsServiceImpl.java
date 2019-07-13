package com.pinyougou.service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbSeller;
import com.pinyougou.sellergoods.service.SellerService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;
//@Service
public class UserDetailsServiceImpl  implements UserDetailsService {
    /**
     * 认证类
     * @param username
     * @return
     * @throws UsernameNotFoundException
     */
    @Reference
    private SellerService sellerService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //构建商家列表
        System.out.println("经过了UserDetailsServiceImpl");

       List< GrantedAuthority> grantedAuths =new ArrayList<>();
       grantedAuths.add(new SimpleGrantedAuthority("ROLE_SELLER"));

       //得到商家对象
        TbSeller seller = sellerService.findOne(username);
        if (seller!=null){
            if (seller.getStatus().equals("1")){
                return new User(username,seller.getPassword(),grantedAuths);
            }else {
                return null;
            }
        }
             return null;
    }
}
