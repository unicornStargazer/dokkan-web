package com.hb.dokkan.common.exception.domain;

import lombok.Getter;

import java.io.Serializable;

/**
 * @Description DokkanErrorCode
 * @Author stargazer
 * @Date 2025/3/22 18:35
 **/
@Getter
public class DokkanErrorCode implements Serializable {
    private static final long serialVersionUID = -7724915182667643468L;
    private final String errorCode;
    private final String errorMsg;

    public DokkanErrorCode(String errorCode, String errorMsg) {
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
    }

    public static DokkanErrorCode buildError(String errorCode, String errorMsg) {
        return new DokkanErrorCode(errorCode,errorMsg);
    }

    @Override
    public String toString() {
        String var = this.getErrorCode();
        return "DokkanErrorCode(errorCode="+var+",errorMsg="+this.getErrorMsg()+")";
    }

}
