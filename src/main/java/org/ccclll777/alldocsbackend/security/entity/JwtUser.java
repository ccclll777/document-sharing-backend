package org.ccclll777.alldocsbackend.security.entity;

import org.ccclll777.alldocsbackend.entity.Role;
import org.ccclll777.alldocsbackend.entity.RoleUser;
import org.ccclll777.alldocsbackend.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author shuang.kou
 * @description JWT用户对象
 */
public class JwtUser implements UserDetails {

    private Integer id;
    private String username;
    private String password;
    private Integer status;
    private Collection<? extends GrantedAuthority> authorities;

    public JwtUser() {
    }

    /**
     * 通过 user 对象创建jwtUser
     */
    public JwtUser(User user) {
        id = user.getId();
        username = user.getUserName();
        password = user.getPassword();
        status = user.getStatus();
        List<Role> roles = user.getRoles();
        List<SimpleGrantedAuthority> auths = new ArrayList<>();
        roles.forEach(role -> auths.add(new SimpleGrantedAuthority("ROLE_" + role.getRoleName())));
        authorities = auths;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return status == 1;
    }

    @Override
    public String toString() {
        return "JwtUser{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", authorities=" + authorities +
                '}';
    }

}
