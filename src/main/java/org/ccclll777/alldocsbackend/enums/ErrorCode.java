package org.ccclll777.alldocsbackend.enums;

import lombok.Getter;
import org.springframework.http.HttpStatus;
@Getter
public enum ErrorCode {

    USER_NAME_ALREADY_EXIST(1001, HttpStatus.BAD_REQUEST, "用户名已经存在"),
    Role_NOT_FOUND(1002, HttpStatus.NOT_FOUND, "未找到指定角色"),
    USER_NAME_NOT_FOUND(1002, HttpStatus.NOT_FOUND, "未找到指定用户"),
    VERIFY_JWT_FAILED(1003, HttpStatus.UNAUTHORIZED, "token验证失败"),
    METHOD_ARGUMENT_NOT_VALID(1003, HttpStatus.BAD_REQUEST, "方法参数验证失败"),
    PARAMS_PROCESS_FAILD(1201,HttpStatus.BAD_REQUEST,"参数处理失败"),
    PARAMS_IS_NOT_NULL(1201,HttpStatus.BAD_REQUEST,"参数是必需的"),
    PARAMS_LENGTH_REQUIRED(1201,HttpStatus.BAD_REQUEST,"参数的长度必须符合要求"),
    PARAMS_FORMAT_ERROR(1201,HttpStatus.BAD_REQUEST,"参数格式错误"),
    PARAMS_TYPE_ERROR(1202,HttpStatus.INTERNAL_SERVER_ERROR,"类型转换错误"),
    OPERATE_FAILED(1202,HttpStatus.INTERNAL_SERVER_ERROR,"操作失败"),
    UPLOAD_FAILED(1202,HttpStatus.INTERNAL_SERVER_ERROR,"上传的文件超过大小限制");

    private final int code;

    private final HttpStatus status;

    private final String message;

    ErrorCode(int code, HttpStatus status, String message) {
        this.code = code;
        this.status = status;
        this.message = message;
    }
}
