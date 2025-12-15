package com.hb.dokkan.common.domain.response.base;

import com.hb.dokkan.common.constants.ResponseErrorCode;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.ObjectUtils;

import java.io.Serializable;

/**
 * @Description 通用响应类
 * @Author stargazer
 * @Date 2025/3/9 18:21
 **/
@Getter
@Setter
public final class DokkanResponse<T> implements Serializable {
    private static final long serialVersionUID = 6993775166278370916L;

    private String errorCode;

    private String errorMessage;

    private T model;

    private boolean isEmpty = true;

    private boolean isSuccess = true;

    public void setErrorInfo(ResponseErrorCode errorInfo) {
        this.setErrorCode(errorInfo.getErrorCode());
        this.setErrorMessage(errorInfo.getErrorMsg());
    }

    public static <T> DokkanResponseBuilder<T> builder() {
        return new DokkanResponseBuilder<>();
    }

    @Getter
    @Setter
    public static class DokkanResponseBuilder<T> {

        private DokkanResponse<T> response = new DokkanResponse<>();

        public DokkanResponse<T> success() {
            this.response.setErrorInfo(ResponseErrorCode.SUCCESS);
            return this.response;
        }

        public DokkanResponseBuilder<T> withModel(T model) {
            this.response.setModel(model);
            this.response.setEmpty(ObjectUtils.isEmpty(model));
            return this;
        }

        public DokkanResponse<T> fail() {
            this.response.setErrorInfo(ResponseErrorCode.FAILED_DEFAULT);
            this.response.setSuccess(false);
            return this.response;
        }

        public DokkanResponse<T> fail(ResponseErrorCode responseErrorCode) {
            this.response.setErrorInfo(responseErrorCode);
            this.response.setSuccess(false);
            return this.response;
        }

        public DokkanResponse<T> fail(String code, String message) {
            this.response.setErrorCode(code);
            this.response.setErrorMessage(message);
            this.response.setSuccess(false);
            return this.response;
        }

    }
}
