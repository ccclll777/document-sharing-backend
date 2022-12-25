package org.ccclll777.alldocsbackend.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RoleUser {
    @Id
    private Integer id;
    @NotBlank
    private Integer userId;
    @NotBlank
    private Integer roleId;
}
