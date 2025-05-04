package com.bigdataai.config;

import com.bigdataai.security.CustomUserDetailsService;
import com.bigdataai.security.JwtAuthenticationEntryPoint;
import com.bigdataai.security.JwtRequestFilter;
import com.bigdataai.security.JwtTokenUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration; // 新增导入
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@Primary
public class SecurityConfig {

    @Autowired
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Autowired
    private final CustomUserDetailsService customUserDetailsService;

    @Autowired
    public SecurityConfig(JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint, CustomUserDetailsService customUserDetailsService) {
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
        this.customUserDetailsService = customUserDetailsService;
    }

    /**
     * 配置 AuthenticationManager 使用自定义的 UserDetailsService 和密码编码器。
     * @param auth AuthenticationManagerBuilder
     * @throws Exception 配置异常
     */


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 配置 AuthenticationManager Bean。
     * @param authenticationConfiguration 认证配置
     * @return AuthenticationManager 实例
     * @throws Exception 配置异常
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * 创建 JwtRequestFilter Bean，并注入依赖。
     * @param jwtTokenUtil JWT 工具类
     * @param customUserDetailsService 自定义用户详情服务
     * @return JwtRequestFilter 实例
     */
    @Bean
    public JwtRequestFilter jwtRequestFilter(JwtTokenUtil jwtTokenUtil, CustomUserDetailsService customUserDetailsService) {
        return new JwtRequestFilter(customUserDetailsService, jwtTokenUtil);
    }

    /**
     * 配置安全过滤链。
     * @param http HttpSecurity 配置器
     * @param jwtRequestFilter JWT 请求过滤器
     * @return SecurityFilterChain 实例
     * @throws Exception 配置异常
     */
    /**
     * 配置安全过滤链。
     * @param http HttpSecurity 配置器
     * @param jwtRequestFilter JWT 请求过滤器
     * @param authenticationManager 认证管理器
     * @return SecurityFilterChain 实例
     * @throws Exception 配置异常
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtRequestFilter jwtRequestFilter, AuthenticationManager authenticationManager) throws Exception {
        // 配置 AuthenticationManager
        http.authenticationManager(authenticationManager);
        http
            .csrf().disable()
            .authorizeRequests()
                .antMatchers("/auth/**").permitAll() // 允许所有用户访问认证相关接口
                .anyRequest().authenticated()
            .and()
            .exceptionHandling().authenticationEntryPoint(jwtAuthenticationEntryPoint)
            .and()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        // 添加JWT过滤器
        http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}