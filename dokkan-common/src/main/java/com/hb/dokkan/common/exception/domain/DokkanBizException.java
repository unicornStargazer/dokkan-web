package com.hb.dokkan.common.exception.domain;

import com.hb.dokkan.common.constants.ExceptionErrorCode;
import lombok.Getter;

/**
 * @Description xxxxx
 * @Author stargazer
 * @Date 2025/3/22 18:33
 **/
@Getter
public class DokkanBizException extends RuntimeException{

    private ExceptionErrorCode error;


    public DokkanBizException(ExceptionErrorCode error) {
        this.error = error;
    }

    public DokkanBizException(ExceptionErrorCode error, Throwable cause){
        super(error.getErrorMsg(),cause);
        this.error = error;
    }

    public DokkanBizException(String msg){
        super(msg);
    }

    @Override
    public String toString() {
        return String.format("[%s]%s",error.getErrorCode(),error.getErrorMsg());
    }

}
