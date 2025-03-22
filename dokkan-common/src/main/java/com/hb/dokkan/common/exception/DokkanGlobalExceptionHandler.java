package com.hb.dokkan.common.exception;

import com.hb.dokkan.common.domain.DokkanResponse;
import com.hb.dokkan.common.exception.domain.DokkanBizException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @Description xxxxx
 * @Author stargazer
 * @Date 2025/3/22 18:31
 **/
@RestControllerAdvice
@Slf4j
public class DokkanGlobalExceptionHandler {

    @ExceptionHandler(DokkanBizException.class)
    public DokkanResponse dokkanBizExceptionHandler(DokkanBizException ex){
        log.error("biz exception,error:{},e",ex.getMessage(),ex.getCause());
        return DokkanResponse.builder().fail(ex.getError());
    }

}
