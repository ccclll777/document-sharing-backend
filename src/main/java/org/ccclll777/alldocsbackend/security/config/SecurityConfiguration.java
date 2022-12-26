package org.ccclll777.alldocsbackend.security.config;
//
//import org.ccclll777.alldocsbackend.security.common.constants.SecurityConstants;
//import org.ccclll777.alldocsbackend.security.exception.JwtAccessDeniedHandler;
//import org.ccclll777.alldocsbackend.security.exception.JwtAuthenticationEntryPoint;
//import org.ccclll777.alldocsbackend.security.filter.JwtAuthorizationFilter;
//import org.springframework.context.annotation.Bean;
//import org.springframework.data.redis.core.StringRedisTemplate;
//import org.springframework.http.HttpMethod;
//import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
//import org.springframework.security.config.http.SessionCreationPolicy;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
//import org.springframework.web.cors.CorsConfiguration;
//import org.springframework.web.cors.CorsConfigurationSource;
//import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
//
//import java.util.Arrays;
//
//import static java.util.Collections.singletonList;
//import static org.springframework.security.config.Customizer.withDefaults;
//
///**
// * 继承了 WebSecurityConfigurerAdapter。
// * WebSecurityConfigurerAdapter提供HttpSecurity来配置 cors，csrf，会话管理和受保护资源的规则。
// **/
//@EnableWebSecurity
//@EnableGlobalMethodSecurity(prePostEnabled = true)
//public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
//
//    private final StringRedisTemplate stringRedisTemplate;
//
//    public SecurityConfiguration(StringRedisTemplate stringRedisTemplate) {
//        this.stringRedisTemplate = stringRedisTemplate;
//    }
//
//    /**
//     * 密码编码器
//     * 存入数据库的密码需要被加密
//     */
//    @Bean
//    public BCryptPasswordEncoder bCryptPasswordEncoder() {
//        return new BCryptPasswordEncoder();
//    }
//
//
//    @Override
//    protected void configure(HttpSecurity http) throws Exception {
//        http
//                // 禁用 CSRF
//                .csrf().disable()
//                .authorizeRequests()
//                // 指定的接口直接放行
//                // swagger
//                .antMatchers(SecurityConstants.SWAGGER_WHITELIST).permitAll()
//                .antMatchers(HttpMethod.POST, SecurityConstants.SYSTEM_WHITELIST).permitAll() //指定的接口和方法可以放行
//                // 其他的接口都需要认证后才能请求
////                .anyRequest().authenticated() //其他的接口需要认证后才能请求
//                .and()
//                //添加自定义Filter
//                .addFilter(new JwtAuthorizationFilter(authenticationManager(), stringRedisTemplate))
//                // 不需要session（不创建会话）
//                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
//                // 授权异常处理
//                .exceptionHandling().authenticationEntryPoint(new JwtAuthenticationEntryPoint())
//                .accessDeniedHandler(new JwtAccessDeniedHandler()).and()
//                //设置跨域, 如果不设置, 即使配置了filter, 也不会生效
//                .cors();
//        // 防止H2 web 页面的Frame 被拦截
//        http.headers().frameOptions().disable();
//    }
//
//    /**
//     * Cors配置优化
//     * 跨域：如果没有设置exposedHeaders("Authorization")暴露 header 中的"Authorization"属性给客户端应用程序的话，前端是获取不到 token 信息的。
//     **/
//    @Bean
//    CorsConfigurationSource corsConfigurationSource() {
//        CorsConfiguration configuration = new CorsConfiguration();
//        configuration.setAllowedOrigins(singletonList("*"));
//        // configuration.setAllowedOriginPatterns(singletonList("*"));
//        configuration.setAllowedHeaders(singletonList("*"));
//        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "DELETE", "PUT", "OPTIONS"));
//        configuration.setExposedHeaders(singletonList(SecurityConstants.TOKEN_HEADER));
//        configuration.setAllowCredentials(true);
//        configuration.setMaxAge(3600L);
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", configuration);
//        return source;
//    }
//}

import org.ccclll777.alldocsbackend.security.common.constants.SecurityConstants;
import org.ccclll777.alldocsbackend.security.exception.JwtAccessDeniedHandler;
import org.ccclll777.alldocsbackend.security.exception.JwtAuthenticationEntryPoint;
import org.ccclll777.alldocsbackend.security.filter.JwtAuthorizationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

import static java.util.Collections.singletonList;
import static org.springframework.security.config.Customizer.withDefaults;

/**
 * @author shuang.kou Saving
 * @version 1.1
 * @date 2020.11.28 14:16
 * @description Spring Security配置类
 **/
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    private final StringRedisTemplate stringRedisTemplate;

    public SecurityConfiguration(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    /**
     * 密码编码器
     * 存入数据库的密码需要被加密
     */
    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }


    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.cors(withDefaults())
                // 禁用 CSRF
                .csrf().disable()
                .authorizeRequests()
                // 指定的接口直接放行
                // swagger
                .antMatchers(SecurityConstants.SWAGGER_WHITELIST).permitAll()
//                .antMatchers(SecurityConstants.H2_CONSOLE).permitAll()
                .antMatchers(SecurityConstants.SYSTEM_WHITELIST).permitAll()
                // 其他的接口都需要认证后才能请求
                .anyRequest().authenticated()
                .and()
                //添加自定义Filter
                .addFilter(new JwtAuthorizationFilter(authenticationManager(), stringRedisTemplate))
                // 不需要session（不创建会话）
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                // 授权异常处理
                .exceptionHandling().authenticationEntryPoint(new JwtAuthenticationEntryPoint())
                .accessDeniedHandler(new JwtAccessDeniedHandler());
        // 防止H2 web 页面的Frame 被拦截
        http.headers().frameOptions().disable();
    }

    /**
     * Cors配置优化
     **/
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        org.springframework.web.cors.CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(singletonList("*"));
        // configuration.setAllowedOriginPatterns(singletonList("*"));
        configuration.setAllowedHeaders(singletonList("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "DELETE", "PUT", "OPTIONS"));
        configuration.setExposedHeaders(singletonList(SecurityConstants.TOKEN_HEADER));
        configuration.setAllowCredentials(false);
        configuration.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

}
