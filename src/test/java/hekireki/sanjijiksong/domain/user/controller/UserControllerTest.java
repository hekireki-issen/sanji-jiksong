package hekireki.sanjijiksong.domain.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hekireki.sanjijiksong.domain.user.dto.PasswordResetRequest;
import hekireki.sanjijiksong.domain.user.dto.UserRegisterRequest;
import hekireki.sanjijiksong.domain.user.dto.UserResponse;
import hekireki.sanjijiksong.domain.user.entity.Role;
import hekireki.sanjijiksong.domain.user.entity.User;
import hekireki.sanjijiksong.domain.user.service.UserService;
import hekireki.sanjijiksong.global.security.dto.CustomUserDetails;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserService userService;

    private final User mockUser = User.builder()
            .id(1L)
            .email("test@example.com")
            .password("encoded123")
            .nickname("테스터")
            .address("서울")
            .role(Role.BUYER)
            .active(true)
            .build();

    private final CustomUserDetails userDetails = new CustomUserDetails(mockUser);

    @TestConfiguration
    static class TestMockConfig {
        @Bean
        public UserService userService() {
            return Mockito.mock(UserService.class);
        }
    }

    @Test
    @DisplayName("회원가입 성공")
    void register() throws Exception {
        UserRegisterRequest request = new UserRegisterRequest("test@example.com", "1234", "nickname", "서울", Role.BUYER);
        UserResponse response = UserResponse.from(mockUser);

        when(userService.register(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    @DisplayName("회원탈퇴 성공")
    void deleteUser() throws Exception {
        mockMvc.perform(patch("/api/v1/users/{id}", 1L)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(
                                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
                        )))
                .andExpect(status().isNoContent());

        verify(userService).deleteUser(1L, mockUser);
    }

    @Test
    @DisplayName("회원복구 성공")
    void restoreUser() throws Exception {
        mockMvc.perform(post("/api/v1/users/{id}/restore", 1L)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(
                                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
                        )))
                .andExpect(status().isNoContent());

        verify(userService).restoreUser(1L, mockUser);
    }

    @Test
    @DisplayName("비밀번호 재설정 성공")
    void resetPassword() throws Exception {
        PasswordResetRequest request = new PasswordResetRequest("newpass");

        mockMvc.perform(post("/api/v1/users/{id}/password", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(SecurityMockMvcRequestPostProcessors.authentication(
                                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
                        )))
                .andExpect(status().isNoContent());

        verify(userService).resetPassword(1L, request, mockUser);
    }

    @Test
    @DisplayName("내 정보 조회 성공")
    void getMyInfo() throws Exception {
        mockMvc.perform(get("/api/v1/me")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(
                                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
                        )))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }
}