package hekireki.sanjijiksong.domain.user.api;

import hekireki.sanjijiksong.domain.user.dto.PasswordResetRequest;
import hekireki.sanjijiksong.domain.user.dto.UserRegisterRequest;
import hekireki.sanjijiksong.domain.user.dto.UserResponse;
import hekireki.sanjijiksong.global.security.dto.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "UserController", description = "사용자 Controller")
public interface UserApi {

    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "회원가입 성공",
                    content = @Content(schema = @Schema(implementation = UserResponse.class),
                            examples = @ExampleObject(name = "회원가입 성공 예시", value = """
                                        {
                                          "id": 1,
                                          "email": "user@example.com",
                                          "nickname": "nickname",
                                          "address": "Seoul, Korea",
                                          "role": "BUYER",
                                          "active": true
                                        }
                                    """))),
            @ApiResponse(responseCode = "400", description = "중복 이메일 오류",
                    content = @Content(
                            examples = @ExampleObject(name = "중복 이메일 오류 예시", value = """
                                        {
                                          "code": "USER_EMAIL_ALREADY_EXISTS",
                                          "message": "이미 존재하는 이메일입니다."
                                        }
                                    """)))
    })
    @PostMapping("/register")
    ResponseEntity<UserResponse> register(
            @RequestBody(
                    description = "회원가입 요청 정보",
                    required = true,
                    content = @Content(schema = @Schema(implementation = UserRegisterRequest.class),
                            examples = @ExampleObject(value = """
                                        {
                                          "email": "user@example.com",
                                          "password": "securePassword123",
                                          "nickname": "nickname",
                                          "address": "Seoul, Korea",
                                          "role": "BUYER"
                                        }
                                    """))
            ) UserRegisterRequest request
    );

    @Operation(summary = "회원탈퇴", description = "회원 계정을 비활성화합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "회원탈퇴 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(examples = @ExampleObject(value = """
                                {
                                  "code": "FORBIDDEN",
                                  "message": "접근 권한이 없습니다."
                                }
                            """)))
    })
    @PatchMapping("/users/{id}")
    ResponseEntity<Void> deleteUser(@PathVariable Long id,
                                    @AuthenticationPrincipal CustomUserDetails userDetails);

    @Operation(summary = "회원복구", description = "비활성화된 회원 계정을 복구합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "회원복구 성공"),
            @ApiResponse(responseCode = "403", description = "권한 없음",
                    content = @Content(examples = @ExampleObject(value = """
                                {
                                  "code": "FORBIDDEN",
                                  "message": "접근 권한이 없습니다."
                                }
                            """)))
    })
    @PostMapping("/users/{id}/restore")
    ResponseEntity<Void> restoreUser(@PathVariable Long id,
                                     @AuthenticationPrincipal CustomUserDetails userDetails);

    @Operation(summary = "비밀번호 재설정", description = "사용자의 비밀번호를 재설정합니다.")
    @ApiResponse(responseCode = "204", description = "비밀번호 재설정 성공")
    @PostMapping("/users/{id}/password")
    ResponseEntity<Void> resetPassword(@PathVariable Long id,
                                       @RequestBody(
                                               description = "재설정할 비밀번호",
                                               required = true,
                                               content = @Content(schema = @Schema(implementation = PasswordResetRequest.class),
                                                       examples = @ExampleObject(value = """
                                                                    {
                                                                      "password": "newPassword123"
                                                                    }
                                                               """))
                                       ) PasswordResetRequest request,
                                       @AuthenticationPrincipal CustomUserDetails userDetails);

    @Operation(summary = "내 정보 조회", description = "로그인된 사용자의 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = UserResponse.class),
                            examples = @ExampleObject(value = """
                                        {
                                          "id": 1,
                                          "email": "user@example.com",
                                          "nickname": "nickname",
                                          "address": "Seoul, Korea",
                                          "role": "BUYER",
                                          "active": true
                                        }
                                    """))),
            @ApiResponse(responseCode = "401", description = "인증 실패",
                    content = @Content(mediaType = "application/json",
                            examples = @ExampleObject(name = "인증 실패 예시", value = """
                                        {
                                          "code": "UNAUTHORIZED",
                                          "message": "Full authentication is required to access this resource"
                                        }
                                    """)))
    })
    @GetMapping("/me")
    ResponseEntity<UserResponse> getMyInfo(@AuthenticationPrincipal CustomUserDetails userDetails);
}