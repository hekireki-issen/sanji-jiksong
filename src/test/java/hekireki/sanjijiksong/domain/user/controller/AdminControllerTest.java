package hekireki.sanjijiksong.domain.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hekireki.sanjijiksong.domain.user.dto.UserResponse;
import hekireki.sanjijiksong.domain.user.entity.Role;
import hekireki.sanjijiksong.domain.user.entity.User;
import hekireki.sanjijiksong.domain.user.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AdminControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockBean private UserService userService;

    private final User mockAdmin = User.builder()
            .id(999L)
            .email("admin@example.com")
            .password("adminpass")
            .nickname("관리자")
            .address("서울")
            .role(Role.ADMIN)
            .active(true)
            .build();

    @Test
    @DisplayName("관리자 - 유저 전체 조회")
    void getAllUsers() throws Exception {
        UserResponse adminResponse = UserResponse.from(mockAdmin);
        Page<UserResponse> mockPage = new PageImpl<>(List.of(adminResponse));

        when(userService.getAllUsers(any(Pageable.class))).thenReturn(mockPage);

        mockMvc.perform(get("/api/v1/admin/users")
                        .param("page", "0")
                        .param("size", "10")
                        .with(SecurityMockMvcRequestPostProcessors.user("admin@example.com").roles("ADMIN")))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("관리자 - 유저 강제 탈퇴")
    void deactivateUser() throws Exception {
        mockMvc.perform(patch("/api/v1/admin/users/{id}/deactivate", 2L)
                        .with(SecurityMockMvcRequestPostProcessors.user("admin@example.com").roles("ADMIN")))
                .andExpect(status().isNoContent());

        verify(userService).deactivateUserByAdmin(2L);
    }
}
