package org.ccclll777.alldocsbackend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoleUser {
    private Integer userId;

    private Integer roleId;
}
