package org.example.globalsourcing.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.example.globalsourcing.service.UserService
import org.example.globalsourcing.util.ResponseData
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint
import javax.servlet.http.HttpServletResponse

/**
 * Spring Security 配置类。
 */
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
class SecurityConfiguration(
    private val passwordEncoder: PasswordEncoder,
    private val userService: UserService,
    private val jacksonObjectMapper: ObjectMapper
) : WebSecurityConfigurerAdapter() {

    override fun configure(auth: AuthenticationManagerBuilder) {
        auth.userDetailsService(userService).passwordEncoder(passwordEncoder)
    }

    override fun configure(http: HttpSecurity) {
        http.authorizeRequests().anyRequest().authenticated()
            .and().formLogin().loginProcessingUrl("/login").permitAll()
            .usernameParameter("username").passwordParameter("password")
            .successHandler { _, response, authentication ->
                sendResponse(response, ResponseData.success(authentication.principal))
            }
            .failureHandler { _, response, _ ->
                sendResponse(response, ResponseData.error(101, "账号或密码错误，登录失败！"))
            }
            .and().logout().logoutUrl("/logout").permitAll()
            .logoutSuccessHandler { _, response, _ ->
                sendResponse(response, ResponseData.success(""))
            }
            .and().exceptionHandling()
            .authenticationEntryPoint(Http403ForbiddenEntryPoint())
            .and().cors()
            .and().csrf().disable()
    }

    private fun sendResponse(response: HttpServletResponse, data: ResponseData<*>) {
        response.setHeader("Content-Type", "application/json;charset=UTF-8")
        response.writer.use {
            it.print(jacksonObjectMapper.writeValueAsString(data))
            it.flush()
        }
    }
}