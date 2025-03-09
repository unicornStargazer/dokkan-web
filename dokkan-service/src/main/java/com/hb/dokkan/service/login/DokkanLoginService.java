package com.hb.dokkan.service.login;

import com.hb.dokkan.common.domain.DokkanResponse;
import com.hb.dokkan.service.login.domain.dto.LoginRequestDTO;
import com.hb.dokkan.service.login.domain.dto.LoginResponseDTO;

/**
 * @Description xxxxx
 * @Author stargazer
 * @Date 2025/3/9 19:12
 **/
public interface DokkanLoginService {
    /**
     * 登录接口
     * @param loginRequestDTO loginRequestDTO
     * @return LoginResponseDTO
     */
    DokkanResponse<LoginResponseDTO> login(LoginRequestDTO loginRequestDTO);
}
