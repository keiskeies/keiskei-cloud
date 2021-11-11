package top.keiskeiframework.common.vo;

import lombok.Getter;
import top.keiskeiframework.common.enums.exception.ApiErrorCode;
import top.keiskeiframework.common.enums.exception.IErrorCode;

import java.io.Serializable;

/**
 * @author James Chen right_way@foxmail.com
 * @version 1.0
 * <p>
 * 数据统一返回结构
 * </p>
 * @since 2020/11/22 21:19
 */
public class R<T> implements Serializable {
    private static final long serialVersionUID = 4464081726731277091L;

    @Getter
    private long code;

    @Getter
    private T data;

    @Getter
    private String msg;


    public static <T> R<T> ok() {
        return restResult(null, ApiErrorCode.SUCCESS);
    }

    public static <T> R<T> ok(T data) {
        return restResult(data, ApiErrorCode.SUCCESS);
    }

    public static <T> R<T> ok(T data, String message) {
        return restResult(data, ApiErrorCode.SUCCESS.getCode(), message);
    }

    public static <T> R<T> failed(String msg) {
        return restResult(null, ApiErrorCode.FAILED.getCode(), msg);
    }

    public static <T> R<T> failed(IErrorCode errorCode) {
        return restResult(null, errorCode);
    }

    public static <T> R<T> restResult(T data, IErrorCode errorCode) {
        return restResult(data, errorCode.getCode(), errorCode.getMsg());
    }

    private static <T> R<T> restResult(T data, long code, String msg) {
        R<T> apiResult = new R<>();
        apiResult.code = code;
        apiResult.data = data;
        apiResult.msg = msg;
        return apiResult;
    }

}
