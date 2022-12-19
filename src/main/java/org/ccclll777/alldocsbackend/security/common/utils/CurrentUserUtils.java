package org.ccclll777.alldocsbackend.security.common.utils;


import lombok.RequiredArgsConstructor;
import org.ccclll777.alldocsbackend.entity.User;
import org.ccclll777.alldocsbackend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * @description 获取当前请求的用户
 */
@Component
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class CurrentUserUtils {

    private final UserService userService;

    public User getCurrentUser() {
        return userService.find(getCurrentUserName());
    }

    /**
     * 当认证成功的用户访问系统的时候，它的认证信息会被设置在 Spring Security 全局中。
     * 我们在其他地方获取到当前登录用户的授权信息也就很简单了，通过SecurityContextHolder.getContext().getAuthentication();方法即可。
     * @return
     */
    private  String getCurrentUserName() {
        //获取正在访问的用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() != null) {
            return (String) authentication.getPrincipal();
        }
        return null;
    }
}
