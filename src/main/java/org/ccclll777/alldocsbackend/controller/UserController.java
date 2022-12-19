package org.ccclll777.alldocsbackend.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ccclll777.alldocsbackend.entity.User;
import org.ccclll777.alldocsbackend.entity.vo.UserRepresentation;
import org.ccclll777.alldocsbackend.security.service.AuthUserService;
import org.ccclll777.alldocsbackend.service.UserService;
import org.ccclll777.alldocsbackend.utils.BaseApiResult;
import org.ccclll777.alldocsbackend.utils.UserRegisterRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * @author shuang.kou
 */
@RestController
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@RequestMapping("/users")
@Api(tags = "用户")
public class UserController {
    private final UserService userService;
    @PostMapping("/sign-up")
    @ApiOperation("用户注册")
    public BaseApiResult signUp(@RequestBody @Valid UserRegisterRequest userRegisterRequest) {
        userService.save(userRegisterRequest);
        return BaseApiResult.success("注册成功");
    }
    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_USER','ROLE_MANAGER','ROLE_ADMIN')")
    @ApiOperation("获取所有用户的信息（分页）")
    public BaseApiResult getAllUser(@RequestParam(value = "pageNum", defaultValue = "0") int pageNum, @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        List<User> users = userService.selectUserList(pageNum, pageSize);
        return BaseApiResult.success(users);
    }

//    @PutMapping
//    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
//    @ApiOperation("更新用户")
//    public BaseApiResult update(@RequestBody @Valid UserUpdateRequest userUpdateRequest) {
//        userService.update(userUpdateRequest);
//        return BaseApiResult.success("更新成功");
//    }
//
//    @DeleteMapping
//    @PreAuthorize("hasAnyRole('ROLE_ADMIN')")
//    @ApiOperation("根据用户名删除用户")
//    public ResponseEntity<Void> deleteUserByUserName(@RequestParam("username") String username) {
//        userService.delete(username);
//        return ResponseEntity.ok().build();
//    }
}
