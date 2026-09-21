package net.persgroep.maisonyvonne.infrastructure.config.security

import net.persgroep.maisonyvonne.application.domain.UserRole
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpStatus
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.HttpStatusEntryPoint
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler

@Configuration
@EnableWebSecurity
class SecurityConfig {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain = http
        .authorizeHttpRequests { requests ->
            requests
                .requestMatchers("/api/health", "/api/auth/login").permitAll()
                .requestMatchers("/api/admin/**").hasRole(UserRole.ADMIN.name)
                .requestMatchers("/api/family/**").hasRole(UserRole.FAMILY.name)
                .requestMatchers("/api/**").authenticated()
                // the public website and the app shell are static files
                .anyRequest().permitAll()
        }
        .formLogin { login ->
            login
                .loginProcessingUrl("/api/auth/login")
                .successHandler { _, response, _ -> response.status = HttpStatus.NO_CONTENT.value() }
                .failureHandler { _, response, _ -> response.status = HttpStatus.UNAUTHORIZED.value() }
        }
        .logout { logout ->
            logout
                .logoutUrl("/api/auth/logout")
                .logoutSuccessHandler(HttpStatusReturningLogoutSuccessHandler(HttpStatus.NO_CONTENT))
        }
        // API calls answer 401 instead of redirecting to a login page
        .exceptionHandling { it.authenticationEntryPoint(HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)) }
        .csrf { it.spa() }
        .build()

    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()
}
