package com.hb.dokkan.api.login.domain.request;

import lombok.Data;

import java.io.Serializable;

/**
 * @Description xxxxx
 * @Author stargazer
 * @Date 2025/3/9 19:00
 **/
@Data
public class LoginRequest implements Serializable {
    private static final long serialVersionUID = -8845154401026378839L;

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
