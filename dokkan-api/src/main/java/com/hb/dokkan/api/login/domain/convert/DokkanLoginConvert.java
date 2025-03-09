package com.hb.dokkan.api.login.domain.convert;

import com.hb.dokkan.api.login.domain.request.LoginRequest;
import com.hb.dokkan.service.login.domain.dto.LoginRequestDTO;
import org.mapstruct.Mapper;

/**
 * @Description xxxxx
 * @Author stargazer
 * @Date 2025/3/9 19:08
 **/

@Mapper(componentModel = "spring")
public interface DokkanLoginConvert {
    LoginRequestDTO loginRequest2Dto(LoginRequest loginRequest);
}
