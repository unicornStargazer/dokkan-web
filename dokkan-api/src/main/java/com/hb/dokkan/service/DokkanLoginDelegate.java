package com.hb.dokkan.service;

import com.hb.dokkan.common.domain.request.login.LoginRequest;
import com.hb.dokkan.common.domain.response.base.DokkanResponse;
import com.hb.dokkan.common.domain.response.login.LoginResponse;

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
