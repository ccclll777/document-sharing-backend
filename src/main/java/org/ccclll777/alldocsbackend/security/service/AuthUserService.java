package org.ccclll777.alldocsbackend.security.service;


import com.google.common.collect.ImmutableMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ccclll777.alldocsbackend.dao.RoleDao;
import org.ccclll777.alldocsbackend.dao.RoleUserDao;
import org.ccclll777.alldocsbackend.dao.UserDao;
import org.ccclll777.alldocsbackend.entity.Role;
import org.ccclll777.alldocsbackend.entity.User;
import org.ccclll777.alldocsbackend.security.common.utils.CurrentUserUtils;
import org.ccclll777.alldocsbackend.security.common.utils.JwtTokenUtils;
import org.ccclll777.alldocsbackend.security.dto.LoginRequest;
import org.ccclll777.alldocsbackend.security.entity.JwtUser;
import org.ccclll777.alldocsbackend.service.UserService;
import org.ccclll777.alldocsbackend.utils.UserRegisterRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author shuang.kou
 **/
@Service
@Slf4j
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class AuthUserService {

    private final UserService userService;
    private final StringRedisTemplate stringRedisTemplate;
    private final CurrentUserUtils currentUserUtils;
    private final RoleDao roleDao;

    public String createToken(LoginRequest loginRequest) {
        User user = userService.find(loginRequest.getUsername());
        System.out.println(loginRequest.getUsername());
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
        return token;
    }

    public void removeToken() {
        stringRedisTemplate.delete(currentUserUtils.getCurrentUser().getId().toString());
    }

}