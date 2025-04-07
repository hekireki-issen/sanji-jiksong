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


public class LoginFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final RefreshRepository refreshRepository;

    public LoginFilter(AuthenticationManager authenticationManager1, JwtUtil jwtUtil, RefreshRepository refreshRepository) {
        this.authenticationManager = authenticationManager1;
        this.jwtUtil = jwtUtil;
        this.refreshRepository = refreshRepository;
    }

    //Login seq : 1
    @Override//자동호출
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

        try {
            ObjectMapper mapper = new ObjectMapper();
            LoginRequest loginRequest = mapper.readValue(request.getInputStream(), LoginRequest.class);

            //UsernamePasswordAuthenticationFilter의 내장 함수
            String email = loginRequest.email();
            if(!isValidEmail(email)){
                throw new BadCredentialsException("Invalid email format");
            }

            String password = loginRequest.password();
            //Dto 역할
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(email,password,null);
            return authenticationManager.authenticate(authToken);//자동으로 검증 수행
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    //Login seq : 3
    @Override//성공시 자동 실행
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult){
        String email = authResult.getName();

        //private final Collection<GrantedAuthority> authorities;
        //[SimpleGrantedAuthority("ROLE_USER"), SimpleGrantedAuthority("ROLE_ADMIN")]
        Collection<? extends GrantedAuthority> authorities = authResult.getAuthorities();
        //Iterator객체
        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
        //iterator의 첫번쨰 값
        GrantedAuthority authority = iterator.next();

        //SimpleGrantedAuthority("ROLE_USER")
        String role = authority.getAuthority().replace("ROLE_", ""); // "ADMIN"

        //access 토큰
        String access = jwtUtil.createJwt(TokenType.ACCESS.getValue(), email, role, JwtUtil.ACCESS_TOKEN_EXPIRE_TIME);
        //refresh 토큰
        String refresh = jwtUtil.createJwt(TokenType.REFRESH.getValue(), email, role, JwtUtil.REFRESH_TOKEN_EXPIRE_TIME);

        addRefreshEntity(email,refresh,JwtUtil.REFRESH_TOKEN_EXPIRE_TIME);

        response.setHeader(TokenType.ACCESS.getValue(),access);
        //xss 공격 대응 : JavaScript에서 쿠키를 읽을 수 없으므로, 악성 스크립트로 refresh token 탈취 어려움
        response.addCookie(createCookie(TokenType.REFRESH.getValue(),refresh));
        response.setStatus(HttpStatus.OK.value());
    }

    @Override//실패시 자동 실행
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed){
        //401 : 요청된 리소스에 대한 유효한 인증 자격 증명이 없어 발생하는 오류
        response.setStatus(401);
    }

    private void addRefreshEntity(String email, String refreshToken, Long expiredMs){
        //만료시간 설정
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
