package hekireki.sanjijiksong.global.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import hekireki.sanjijiksong.global.security.entity.Refresh;
import hekireki.sanjijiksong.global.security.entity.TokenType;
import hekireki.sanjijiksong.global.security.dto.LoginRequest;
import hekireki.sanjijiksong.global.security.repository.RefreshRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;

@Slf4j
public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final RefreshRepository refreshRepository;

    public LoginFilter(AuthenticationManager authenticationManager1, JwtUtil jwtUtil, RefreshRepository refreshRepository) {
        this.authenticationManager = authenticationManager1;
        this.jwtUtil = jwtUtil;
        this.refreshRepository = refreshRepository;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
            log.info("--- login process ---");
        try {
            ObjectMapper mapper = new ObjectMapper();
            LoginRequest loginRequest = mapper.readValue(request.getInputStream(), LoginRequest.class);

            log.info("loginRequest : " + loginRequest);

            String email = loginRequest.email();
            if(!isValidEmail(email)){
                log.info("invalid email : " + email);
                throw new BadCredentialsException("Invalid email format");
            }

            String password = loginRequest.password();
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(email,password,null);

            log.info("authToken : " + authToken);

            return authenticationManager.authenticate(authToken);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult){
        log.info("login success");
        String email = authResult.getName();

        Collection<? extends GrantedAuthority> authorities = authResult.getAuthorities();
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        GrantedAuthority authority = iterator.next();

        String role = authority.getAuthority().replace("ROLE_", "");

        log.info("success role : " + role);

        String access = jwtUtil.createJwt(TokenType.ACCESS.getValue(), email, role, JwtUtil.ACCESS_TOKEN_EXPIRE_TIME);
        String refresh = jwtUtil.createJwt(TokenType.REFRESH.getValue(), email, role, JwtUtil.REFRESH_TOKEN_EXPIRE_TIME);

        log.info("access token : " + access);
        log.info("refresh token : " + refresh);

        addRefreshEntity(email,refresh,JwtUtil.REFRESH_TOKEN_EXPIRE_TIME);

        response.setHeader(TokenType.ACCESS.getValue(),access);
        response.addCookie(createCookie(TokenType.REFRESH.getValue(),refresh));
        response.setStatus(HttpStatus.OK.value());
        log.info("--- login complete ---");
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed){
        log.info("login fail");
        response.setStatus(401);
    }

    private void addRefreshEntity(String email, String refreshToken, Long expiredMs){
        Date date = new Date(System.currentTimeMillis() + expiredMs);

        Refresh refreshEntity = Refresh.builder()
                .email(email)
                .refreshToken(refreshToken)
                .expiration(date.toString())
                .build();

        refreshRepository.save(refreshEntity);
    }

    private Cookie createCookie(String key, String value){
        Cookie cookie = new Cookie(key,value);
        cookie.setMaxAge((int) (JwtUtil. REFRESH_TOKEN_EXPIRE_TIME / 1000));
        cookie.setHttpOnly(true);
        return cookie;
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }
}
