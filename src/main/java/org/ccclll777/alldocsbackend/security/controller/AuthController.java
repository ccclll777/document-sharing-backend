package org.ccclll777.alldocsbackend.security.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ccclll777.alldocsbackend.enums.ErrorCode;
import org.ccclll777.alldocsbackend.security.dto.LoginRequest;
import org.ccclll777.alldocsbackend.security.service.AuthUserService;
import org.ccclll777.alldocsbackend.utils.BaseApiResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

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
            String[] list = authUserService.createToken(loginRequest);
            Map<String, String> result = new HashMap<>(8);
            result.put("token", list[1]);
            result.put("userId", list[0]);
            result.put("userName", loginRequest.getUserName());
            return BaseApiResult.success(result);
        }catch (BadCredentialsException e){
            return BaseApiResult.error(ErrorCode.OPERATE_FAILED.getCode(),e.getMessage());
        }
    }

    @PostMapping("/logout")
    @ApiOperation("退出")
    public BaseApiResult logout() {
        authUserService.removeToken();
        return BaseApiResult.success("退出登录成功");
    }
}
