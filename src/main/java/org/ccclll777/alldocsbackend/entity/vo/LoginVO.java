package org.ccclll777.alldocsbackend.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginVO {
    private Integer userId;
    private String userName;
    private String token;
//    private String email;
    private Integer role;
}
