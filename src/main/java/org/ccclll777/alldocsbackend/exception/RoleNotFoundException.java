package org.ccclll777.alldocsbackend.exception;

import org.ccclll777.alldocsbackend.enums.ErrorCode;

import java.util.Map;

/**
 * @author shuang.kou
 */
public class RoleNotFoundException extends BaseException {
    public RoleNotFoundException(Map<String, Object> data) {
        super(ErrorCode.Role_NOT_FOUND, data);
    }
}
