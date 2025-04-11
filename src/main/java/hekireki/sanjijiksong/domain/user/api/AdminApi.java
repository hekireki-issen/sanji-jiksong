package hekireki.sanjijiksong.domain.user.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import hekireki.sanjijiksong.domain.user.dto.UserResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "AdminController", description = "관리자 전용 사용자 관리 Controller")
@RequestMapping("/api/v1/admin")
public interface AdminApi {

    @Operation(summary = "모든 사용자 조회", description = "모든 사용자 정보를 조회합니다. 관리자 권한이 필요합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = UserResponse.class)),
                            examples = @ExampleObject(name = "조회 성공 예시", value = """
                    [
                      {
                        "id": 1,
                        "email": "admin@example.com",
                        "nickname": "관리자",
                        "address": "Seoul",
                        "role": "ADMIN",
                        "active": true
                      },
                      {
                        "id": 2,
                        "email": "user@example.com",
                        "nickname": "사용자",
                        "address": "Busan",
                        "role": "BUYER",
                        "active": true
                      }
                    ]
                """))
            ),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음",
                    content = @Content(
                            examples = @ExampleObject(name = "권한 없음 예시", value = """
                    {
                      "code": "ACCESS_DENIED",
                      "message": "접근 권한이 없습니다."
                    }
                """))
            )
    })
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/users")
    ResponseEntity<List<UserResponse>> getAllUsers();

    @Operation(summary = "사용자 비활성화", description = "지정한 사용자의 active 상태를 false로 설정합니다. 관리자 권한이 필요합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "비활성화 성공"),
            @ApiResponse(responseCode = "403", description = "관리자 권한 없음",
                    content = @Content(
                            examples = @ExampleObject(name = "권한 없음 예시", value = """
                    {
                      "code": "ACCESS_DENIED",
                      "message": "접근 권한이 없습니다."
                    }
                """))
            ),
            @ApiResponse(responseCode = "404", description = "사용자 없음",
                    content = @Content(
                            examples = @ExampleObject(name = "사용자 없음 예시", value = """
                    {
                      "code": "USER_NOT_FOUND",
                      "message": "존재하지 않는 사용자입니다."
                    }
                """))
            ),
            @ApiResponse(responseCode = "400", description = "이미 탈퇴된 사용자",
                    content = @Content(
                            examples = @ExampleObject(name = "이미 탈퇴된 사용자 예시", value = """
                                {
                                  "code": "USER_ALREADY_DEACTIVATED",
                                  "message": "이미 탈퇴된 사용자입니다."
                                }
                            """))
            )
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/users/{id}/deactivate")
    ResponseEntity<Void> deactivateUser(
            @Parameter(description = "비활성화할 사용자 ID", example = "2")
            @PathVariable Long id
    );
}
