package com.auzcean.macolabackend.model.v0;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * (脱敏)用户登录信息
 */
@Data
public class LoginUserV0 implements Serializable {
    private static final long serialVersionUID = 7919082601832698310L;
    /**
     * id
     */
    private Long id;

    /**
     * 账号
     */
    private String userAccount;


    /**
     * 用户昵称
     */
    private String userName;

    /**
     * 用户头像
     */
    private String userAvatar;

    /**
     * 用户简介
     */
    private String userProfile;

    /**
     * 用户角色：user/admin
     */
    private String userRole;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;
}