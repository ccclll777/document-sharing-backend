package org.ccclll777.alldocsbackend.controller;

import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
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
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Date;
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
    public BaseApiResult getAllUser(@RequestParam(value = "pageNum", defaultValue = "0") int pageNum,
                                    @RequestParam(value = "pageSize", defaultValue = "10") int pageSize) {
        List<User> users = userService.selectUserList(pageNum, pageSize);
        return BaseApiResult.success(users);
    }
    @PostMapping(value = "/insert")
    @PreAuthorize("hasAnyRole('ROLE_MANAGER','ROLE_ADMIN')")
    @ApiOperation(value = "新增单个用户", notes = "新增单个用户")
    public BaseApiResult insertObj(@RequestBody User user) {
        userService.saveUser(user);
        return BaseApiResult.success("新增成功");
    }

    @ApiOperation(value = "根据id查询")
    @PreAuthorize("hasAnyRole('ROLE_MANAGER','ROLE_ADMIN')")
    @GetMapping(value = "/getUserById")
    public BaseApiResult getById(@RequestParam(value = "userId") int userId) {
        User user = userService.selectUserByid(userId);
        return BaseApiResult.success(user);
    }

    @ApiOperation(value = "根据用户名称查询", notes = "根据用户名称查询")
    @PreAuthorize("hasAnyRole('ROLE_MANAGER','ROLE_ADMIN')")
    @GetMapping(value = "/getByUsername")
    public BaseApiResult getByUsername(@RequestParam(value = "userName") String userName) {
        User user = userService.selectUserByUserName(userName);
        return BaseApiResult.success(user);
    }

    @ApiOperation(value = "更新用户信息", notes = "更新用户信息")
    @PreAuthorize("hasAnyRole('ROLE_MANAGER','ROLE_ADMIN','ROLE_USER')")
    @PostMapping(value = "/updateUser")
    public BaseApiResult updateUser(@RequestBody User user) {
        log.info("更新用户入参==={}", user.toString());
        userService.updateUser(user);
        return BaseApiResult.success("更新成功！");
    }

//    @ApiOperation(value = "根据id删除用户", notes = "根据id删除用户")
//    @PreAuthorize("hasAnyRole('ROLE_MANAGER','ROLE_ADMIN')")
//    @DeleteMapping(value = "/auth/deleteByUserID")
//    public BaseApiResult deleteById(@RequestBody HttpServletRequest request) {
//        String userId = (String) request.getAttribute("id");
//
//        DeleteResult remove = template.remove(user, COLLECTION_NAME);
//        if (remove.getDeletedCount() > 0) {
//            log.warn("[删除警告]正在删除用户：{}", user);
//            return BaseApiResult.success("删除成功");
//        } else {
//            return BaseApiResult.error(1201, MessageConstant.OPERATE_FAILED);
//        }
//    }


}
