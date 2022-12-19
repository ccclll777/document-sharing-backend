package org.ccclll777.alldocsbackend.utils;

import java.io.Serializable;

/**
 * 错误返回
 *
 */
public final class ErrorApiResult extends BaseApiResult implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 错误信息
     */
    public String message;

    public ErrorApiResult(Integer code, String message) {
        this.timestamp = System.currentTimeMillis();
        this.code = code;
        this.message = message;
    }

}
