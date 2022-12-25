package org.ccclll777.alldocsbackend.security.service;


import io.swagger.models.auth.In;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ccclll777.alldocsbackend.dao.RoleDao;
import org.ccclll777.alldocsbackend.dao.RoleUserDao;
import org.ccclll777.alldocsbackend.entity.Role;
import org.ccclll777.alldocsbackend.entity.RoleUser;
import org.ccclll777.alldocsbackend.entity.User;
import org.ccclll777.alldocsbackend.enums.RoleType;
import org.ccclll777.alldocsbackend.security.common.utils.CurrentUserUtils;
import org.ccclll777.alldocsbackend.security.common.utils.JwtTokenUtils;
import org.ccclll777.alldocsbackend.security.dto.LoginRequest;
import org.ccclll777.alldocsbackend.security.entity.JwtUser;
import org.ccclll777.alldocsbackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
@Slf4j
public class AuthUserService {
    private final UserService userService;

    private final StringRedisTemplate stringRedisTemplate;

    private final CurrentUserUtils currentUserUtils;

    private final RoleDao roleDao;
    private final RoleUserDao roleUserDao;

    public String[] createToken(LoginRequest loginRequest) {
        User user = userService.find(loginRequest.getUserName());
        if (!userService.check(loginRequest.getPassword(), user.getPassword())) {
            log.error("The user name or password is not correct.");
            throw new BadCredentialsException("The user name or password is not correct.");
        }
        List<Role> roles = roleDao.selectRoleByUserId(user.getId());
        user.setRoles(roles);
        JwtUser jwtUser = new JwtUser(user);
        if (!jwtUser.isEnabled()) {
            log.error("User is forbidden to login.");
            throw new BadCredentialsException("User is forbidden to login.");
        }
        List<String> authorities = jwtUser.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
        String token = JwtTokenUtils.createToken(user.getUserName(), user.getId().toString(), authorities, loginRequest.getRememberMe());
        stringRedisTemplate.opsForValue().set(user.getId().toString(), token);
        return new String[]{user.getId().toString(), token};
    }
    public Integer getUserRole(Integer userId) {
        List<Role> roles = roleDao.selectRoleByUserId(userId);
        if (isManager(roles)) {
            return RoleType.MANAGER.getCode();
        } else if (isAdmin(roles)) {
            return RoleType.ADMIN.getCode();
        } else if (isUser(roles)) {
            return RoleType.USER.getCode();
        } else {
            return RoleType.TEMP_USER.getCode();
        }

    }
    public boolean isManager(List<Role> roles) {
        for(Role role : roles ) {
            if (role.getRoleName().equals(RoleType.MANAGER.getRoleName())){
                return true;
            }
        }
        return false;
    }

    public boolean isAdmin(List<Role> roles) {
        for(Role role : roles ) {
            if (role.getRoleName().equals(RoleType.ADMIN.getRoleName())){
                return true;
            }
        }
        return false;
    }
    public User selectUserById(Integer userId) {
        return userService.selectUserById(userId);
    }

    public boolean isUser(List<Role> roles) {
        for(Role role : roles ) {
            if (role.getRoleName().equals(RoleType.USER.getRoleName())){
                return true;
            }
        }
        return false;
    }

    public void removeToken() {
        stringRedisTemplate.delete(currentUserUtils.getCurrentUser().getId().toString());
    }

}