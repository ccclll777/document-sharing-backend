package org.ccclll777.alldocsbackend.service;

import com.google.common.collect.ImmutableMap;
import lombok.extern.slf4j.Slf4j;
import org.ccclll777.alldocsbackend.dao.RoleDao;
import org.ccclll777.alldocsbackend.dao.RoleUserDao;
import org.ccclll777.alldocsbackend.dao.UserDao;
import org.ccclll777.alldocsbackend.entity.Role;
import org.ccclll777.alldocsbackend.entity.User;
import org.ccclll777.alldocsbackend.entity.dto.UserRegisterDTO;
import org.ccclll777.alldocsbackend.entity.vo.UserInfoVO;
import org.ccclll777.alldocsbackend.enums.RoleType;
import org.ccclll777.alldocsbackend.exception.RoleNotFoundException;
import org.ccclll777.alldocsbackend.exception.UserNameAlreadyExistException;
import org.ccclll777.alldocsbackend.security.service.AuthUserService;
import org.ccclll777.alldocsbackend.utils.BaseApiResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * @author shuang.kou
 */
@Service
@Slf4j
public class UserService {
    public static final String USERNAME = "username:";
    @Autowired
    private  UserDao userDao;
    @Autowired
    private  RoleDao roleDao;
    @Autowired
    private AuthUserService authUserService;
    @Autowired
    private  RoleUserDao roleUserDao;
    @Autowired
    private  BCryptPasswordEncoder bCryptPasswordEncoder;
    @Transactional(rollbackFor = Exception.class)
    public void save(UserRegisterDTO userRegisterRequest) {
        ensureUserNameNotExist(userRegisterRequest.getUserName());
        User user = userRegisterRequest.toUser();
        user.setPassword(bCryptPasswordEncoder.encode(userRegisterRequest.getPassword()));
        userDao.insertUser(user);
        //给用户绑定用户角色
        Role userRole = roleDao.selectRoleByName(RoleType.USER.getRoleName());
        if (userRole == null ) {
            log.error("RoleNotFoundException:{}",RoleType.USER.getRoleName());
            throw new RoleNotFoundException(ImmutableMap.of("roleName", RoleType.USER.getRoleName()));
        }
//        Role managerRole = roleDao.selectRoleByName(RoleType.MANAGER.getRoleName());
//        if ( managerRole == null) {
//            log.error("RoleNotFoundException:{}",RoleType.USER.getRoleName());
//            throw new RoleNotFoundException(ImmutableMap.of("roleName", RoleType.MANAGER.getRoleName()));
//        }
        //注册成功后 需要获取到用户ID，然后添加权限
        User newUser = userDao.selectUserByUserName(user.getUserName());
        roleUserDao.insertRoleUser(newUser.getId(),userRole.getId());
//        roleUserDao.insertRoleUser(newUser.getId(),managerRole.getId());
    }
    @Transactional(rollbackFor = Exception.class)
    public void saveUser(User user) {
        ensureUserNameNotExist(user.getUserName());
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        userDao.insertUser(user);
        //给用户绑定用户角色
        Role userRole = roleDao.selectRoleByName(RoleType.USER.getRoleName());
        if (userRole == null ) {
            log.error("RoleNotFoundException:{}",RoleType.USER.getRoleName());
            throw new RoleNotFoundException(ImmutableMap.of("roleName", RoleType.USER.getRoleName()));
        }
        Role managerRole = roleDao.selectRoleByName(RoleType.MANAGER.getRoleName());
        if ( managerRole == null) {
            log.error("RoleNotFoundException:{}",RoleType.USER.getRoleName());
            throw new RoleNotFoundException(ImmutableMap.of("roleName", RoleType.MANAGER.getRoleName()));
        }
        //注册成功后 需要获取到用户ID，然后添加权限
        User newUser = userDao.selectUserByUserName(user.getUserName());
        roleUserDao.insertRoleUser(newUser.getId(),userRole.getId());
        roleUserDao.insertRoleUser(newUser.getId(),managerRole.getId());
    }

    public User find(String userName) {
        return userDao.selectUserByUserName(userName);
    }
    public boolean check(String currentPassword, String password) {
        return this.bCryptPasswordEncoder.matches(currentPassword, password);
    }


    private void ensureUserNameNotExist(String userName) {
        User user = userDao.selectUserByUserName(userName);
        if (user != null) {
            log.error("UserNameAlreadyExistException.");
            throw new UserNameAlreadyExistException(ImmutableMap.of(USERNAME, userName));
        }
    }
    public List<UserInfoVO> selectUserList(int pageNum, int pageSize) {
        int offset = pageNum * pageSize;
        List<User> users= userDao.selectUserList(pageSize,offset);
        List<UserInfoVO> userInfoVOS = new ArrayList<>();
        for (User user : users) {
            int role = authUserService.getUserRole(user.getId());
            UserInfoVO userInfoVO = UserInfoVO.builder().id(user.getId()).userName(user.getUserName())
                    .email(user.getEmail()).nickName(user.getNickName())
                    .phone(user.getPhone()).role(role).status(user.getStatus())
                    .updateTime(user.getUpdateTime()).createTime(user.getCreateTime())
                    .build();
            userInfoVOS.add(userInfoVO);
        }
        return userInfoVOS;
    }

    public Integer getUserCount() {
        return userDao.userCount();
    }

    public User selectUserById(int userId) {
        return userDao.selectUserById(userId);
    }
    public User selectUserByUserName(String userName) {
        return userDao.selectUserByUserName(userName);
    }

    static final Map<String, String> fieldRegx = new HashMap<>(8);

    static {
        // 1-64个数字字母下划线
        fieldRegx.put("password", "^[0-9a-z_]{1,64}$");
        fieldRegx.put("phone", "/^1(3\\d|4[5-9]|5[0-35-9]|6[567]|7[0-8]|8\\d|9[0-35-9])\\d{8}$/");
        fieldRegx.put("mail", "^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$");
        // 1-140个任意字符
        fieldRegx.put("description", "(.*){1,140}");
    }
    public BaseApiResult updateUser(User user) {
            userDao.updateUser(user);
        return BaseApiResult.success("更新成功");
    }
    @Transactional(rollbackFor=Exception.class)
    public BaseApiResult updateUserRole(Integer userId, Integer roleId) {
        roleUserDao.deleteRoleUser(userId);
        roleUserDao.insertRoleUser(userId,roleId);
        return BaseApiResult.success("更新成功");
    }
    public int deleteUser(Integer userId){
        return userDao.deleteUser(userId);
    }
}
