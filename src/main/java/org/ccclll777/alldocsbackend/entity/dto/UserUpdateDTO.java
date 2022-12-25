package org.ccclll777.alldocsbackend.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ccclll777.alldocsbackend.entity.User;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateDTO {

    private Integer id;
    private String userName;
    private String nickName;
    private String phone;
    private String email;
}
