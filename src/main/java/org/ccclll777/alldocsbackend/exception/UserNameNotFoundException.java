package org.ccclll777.alldocsbackend.exception;

import org.ccclll777.alldocsbackend.enums.ErrorCode;

import java.util.Map;

/**
 * @author shuang.kou
 */
public class UserNameNotFoundException extends BaseException {
    public UserNameNotFoundException(Map<String, Object> data) {
        super(ErrorCode.USER_NAME_NOT_FOUND, data);
    }
}
