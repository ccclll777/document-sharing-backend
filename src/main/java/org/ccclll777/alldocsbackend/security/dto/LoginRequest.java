package org.ccclll777.alldocsbackend.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;


/**
 * 用户登录请求DTO
 */
@Data
@Builder
@AllArgsConstructor
public class LoginRequest {
    private String username;
    private String password;
    private Boolean rememberMe;
}
