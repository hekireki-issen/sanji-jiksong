package hekireki.sanjijiksong.global.security.controller;

import hekireki.sanjijiksong.global.common.exception.ErrorCode;
import hekireki.sanjijiksong.global.common.exception.SecurityException;
import hekireki.sanjijiksong.global.security.dto.ReissueResponse;
import hekireki.sanjijiksong.global.security.entity.TokenType;
import hekireki.sanjijiksong.global.security.jwt.JwtUtil;
import hekireki.sanjijiksong.global.security.repository.RefreshRepository;
import hekireki.sanjijiksong.global.security.service.ReissueService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ReissueController {

    private final ReissueService reissueService;

    @PostMapping("/reissue")
    private ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response) throws Exception {
        String refresh = null;

        Cookie[] cookies = request.getCookies();
        if(cookies == null || cookies.length == 0){
            log.info("no cookie");
            throw new SecurityException(ErrorCode.NO_REFRESH_TOKEN_COOKIE);
        }

        log.info("cookies : " + cookies);

        for (Cookie cookie : cookies) {
            if(cookie.getName().equals(TokenType.REFRESH.getValue())){
                refresh = cookie.getValue();
            }
        }

        ReissueResponse reissueResponse = reissueService.reIssueToken(refresh);

        response.setHeader(TokenType.ACCESS.getValue(), reissueResponse.newAccessToken());
        response.addCookie(reissueResponse.refreshCookie());

        return new ResponseEntity<>(HttpStatus.OK);

    }
}
