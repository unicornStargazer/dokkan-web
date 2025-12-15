package com.hb.dokkan.service;

import com.hb.dokkan.common.domain.request.login.LoginRequest;
import com.hb.dokkan.common.domain.response.login.LoginResponse;
import com.hb.dokkan.common.domain.response.base.DokkanResponse;
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
public class DokkanLoginDelegate {

    @Resource
    private DokkanLoginService loginService;

    @Resource
    private DokkanLoginConvert loginConvert;

    /**
     * 登录接口
     *
     */
    public DokkanResponse<LoginResponse> login(LoginRequest request) {
        LoginResponseDTO response = loginService.login(loginConvert.loginRequest2Dto(request));
        return DokkanResponse.<LoginResponse>builder()
                .withModel(loginConvert.loginResponseDto2Response(response))
                .success();
    }
}
