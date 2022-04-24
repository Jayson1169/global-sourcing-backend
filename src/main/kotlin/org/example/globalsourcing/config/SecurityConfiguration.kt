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
import org.springframework.security.web.authentication.AuthenticationFailureHandler
import org.springframework.security.web.authentication.AuthenticationSuccessHandler
import org.springframework.security.web.authentication.Http403ForbiddenEntryPoint
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository
import javax.servlet.http.HttpServletResponse
import javax.sql.DataSource

/**
 * Spring Security 配置类。
 */
@Configuration
@EnableGlobalMethodSecurity(prePostEnabled = true)
class SecurityConfiguration(
    private val passwordEncoder: PasswordEncoder,
    private val userService: UserService,
    private val dataSource: DataSource,
    private val jacksonObjectMapper: ObjectMapper
) : WebSecurityConfigurerAdapter() {

    override fun configure(auth: AuthenticationManagerBuilder) {
        auth.userDetailsService(userService).passwordEncoder(passwordEncoder)
    }

    override fun configure(http: HttpSecurity) {
        http.authorizeRequests().anyRequest().authenticated()
            .and().formLogin().loginProcessingUrl("/login").permitAll()
            .usernameParameter("username").passwordParameter("password")
            .successHandler(loginSuccessHandler())
            .failureHandler(loginFailureHandler())
            .and().logout().logoutUrl("/logout").permitAll()
            .logoutSuccessHandler(logoutSuccessHandler())
            .and().exceptionHandling()
            .authenticationEntryPoint(Http403ForbiddenEntryPoint())
            .and().rememberMe().alwaysRemember(true)
            .userDetailsService(userService)
            .tokenRepository(persistentTokenRepository())
            .and().cors()
            .and().csrf().disable()
    }

    private fun persistentTokenRepository(): PersistentTokenRepository {
        return JdbcTokenRepositoryImpl().apply {
            setDataSource(this@SecurityConfiguration.dataSource)
            setCreateTableOnStartup(true)
        }
    }

    private fun logoutSuccessHandler(): LogoutSuccessHandler {
        return LogoutSuccessHandler { _, response, _ ->
            sendResponse(response, ResponseData.success(""))
        }
    }

    private fun loginFailureHandler(): AuthenticationFailureHandler {
        return AuthenticationFailureHandler { _, response, _ ->
            sendResponse(response, ResponseData.error(101, "账号或密码错误，登录失败！"))
        }
    }

    private fun loginSuccessHandler(): AuthenticationSuccessHandler {
        return AuthenticationSuccessHandler { _, response, authentication ->
            sendResponse(response, ResponseData.success(authentication.principal))
        }
    }

    private fun sendResponse(response: HttpServletResponse, data: ResponseData<*>) {
        response.setHeader("Content-type", "application/json;charset=UTF-8")
        response.writer.use { writer ->
            val jsonString = jacksonObjectMapper.writeValueAsString(data)
            writer.print(jsonString)
            writer.flush()
        }
    }
}