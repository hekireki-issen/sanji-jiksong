package hekireki.sanjijiksong.global.security.dto;

public record LoginRequest(
        String email,
        String password
) {}

