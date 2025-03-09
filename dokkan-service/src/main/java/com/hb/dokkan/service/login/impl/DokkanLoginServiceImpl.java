package com.hb.dokkan.service.login.impl;

import com.hb.dokkan.common.domain.DokkanResponse;
import com.hb.dokkan.infrastructure.login.DokkanLoginRepository;
import com.hb.dokkan.service.login.DokkanLoginService;
import com.hb.dokkan.service.login.domain.dto.LoginRequestDTO;
import com.hb.dokkan.service.login.domain.dto.LoginResponseDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @Description xxxxx
 * @Author stargazer
 * @Date 2025/3/9 19:19
 **/
@Service
@Slf4j
public class DokkanLoginServiceImpl implements DokkanLoginService {

    @Resource
    private DokkanLoginRepository loginRepository;

    /**
     * 登录接口
     * @param loginRequestDTO loginRequestDTO
     * @return LoginResponseDTO
     */
    @Override
    public DokkanResponse<LoginResponseDTO> login(LoginRequestDTO loginRequestDTO) {
        return null;
    }
}
