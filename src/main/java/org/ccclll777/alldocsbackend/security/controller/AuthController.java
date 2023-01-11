package org.ccclll777.alldocsbackend.security.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.models.auth.In;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ccclll777.alldocsbackend.entity.User;
import org.ccclll777.alldocsbackend.entity.vo.LoginVO;
import org.ccclll777.alldocsbackend.entity.vo.UserInfoVO;
import org.ccclll777.alldocsbackend.enums.ErrorCode;
import org.ccclll777.alldocsbackend.security.common.constants.SecurityConstants;
import org.ccclll777.alldocsbackend.security.common.utils.JwtTokenUtils;
import org.ccclll777.alldocsbackend.security.dto.LoginRequest;
import org.ccclll777.alldocsbackend.security.service.AuthUserService;
import org.ccclll777.alldocsbackend.utils.BaseApiResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
            Integer userId = Integer.parseInt(list[0]);
            Integer roleId = authUserService.getUserRole(userId);
            LoginVO loginVO = LoginVO.builder().token(list[1]).userId(userId)
                    .userName(loginRequest.getUserName()).role(roleId).build();
//            Map<String, String> result = new HashMap<>(8);
//            result.put("token", list[1]);
//            result.put("userId", list[0]);
//            result.put("userName", loginRequest.getUserName());
            return BaseApiResult.success(loginVO);
        }catch (BadCredentialsException e){
            return BaseApiResult.error(ErrorCode.OPERATE_FAILED.getCode(),e.getMessage());
        }
    }
    @ApiOperation(value = "根据token获取用户信息")
    @PreAuthorize("hasAnyRole('ROLE_MANAGER','ROLE_ADMIN','ROLE_USER')")
    @GetMapping(value = "/info")
    public BaseApiResult getInfoByToken(@RequestHeader(SecurityConstants.TOKEN_HEADER) String token) {
        String tokenValue = token.replace(SecurityConstants.TOKEN_PREFIX, "");
        String userId =   JwtTokenUtils.getId(tokenValue);
        User user = authUserService.selectUserById(Integer.parseInt(userId));
        int role = authUserService.getUserRole(user.getId());
        UserInfoVO userInfoVO = UserInfoVO.builder().id(user.getId()).userName(user.getUserName())
                .email(user.getEmail()).nickName(user.getNickName())
                .phone(user.getPhone()).role(role).build();
        return BaseApiResult.success(userInfoVO);
    }

    @PostMapping("/logout")
    @ApiOperation("退出")
    public BaseApiResult logout() {
//        authUserService.removeToken();
        return BaseApiResult.success("退出登录成功");
    }
}
