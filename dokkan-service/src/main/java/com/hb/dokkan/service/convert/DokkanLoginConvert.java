package com.hb.dokkan.service.convert;

import com.hb.dokkan.common.domain.request.login.LoginRequest;
import com.hb.dokkan.common.domain.response.login.LoginResponse;
import com.hb.dokkan.service.login.domain.dto.LoginRequestDTO;
import com.hb.dokkan.service.login.domain.dto.LoginResponseDTO;
import org.mapstruct.Mapper;

/**
 * @Description xxxxx
 * @Author stargazer
 * @Date 2025/3/9 19:08
 **/

@Mapper(componentModel = "spring")
public interface DokkanLoginConvert {
    /**
     * loginRequest2Dto
     */
    LoginRequestDTO loginRequest2Dto(LoginRequest loginRequest);

    /**
     * loginResponseDto2Response
     */
    LoginResponse loginResponseDto2Response(LoginResponseDTO response);
}
