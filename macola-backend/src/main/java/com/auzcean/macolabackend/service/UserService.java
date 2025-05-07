package com.auzcean.macolabackend.service;

import com.auzcean.macolabackend.model.dto.user.UserQueryRequest;
import com.auzcean.macolabackend.model.entity.User;
import com.auzcean.macolabackend.model.v0.LoginUserV0;
import com.auzcean.macolabackend.model.v0.UserV0;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
* @author xchuckie
* @description 针对表【user(用户)】的数据库操作Service
* @createDate 2025-04-25 18:20:46
*/
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
     * @return 新用户 id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword);

    /**
     * 密码进行加密操作
     * @param password 需要加密的密码
     * @return 加密后密码
     */
    String getEncryptPassword(String password);

    /**
     * 用户登录
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param request       服务端请求服务，用于存储用户登录状态信息
     * @return 脱敏后的用户信息
     */
    LoginUserV0 userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 对用户登录信息进行脱敏操作
     * @param user 需要脱敏的用户
     * @return 返回脱敏的用户
     */
    LoginUserV0 getLoginUserV0(User user);

    /**
     * 获取登录用户信息
     *
     * @param request  服务端请求服务，用于存储用户登录状态信息
     * @return  脱敏后的用户信息
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 对用户信息进行脱敏
     * @param user  用户
     * @return  脱敏后的用户信息
     */
    UserV0 getUserV0(User user);

    /**
     * 获得脱敏后的用户列表
     * @param userList 用户列表
     * @return 脱敏后的用户列表
     */
    List<UserV0> getUserV0List(List<User> userList);

    /**
     * 用户注销
     * @param request   用户请求服务端
     * @return  返回注销成功信息
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 请求对象转换为SQL查询
     * @param userQueryRequest  请求对象
     * @return  SQL查询
     */
    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

    /**
     * 是否为管理员
     *
     * @param user 用户信息
     * @return
     */
    boolean isAdmin(User user);
}
