package com.hb.dokkan.common.domain;

import com.hb.dokkan.common.constants.ResponseErrorCode;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.ObjectUtils;

import java.io.Serializable;

/**
 * @Description xxxxx
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

        private DokkanResponse<T> response;

        public DokkanResponseBuilder<T> success() {
            this.response.setErrorInfo(ResponseErrorCode.SUCCESS);
            return this;
        }

        public DokkanResponse<T> withModel(T model) {
            this.response.setModel(model);
            this.response.setEmpty(ObjectUtils.isEmpty(model));
            return this.response;
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

    }
}
