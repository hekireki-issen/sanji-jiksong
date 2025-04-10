package hekireki.sanjijiksong.domain.user.controller;

import hekireki.sanjijiksong.domain.user.dto.UserResponse;
import hekireki.sanjijiksong.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final UserService userService;

    // 모든 유저 조회
    @GetMapping("/users")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    // 유저 탈퇴 처리(active 비활성화)
    @PatchMapping("/users/{id}/deactivate")
    public ResponseEntity<Void> deactivateUser(@PathVariable Long id) {
        userService.deactivateUserByAdmin(id);
        return ResponseEntity.noContent().build();
    }
}
