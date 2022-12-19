package org.ccclll777.alldocsbackend.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ccclll777.alldocsbackend.entity.User;
import org.ccclll777.alldocsbackend.validator.FullName;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRegisterRequest {
    @NotBlank
    private String userName;
    @NotBlank
    private String password;
    @FullName
    @NotBlank
    private String nickName;

    public User toUser() {
        return User.builder().nickName(this.getNickName())
                .userName(this.getUserName()).build();
    }
}
