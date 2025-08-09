package com.genersoft.iot.vmp.test.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Arrays;
import java.util.Collection;

/**
 * 测试安全配置
 * 提供测试环境下的安全认证Mock实现
 * 
 * @author Claude
 */
@TestConfiguration
@Profile("test") 
@EnableWebSecurity
public class TestSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable()
            .authorizeRequests()
            .anyRequest().permitAll()
            .and()
            .httpBasic().disable()
            .formLogin().disable();
    }

    @Bean
    @Primary
    public AuthenticationManager testAuthenticationManager() {
        return new AuthenticationManager() {
            @Override
            public Authentication authenticate(Authentication authentication) throws AuthenticationException {
                // 测试环境下总是认证成功
                String username = authentication.getName();
                Collection<GrantedAuthority> authorities = Arrays.asList(
                    new SimpleGrantedAuthority("ROLE_USER"),
                    new SimpleGrantedAuthority("ROLE_ADMIN")
                );
                
                return new UsernamePasswordAuthenticationToken(username, null, authorities);
            }
        };
    }
    
    /**
     * 设置测试用户上下文
     * 在测试方法中调用此方法可以模拟用户登录状态
     */
    public static void setTestUserContext(String username, String... roles) {
        Collection<GrantedAuthority> authorities = Arrays.stream(roles)
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()))
            .collect(java.util.stream.Collectors.toList());
            
        UserDetails userDetails = User.builder()
            .username(username)
            .password("test")
            .authorities(authorities)
            .build();
            
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            userDetails, null, authorities);
        
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
    }
    
    /**
     * 清除测试用户上下文
     */
    public static void clearTestUserContext() {
        SecurityContextHolder.clearContext();
    }
    
    /**
     * 设置管理员用户上下文
     */
    public static void setAdminUserContext() {
        setTestUserContext("test-admin", "ADMIN", "USER");
    }
    
    /**
     * 设置普通用户上下文
     */
    public static void setRegularUserContext() {
        setTestUserContext("test-user", "USER");
    }
}