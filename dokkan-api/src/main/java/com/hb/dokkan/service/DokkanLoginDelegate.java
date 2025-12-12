package com.hb.dokkan.service;

import com.hb.dokkan.api.login.domain.request.LoginRequest;
import com.hb.dokkan.api.login.domain.response.LoginResponse;
import com.hb.dokkan.common.domain.DokkanResponse;

/**
 * @Description 控制层-登录服务
 * @Author stargazer
 * @Date 2025/12/12 23:26
 **/
public interface DokkanLoginDelegate {
    /**
     * 登录接口
     */
    DokkanResponse<LoginResponse> login(LoginRequest request);
}
