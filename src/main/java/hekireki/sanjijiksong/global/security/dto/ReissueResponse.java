package hekireki.sanjijiksong.global.security.dto;

import jakarta.servlet.http.Cookie;

public record ReissueResponse (
        String newAccessToken,
        Cookie refreshCookie
){}
