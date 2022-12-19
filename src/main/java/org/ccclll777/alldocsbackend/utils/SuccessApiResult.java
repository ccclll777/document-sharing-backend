package org.ccclll777.alldocsbackend.utils;

import java.io.Serializable;

/**
 * 成功返回体.
 *
 */
public final class SuccessApiResult<T> extends BaseApiResult implements Serializable {

    private static final long serialVersionUID = 1L;

    public SuccessApiResult() {
        this(null);
    }

    public SuccessApiResult(T data) {
        this.timestamp = System.currentTimeMillis();
        this.code = 200;
        this.data = data;
    }

    private T data;

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }



}