package hekireki.sanjijiksong.domain.user.dto;

import hekireki.sanjijiksong.domain.user.entity.Role;

public record UserRegisterRequest(
        String email,
        String password,
        String nickname,
        String address,
        Role role
) {}