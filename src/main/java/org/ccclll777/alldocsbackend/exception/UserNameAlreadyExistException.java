package org.ccclll777.alldocsbackend.exception;

import org.ccclll777.alldocsbackend.enums.ErrorCode;

import java.util.Map;

/**
 * @author shuang.kou
 */
public class UserNameAlreadyExistException extends BaseException {

    public UserNameAlreadyExistException(Map<String, Object> data) {
        super(ErrorCode.USER_NAME_ALREADY_EXIST, data);
    }
}
