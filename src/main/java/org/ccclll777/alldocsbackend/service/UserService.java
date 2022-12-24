package org.ccclll777.alldocsbackend.service;

import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ccclll777.alldocsbackend.dao.RoleDao;
import org.ccclll777.alldocsbackend.dao.RoleUserDao;
import org.ccclll777.alldocsbackend.dao.UserDao;
import org.ccclll777.alldocsbackend.entity.Role;
import org.ccclll777.alldocsbackend.entity.User;
import org.ccclll777.alldocsbackend.entity.dto.UserRegisterDTO;
import org.ccclll777.alldocsbackend.enums.ErrorCode;
import org.ccclll777.alldocsbackend.enums.RoleType;
import org.ccclll777.alldocsbackend.exception.RoleNotFoundException;
import org.ccclll777.alldocsbackend.exception.UserNameAlreadyExistException;
import org.ccclll777.alldocsbackend.utils.BaseApiResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

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
    private boolean patternMatch(String s, String regex) {
        return  Pattern.compile(regex).matcher(s).matches();
    }
    public BaseApiResult updateUser(User user){
        if (StringUtils.hasText(user.getPassword())) {
            if (!patternMatch(user.getPassword(), fieldRegx.get("password"))) {
                return BaseApiResult.error(ErrorCode.PARAMS_PROCESS_FAILD.getCode(), ErrorCode.PARAMS_PROCESS_FAILD.getMessage());
            }
        }
        if (StringUtils.hasText(user.getEmail())) {
            if (!patternMatch(user.getEmail(), fieldRegx.get("mail"))) {
                return BaseApiResult.error(ErrorCode.PARAMS_PROCESS_FAILD.getCode(), ErrorCode.PARAMS_PROCESS_FAILD.getMessage());
            }
        }

        if (StringUtils.hasText(user.getPhone())) {
            if (!patternMatch(user.getPhone(), fieldRegx.get("phone"))) {
                return BaseApiResult.error(ErrorCode.PARAMS_PROCESS_FAILD.getCode(), ErrorCode.PARAMS_PROCESS_FAILD.getMessage());
            }
        }
        if (StringUtils.hasText(user.getNickName())) {
            if (!patternMatch(user.getNickName(), fieldRegx.get("description"))) {
                return BaseApiResult.error(ErrorCode.PARAMS_PROCESS_FAILD.getCode(), ErrorCode.PARAMS_PROCESS_FAILD.getMessage());
            }
        }
            userDao.updateUser(user);
        return BaseApiResult.success("更新成功");
    }


    public int deleteUser(Integer userId){
        return userDao.deleteUser(userId);
    }
}
