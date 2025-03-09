package com.hb.dokkan.api.login;

import com.hb.dokkan.api.login.domain.convert.DokkanLoginConvert;
import com.hb.dokkan.api.login.domain.request.LoginRequest;
import com.hb.dokkan.common.domain.DokkanResponse;
import com.hb.dokkan.service.login.DokkanLoginService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @Description 登录模块
 * @Author stargazer
 * @Date 2025/3/9 18:20
 **/
@RestController
@RequestMapping("/login")
public class DokkanLoginApi {
    @Resource
    private DokkanLoginService dokkanLoginService;

    @Resource
    private DokkanLoginConvert convert;

    /**
     * 登录接口
     * @param loginRequest 登录参数
     * @return DokkanResponse
     */
    @PostMapping("")
    public DokkanResponse login(@RequestBody LoginRequest loginRequest){
        return dokkanLoginService.login(convert.loginRequest2Dto(loginRequest));
    }


}
