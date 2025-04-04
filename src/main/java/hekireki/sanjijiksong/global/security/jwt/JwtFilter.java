package hekireki.sanjijiksong.global.security.jwt;

import hekireki.sanjijiksong.domain.user.entity.Role;
import hekireki.sanjijiksong.domain.user.entity.User;
import hekireki.sanjijiksong.global.security.dto.CustomUserDetails;
import hekireki.sanjijiksong.global.security.entity.TokenType;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;

//OncePerRequestFilter : 스프링의 필터 체인에서 요청 당 한 번만 실행되도록 보장하는 베이스 클래스
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    //Auth seq : 1
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // 헤더에서 access키에 담긴 토큰을 꺼냄
        String accessToken = request.getHeader(TokenType.ACCESS.getValue());

        // 토큰이 없다면 다음 필터인 로그인 필터로 넘김
        if (accessToken == null) {
            System.out.println("login");
            filterChain.doFilter(request, response);
            return;
        }

        // 토큰 존재
        // 토큰 만료 여부 확인, 만료시 다음 필터로 넘기지 않음
        try {
            jwtUtil.isExpired(accessToken);
        } catch (ExpiredJwtException e) {

            //response body
            PrintWriter writer = response.getWriter();
            writer.print("access token expired");

            //response status code
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // 토큰 만료 X
        // 토큰이 access인지 확인 (발급시 페이로드에 명시)
        String category = jwtUtil.getCategory(accessToken);
        if (!category.equals(TokenType.ACCESS.getValue())) {

            //response body
            PrintWriter writer = response.getWriter();
            writer.print("invalid access token");

            //response status code
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // username, role 값을 획득
        String email = jwtUtil.getEmail(accessToken);
        Role role = Role.valueOf(jwtUtil.getRole(accessToken));

        User user = User.builder()
                .email(email)
                .role(role).build();
        CustomUserDetails customUserDetails = new CustomUserDetails(user);

        //Role을 붙이는 이유
        Authentication authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken);

        filterChain.doFilter(request, response);
    }
}
