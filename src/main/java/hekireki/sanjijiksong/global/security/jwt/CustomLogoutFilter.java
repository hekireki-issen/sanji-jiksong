package hekireki.sanjijiksong.global.security.jwt;

import hekireki.sanjijiksong.global.security.entity.TokenType;
import hekireki.sanjijiksong.global.security.repository.RefreshRepository;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.GenericFilterBean;

import java.io.IOException;

@Slf4j
public class CustomLogoutFilter extends GenericFilterBean {

    private final JwtUtil jwtUtil;
    private final RefreshRepository refreshRepository;

    public CustomLogoutFilter(JwtUtil jwtUtil, RefreshRepository refreshRepository) {

        this.jwtUtil = jwtUtil;
        this.refreshRepository = refreshRepository;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        doFilter((HttpServletRequest) request, (HttpServletResponse) response, chain);
    }

    private void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws IOException, ServletException {

        String requestUri = request.getRequestURI();
        log.info("requestUri : " + requestUri);
        if (!requestUri.matches("^\\/logout$")) {
            log.info("URI is not logout");
            filterChain.doFilter(request, response);
            return;
        }

        String requestMethod = request.getMethod();
        log.info("requestMethod : " + requestMethod);
        if (!requestMethod.equals("POST")) {

            filterChain.doFilter(request, response);
            return;
        }

        String refresh = null;
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {

            if (cookie.getName().equals(TokenType.REFRESH.getValue())) {

                refresh = cookie.getValue();
            }
        }

        if (refresh == null) {
            log.info("token is null");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            jwtUtil.isExpired(refresh);
        } catch (ExpiredJwtException e) {
            log.info("token is expired");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        String category = jwtUtil.getCategory(refresh);
        if (!category.equals(TokenType.REFRESH.getValue())) {

            log.info("token is not refresh");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        Boolean isExist = refreshRepository.existsByRefreshToken(refresh);
        if (!isExist) {
            log.info("token is not exist");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        refreshRepository.deleteByRefreshToken(refresh);

        Cookie cookie = new Cookie(TokenType.REFRESH.getValue(), null);
        cookie.setMaxAge(0);
        cookie.setPath("/");

        response.addCookie(cookie);
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
