package org.ccclll777.alldocsbackend.enums;

import lombok.Getter;

/**
 * @author shuang.kou
 */

@Getter
public enum RoleType {
    USER("USER", "用户"),
    TEMP_USER("TEMP_USER", "临时用户"),
    MANAGER("SUPER_ADMIN", "管理者"),
    ADMIN("ADMIN", "Admin");
    private final String roleName;
    private final String description;

    RoleType(String roleName, String description) {
        this.roleName = roleName;
        this.description = description;
    }
}
