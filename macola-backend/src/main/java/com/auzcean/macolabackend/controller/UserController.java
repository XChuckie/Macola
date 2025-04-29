package com.auzcean.macolabackend.controller;

import com.auzcean.macolabackend.annotation.AuthCheck;
import com.auzcean.macolabackend.common.BaseResponse;
import com.auzcean.macolabackend.common.DeleteRequest;
import com.auzcean.macolabackend.common.ResultUtils;
import com.auzcean.macolabackend.constant.UserConstant;
import com.auzcean.macolabackend.exception.BusinessException;
import com.auzcean.macolabackend.exception.ErrorCode;
import com.auzcean.macolabackend.exception.ThrowUtils;
import com.auzcean.macolabackend.model.dto.user.*;
import com.auzcean.macolabackend.model.entity.User;
import com.auzcean.macolabackend.model.v0.LoginUserV0;
import com.auzcean.macolabackend.model.v0.UserV0;
import com.auzcean.macolabackend.service.UserService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 用户注册
     * @param userRegisterRequest 请求参数
     * @return  返回注册成功信息
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        ThrowUtils.throwIf(userRegisterRequest == null, ErrorCode.PARAMS_ERROR);

        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();

        long result = userService.userRegister(userAccount, userPassword, checkPassword);
        return ResultUtils.success(result);
    }

    /**
     * 用户登录
     * @param userLoginRequest 请求参数
     * @param request 服务端请求服务
     * @return  返回登录成功信息
     */
    @PostMapping("/login")
    public BaseResponse<LoginUserV0> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(userLoginRequest == null, ErrorCode.PARAMS_ERROR);

        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();

        LoginUserV0 loginUserV0= userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(loginUserV0);
    }

    /**
     * 获取登录用户信息
     * @param request 服务端请求服务
     * @return  返回登录成功信息
     */
    @GetMapping("/get/login")
    public BaseResponse<LoginUserV0> getLoginUser(HttpServletRequest request) {

        User user = userService.getLoginUser(request);
        return ResultUtils.success(userService.getLoginUserV0(user));  // 对数据进行脱敏操作
    }

    /**
     * 用户注销
     * @param request 前端发给后端的请求
     * @return 返回注销成功信息
     */
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        return ResultUtils.success(userService.userLogout(request));
    }

    /**
     * 增加用户（仅管理员）
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest) {
        ThrowUtils.throwIf(userAddRequest == null, ErrorCode.PARAMS_ERROR);

        User user = new User();
        BeanUtils.copyProperties(userAddRequest, user);
        // 1. 设置默认密码
        final String DEFAULT_PASSWORD = "12345678";
        String encryptPassword = userService.getEncryptPassword(DEFAULT_PASSWORD);
        user.setUserPassword(encryptPassword);
        // 2. 将数据创建到数据库中
        try {
            boolean result = userService.save(user);
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "用户账号已存在");
        }
        return ResultUtils.success(user.getId());
    }

    /**
     * 根据 id 获取用户（仅管理员）
     * @return 返回不脱敏数据
     */
    @GetMapping("/get/")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<User> getUserById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(user);
    }

    /**
     * 根据 id 获取包装类
     * @return 脱敏后的数据
     */
    @GetMapping("/get/v0/")
    public BaseResponse<UserV0> getUserV0ById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(userService.getUserV0(user));
    }

    /**
     * 删除用户（仅管理员）
     */
    @PostMapping("/delete/")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean result = userService.removeById(deleteRequest.getId());
        return ResultUtils.success(result);
    }

    /**
     * 更新用户（仅管理员）
     */
    @PostMapping("/update/")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest) {
        ThrowUtils.throwIf(userUpdateRequest == null, ErrorCode.PARAMS_ERROR);

        User user = new User();
        BeanUtils.copyProperties(userUpdateRequest, user);
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);

        return ResultUtils.success(true);
    }

    /**
     * 用户分页查询
     */
    @PostMapping("/list/page/vo")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<UserV0>> listUserV0ByPage(@RequestBody UserQueryRequest userQueryRequest) {
        ThrowUtils.throwIf(userQueryRequest == null, ErrorCode.PARAMS_ERROR);

        long current = userQueryRequest.getCurrent();
        long pageSize = userQueryRequest.getPageSize();
        // 1. 根据参数调用封装的getQueryWrapper获取分页查询结果
        Page<User> userPage = userService.page(new Page<>(current, pageSize), userService.getQueryWrapper(userQueryRequest));
        // 2. 对查询结果进行脱敏操作
        List<UserV0> userV0List = userService.getUserV0List(userPage.getRecords());
        Page<UserV0> userV0Page = new Page<>(current, pageSize, userPage.getTotal());
        userV0Page.setRecords(userV0List);

        return ResultUtils.success(userV0Page);
    }

}
