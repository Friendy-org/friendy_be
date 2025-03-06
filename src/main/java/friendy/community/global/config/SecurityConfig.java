package friendy.community.global.config;

import friendy.community.domain.auth.jwt.JwtTokenFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
@Configuration
public class SecurityConfig {

    private final JwtTokenFilter jwtTokenFilter;

    // Swagger 관련 URL들을 배열로 정의
    private final String[] SWAGGER = {
        "/v3/api-docs",
        "/swagger-resources/**",
        "/configuration/security",
        "/webjars/**",
        "/swagger-ui.html",
        "/swagger-ui/**",
        "/swagger/**"
    };

    public SecurityConfig(JwtTokenFilter jwtTokenFilter) {
        this.jwtTokenFilter = jwtTokenFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests((authz) -> authz
                .requestMatchers(SWAGGER).permitAll()  // Swagger URL을 예외 처리
                .anyRequest().authenticated()  // 나머지 요청은 인증 필요
            )
            .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class);  // JWT 필터 추가
        return http.build();
    }
}
    