package com.hb.dokkan.common.exception.domain;

import com.hb.dokkan.common.constants.ResponseErrorCode;

/**
 * @Description xxxxx
 * @Author stargazer
 * @Date 2025/3/22 23:35
 **/
public class DokkanSysException extends RuntimeException{

    private ResponseErrorCode error;

    private String errorMsg;

    public DokkanSysException(ResponseErrorCode error) {
        this.error = error;
    }

    public DokkanSysException(ResponseErrorCode error, Throwable cause){
        super(error.getErrorMsg(),cause);
        this.error = error;
    }

    public DokkanSysException(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    @Override
    public String toString() {
        return String.format("[%s]%s",error.getErrorCode(),error.getErrorMsg());
    }

}
