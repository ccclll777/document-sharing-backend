package org.ccclll777.alldocsbackend.controller;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ccclll777.alldocsbackend.entity.User;
import org.ccclll777.alldocsbackend.entity.dto.UserRegisterDTO;
import org.ccclll777.alldocsbackend.entity.dto.UserUpdateDTO;
import org.ccclll777.alldocsbackend.enums.ErrorCode;
import org.ccclll777.alldocsbackend.service.UserService;
import org.ccclll777.alldocsbackend.utils.BaseApiResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * @author shuang.kou
 */
@RestController
@Slf4j
@RequestMapping("/user")
@Api(tags = "用户")
public class UserController {
    @Autowired
    private  UserService userService;
    @PostMapping("/sign-up")
    @ApiOperation("用户注册")
    public BaseApiResult signUp(@RequestBody UserRegisterDTO userRegisterDTO) {
        userService.save(userRegisterDTO);
        return BaseApiResult.success("注册成功");
    }
    @GetMapping(value = "/getUsers")
    @PreAuthorize("hasAnyRole('ROLE_MANAGER','ROLE_ADMIN')")
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
    @PreAuthorize("hasAnyRole('ROLE_MANAGER','ROLE_ADMIN','ROLE_USER')")
    @GetMapping(value = "/getUserById")
    public BaseApiResult getById(@RequestParam(value = "userId") int userId) {
        User user = userService.selectUserById(userId);
        return BaseApiResult.success(user);
    }



    @ApiOperation(value = "根据用户名称查询", notes = "根据用户名称查询")
    @PreAuthorize("hasAnyRole('ROLE_MANAGER','ROLE_ADMIN','ROLE_USER')")
    @GetMapping(value = "/getByUsername")
    public BaseApiResult getByUsername(@RequestParam(value = "userName") String userName) {
        User user = userService.selectUserByUserName(userName);
        return BaseApiResult.success(user);
    }

    @ApiOperation(value = "更新用户信息", notes = "更新用户信息")
    @PreAuthorize("hasAnyRole('ROLE_MANAGER','ROLE_ADMIN','ROLE_USER')")
    @PostMapping(value = "/updateUser")
    public BaseApiResult updateUserInfo(@RequestBody UserUpdateDTO userUpdateDTO) {
        log.info("更新用户入参==={}", userUpdateDTO.toString());
        User user = User.builder()
                .id(userUpdateDTO.getId()).userName(userUpdateDTO.getUserName())
                .nickName(userUpdateDTO.getNickName()).email(userUpdateDTO.getEmail())
                .phone(userUpdateDTO.getPhone()).build();
        return userService.updateUser(user);
    }
    @ApiOperation(value = "根据id删除用户", notes = "根据id删除用户")
    @PreAuthorize("hasAnyRole('ROLE_MANAGER','ROLE_ADMIN')")
    @DeleteMapping(value = "/deleteByUserId/{userId}")
    public BaseApiResult deleteById( @PathVariable Integer userId) {
        int row = userService.deleteUser(userId);
        if (row > 0) {
            return  BaseApiResult.success("删除成功");
        }
        return BaseApiResult.error(ErrorCode.DELETE_FAILE.getCode(),ErrorCode.DELETE_FAILE.getMessage());
    }
}
