package hekireki.sanjijiksong.global.config;

import hekireki.sanjijiksong.domain.user.entity.Role;
import hekireki.sanjijiksong.global.security.jwt.JwtFilter;
import hekireki.sanjijiksong.global.security.jwt.JwtUtil;
import hekireki.sanjijiksong.global.security.jwt.LoginFilter;
import hekireki.sanjijiksong.global.security.repository.RefreshRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final AuthenticationConfiguration authenticationConfiguration;
    private final JwtUtil jwtUtil;
    private final RefreshRepository refreshRepository;

    public SecurityConfig(AuthenticationConfiguration authenticationConfiguration, JwtUtil jwtUtil, RefreshRepository refreshRepository) {
        this.authenticationConfiguration = authenticationConfiguration;
        this.jwtUtil = jwtUtil;
        this.refreshRepository = refreshRepository;
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder(){
        return new BCryptPasswordEncoder();//비밀번호 암호화
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception{
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception{
        http.csrf((auth) -> auth.disable());//jwt는 session을 stateless로 관리하기에 필요 없음
        http.formLogin((auth)->auth.disable());//rest 방식이기에 login form 필요없음
        http.httpBasic((auth) -> auth.disable());//시큐리티의 기본 인증인 HTTP Basic 인증 비활성화
        http.authorizeHttpRequests((auth) -> auth
                .requestMatchers("/login", "/", "/join").permitAll()//해당 url경로는 인증 필요 없음
//                        .requestMatchers("/admin").hasRole(Role.ADMIN.name())// ADMIN만 접근 가능
//                .requestMatchers().hasRole(Role.BUYER.name())//Buyer만 접근 가능
//                .requestMatchers().hasRole(Role.SELLER.name())//Seller만 접근 가능
                        .requestMatchers("/reissue").permitAll()
                        .anyRequest().authenticated()

        );
        http.sessionManagement((session) -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS));//jwt는 session을 stateless로 관리
        http.addFilterBefore(new JwtFilter(jwtUtil),LoginFilter.class);
        http.addFilterAt(new LoginFilter(authenticationManager(authenticationConfiguration), jwtUtil, refreshRepository), UsernamePasswordAuthenticationFilter.class);//로그인 필터 설정
        return http.build();
    }
}
