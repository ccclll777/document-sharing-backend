package org.ccclll777.alldocsbackend.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.ccclll777.alldocsbackend.entity.User;

import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRegisterDTO {
    @NotBlank
    private String userName;
    @NotBlank
    private String password;

    public User toUser() {
        return User.builder()
                .userName(this.getUserName()).build();
    }
}
