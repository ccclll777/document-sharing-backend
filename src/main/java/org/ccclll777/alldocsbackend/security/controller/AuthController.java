package org.ccclll777.alldocsbackend.security.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ccclll777.alldocsbackend.security.common.constants.SecurityConstants;
import org.ccclll777.alldocsbackend.security.dto.LoginRequest;
import org.ccclll777.alldocsbackend.security.service.AuthUserService;
import org.ccclll777.alldocsbackend.utils.BaseApiResult;
import org.ccclll777.alldocsbackend.utils.MessageConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/auth")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Api(tags = "认证")
public class AuthController {

    private final AuthUserService authUserService;

    @PostMapping("/login")
    @ApiOperation("登录")
    public BaseApiResult login(@RequestBody LoginRequest loginRequest) {
        try {
            String token = authUserService.createToken(loginRequest);
            Map<String, String> result = new HashMap<>(8);
            result.put("token", token);
            result.put("userId", loginRequest.getUsername());
            return BaseApiResult.success(result);
        }catch (BadCredentialsException e){
            return BaseApiResult.error(MessageConstant.PROCESS_ERROR_CODE,e.getMessage());
        }
    }

    @PostMapping("/logout")
    @ApiOperation("退出")
    public BaseApiResult logout() {
        authUserService.removeToken();
        return BaseApiResult.success("退出登录成功");
    }
}
