package hekireki.sanjijiksong.global.security.service;

import hekireki.sanjijiksong.global.common.exception.ErrorCode;
import hekireki.sanjijiksong.global.common.exception.SecurityException;
import hekireki.sanjijiksong.global.security.dto.ReissueResponse;
import hekireki.sanjijiksong.global.security.entity.Refresh;
import hekireki.sanjijiksong.global.security.entity.TokenType;
import hekireki.sanjijiksong.global.security.jwt.JwtUtil;
import hekireki.sanjijiksong.global.security.repository.RefreshRepository;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class ReissueService {

    private final JwtUtil jwtUtil;
    private final RefreshRepository refreshRepository;

    public ReissueResponse reIssueToken(String refresh) throws Exception {
        //쿠키에 토큰이 없는 경우
        if (refresh == null) {
            throw new SecurityException(ErrorCode.MISSING_REFRESH_TOKEN);
        }

        //토큰 만료 확인
        try {
            jwtUtil.isExpired(refresh);
        }catch (ExpiredJwtException e){
            throw new SecurityException(ErrorCode.EXPIRED_REFRESH_TOKEN);
        }

        //refresh인지 확인
        String category = jwtUtil.getCategory(refresh);
        if(!category.equals(TokenType.REFRESH.getValue())){
            throw new SecurityException(ErrorCode.INVALID_REFRESH_TOKEN);
        }

        //실제 db에 있는지 확인
        Boolean isExist = refreshRepository.existsByRefreshToken(refresh);
        if(!isExist){
            throw new SecurityException(ErrorCode.REFRESH_TOKEN_NOT_FOUND);
        }

        String email = jwtUtil.getEmail(refresh);
        String role = jwtUtil.getRole(refresh);

        String newAccess = jwtUtil.createJwt(TokenType.ACCESS.getValue(), email, role, JwtUtil.ACCESS_TOKEN_EXPIRE_TIME);

        String newRefresh = jwtUtil.createJwt(TokenType.REFRESH.getValue(), email, role, JwtUtil.REFRESH_TOKEN_EXPIRE_TIME);
        refreshRepository.deleteByRefreshToken(refresh);
        addRefreshEntity(email, newRefresh, JwtUtil.REFRESH_TOKEN_EXPIRE_TIME);
        Cookie refreshCookie = createCookie(TokenType.REFRESH.getValue(), newRefresh);

        return new ReissueResponse(newAccess,refreshCookie);
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
