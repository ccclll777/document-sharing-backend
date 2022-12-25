package org.ccclll777.alldocsbackend.enums;

import io.swagger.models.auth.In;
import lombok.Getter;

/**
 * @author shuang.kou
 */

@Getter
public enum RoleType {
    USER(2,"USER", "用户"),
    TEMP_USER(4,"TEMP_USER", "临时用户"),
    MANAGER(1,"MANAGER", "管理者"),
    ADMIN(3,"ADMIN", "Admin");
    private final String roleName;
    private final String description;
    private final Integer code;

    RoleType(Integer code, String roleName, String description) {
        this.code = code;
        this.roleName = roleName;
        this.description = description;
    }
}
