package hekireki.sanjijiksong.global.security.controller;

import hekireki.sanjijiksong.global.security.entity.Refresh;
import hekireki.sanjijiksong.global.security.entity.TokenType;
import hekireki.sanjijiksong.global.security.jwt.JwtUtil;
import hekireki.sanjijiksong.global.security.repository.RefreshRepository;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

@RestController
public class ReissueController {

    private final JwtUtil jwtUtil;

    private final RefreshRepository refreshRepository;

    public ReissueController(JwtUtil jwtUtil, RefreshRepository refreshRepository) {
        this.jwtUtil = jwtUtil;
        this.refreshRepository = refreshRepository;
    }
    //Refresh seq : 1
    @PostMapping("/reissue")
    private ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response){
        String refresh = null;
        Cookie[] cookies = request.getCookies();
        for (Cookie cookie : cookies) {
            if(cookie.getName().equals(TokenType.REFRESH.getValue())){
                refresh = cookie.getValue();
            }
        }
        //쿠키에 토큰이 없는 경우
        if (refresh == null) {
            return new ResponseEntity<>("refresh token null", HttpStatus.BAD_REQUEST);
        }

        //토큰 만료 확인
        try {
            jwtUtil.isExpired(refresh);
        }catch (ExpiredJwtException e){
            return new ResponseEntity<>("access token expired", HttpStatus.BAD_REQUEST);
        }

        //refresh인지 확인
        String category = jwtUtil.getCategory(refresh);
        if(!category.equals(TokenType.REFRESH.getValue())){
            return new ResponseEntity<>("invalid refresh token", HttpStatus.BAD_REQUEST);
        }

        //실제 db에 있는지 확인
        Boolean isExist = refreshRepository.existsByRefreshToken(refresh);
        if(!isExist){
            return new ResponseEntity<>("invalid refresh token(DB)", HttpStatus.BAD_REQUEST);
        }

        String email = jwtUtil.getEmail(refresh);
        String role = jwtUtil.getRole(refresh);

        String newAccess = jwtUtil.createJwt(TokenType.ACCESS.getValue(), email, role, JwtUtil.ACCESS_TOKEN_EXPIRE_TIME);
        String newRefresh = jwtUtil.createJwt(TokenType.REFRESH.getValue(), email, role, JwtUtil.REFRESH_TOKEN_EXPIRE_TIME);

        refreshRepository.deleteByRefreshToken(refresh);
        addRefreshEntity(email, newRefresh, JwtUtil.REFRESH_TOKEN_EXPIRE_TIME);

        response.setHeader(TokenType.ACCESS.getValue(), newAccess);
        response.addCookie(createCookie(TokenType.REFRESH.getValue(), newRefresh));
        return new ResponseEntity<>(HttpStatus.OK);
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

    private Cookie createCookie(String key, String value) {
        Cookie cookie = new Cookie(key,value);
        cookie.setMaxAge((int) (JwtUtil.REFRESH_TOKEN_EXPIRE_TIME / 1000));
        cookie.setHttpOnly(true);
        return cookie;
    }
}
