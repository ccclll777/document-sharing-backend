package org.ccclll777.alldocsbackend.service;

import com.google.common.collect.ImmutableMap;
import io.swagger.models.auth.In;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ccclll777.alldocsbackend.dao.RoleDao;
import org.ccclll777.alldocsbackend.dao.RoleUserDao;
import org.ccclll777.alldocsbackend.dao.UserDao;
import org.ccclll777.alldocsbackend.entity.Role;
import org.ccclll777.alldocsbackend.entity.User;
import org.ccclll777.alldocsbackend.enums.RoleType;
import org.ccclll777.alldocsbackend.exception.RoleNotFoundException;
import org.ccclll777.alldocsbackend.exception.UserNameAlreadyExistException;
import org.ccclll777.alldocsbackend.utils.UserRegisterRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author shuang.kou
 */
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class UserService {
    public static final String USERNAME = "username:";
    @Resource
    private final UserDao userDao;
    @Resource
    private final RoleDao roleDao;
    @Resource
    private final RoleUserDao roleUserDao;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    @Transactional(rollbackFor = Exception.class)
    public void save(UserRegisterRequest userRegisterRequest) {
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
    public List<User> selectUserList(int pageNum, int pageSize) {
        int offset = pageNum * pageSize;
        return userDao.selectUserList(pageSize,offset);
    }

    public User selectUserByid(int userId) {
        return userDao.selectUserById(userId);
    }
    public User selectUserByUserName(String userName) {
        return userDao.selectUserByUserName(userName);
    }
    public void updateUser(User user){
            userDao.updateUser(user);
    }
    public void deleteUser(Integer userId){
        userDao.deleteUser(userId);
    }
}
