package com.hb.dokkan.service.login.impl;

import com.hb.dokkan.api.login.domain.request.LoginRequest;
import com.hb.dokkan.api.login.domain.response.LoginResponse;
import com.hb.dokkan.common.domain.DokkanResponse;
import com.hb.dokkan.service.DokkanLoginDelegate;
import com.hb.dokkan.service.convert.DokkanLoginConvert;
import com.hb.dokkan.service.login.DokkanLoginService;
import com.hb.dokkan.service.login.domain.dto.LoginResponseDTO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * @Description xxxxx
 * @Author stargazer
 * @Date 2025/12/13 0:42
 **/
@Service
public class DokkanLoginDelegateImpl implements DokkanLoginDelegate {

    @Resource
    private DokkanLoginService loginService;

    @Resource
    private DokkanLoginConvert loginConvert;

    /**
     * 登录接口
     *
     */
    @Override
    public DokkanResponse<LoginResponse> login(LoginRequest request) {
        LoginResponseDTO response = loginService.login(loginConvert.loginRequest2Dto(request));
        return DokkanResponse.<LoginResponse>builder().withModel(loginConvert.loginResponseDto2Response(response));
    }
}
