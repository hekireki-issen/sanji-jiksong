package hekireki.sanjijiksong.domain.user.controller;

import hekireki.sanjijiksong.domain.user.dto.PasswordResetRequest;
import hekireki.sanjijiksong.domain.user.dto.UserRegisterRequest;
import hekireki.sanjijiksong.domain.user.dto.UserResponse;
import hekireki.sanjijiksong.domain.user.service.UserService;
import hekireki.sanjijiksong.global.security.dto.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 회원가입
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody UserRegisterRequest request) {
        UserResponse response = userService.register(request);
        return ResponseEntity.ok(response);
    }

    // 회원탈퇴
    @PatchMapping("/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id,
                                           @AuthenticationPrincipal CustomUserDetails userDetails) {
        userService.deleteUser(id, userDetails.getUser());
        return ResponseEntity.noContent().build();
    }

    // 회원복구
    @PostMapping("/users/{id}/restore")
    public ResponseEntity<Void> restoreUser(@PathVariable Long id,
                                            @AuthenticationPrincipal CustomUserDetails userDetails) {
        userService.restoreUser(id, userDetails.getUser());
        return ResponseEntity.noContent().build();
    }

    // 비밀번호 재설정
    @PostMapping("/users/{id}/password")
    public ResponseEntity<Void> resetPassword(@PathVariable Long id,
                                              @RequestBody PasswordResetRequest request,
                                              @AuthenticationPrincipal CustomUserDetails userDetails) {
        userService.resetPassword(id, request, userDetails.getUser());
        return ResponseEntity.noContent().build();
    }

    // 내 정보 조회
    @GetMapping("/me")
    public ResponseEntity<UserResponse> getMyInfo(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(UserResponse.from(userDetails.getUser()));
    }
}
