package org.ccclll777.alldocsbackend.enums;

import org.springframework.http.HttpStatus;

public enum FileStateEnum {
    /**
     * 建立索引时的等待状态，默认都是等待状态
     */
    WAITE(1,"建立索引时的等待状态"),
    /**
     * 进行中的状态
     */
    ON_PROCESS(2," 进行中的状态"),
    /**
     * 成功状态
     */
    SUCCESS(3,"成功状态"),
    /**
     * 失败状态
     */
    FAIL(4,"失败状态"),
    DELETE(5,"被删除状态");
    private final int code;


    private final String message;

    FileStateEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
