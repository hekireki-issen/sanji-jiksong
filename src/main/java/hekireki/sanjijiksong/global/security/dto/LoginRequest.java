package hekireki.sanjijiksong.global.security.dto;


import lombok.Data;

public record LoginRequest(
        String email,
        String password
) {}

