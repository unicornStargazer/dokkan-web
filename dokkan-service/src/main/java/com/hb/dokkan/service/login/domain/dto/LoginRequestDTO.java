package com.hb.dokkan.service.login.domain.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description xxxxx
 * @Author stargazer
 * @Date 2025/3/9 19:12
 **/
@Data
public class LoginRequestDTO implements Serializable {
    private static final long serialVersionUID = -5031666760976020958L;

    /**
     * 用户id
     */
    private String userId;

    /**
     * 密码
     */
    private String password;

    /**
     * 用户昵称
     */
    private String userNickName;
}
