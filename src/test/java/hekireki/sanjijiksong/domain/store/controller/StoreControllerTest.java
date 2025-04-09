package hekireki.sanjijiksong.domain.store.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hekireki.sanjijiksong.domain.store.dto.StoreCreateRequest;
import hekireki.sanjijiksong.domain.store.dto.StoreResponse;
import hekireki.sanjijiksong.domain.store.service.StoreService;
import hekireki.sanjijiksong.domain.user.entity.Role;
import hekireki.sanjijiksong.domain.user.entity.User;
import hekireki.sanjijiksong.global.common.s3.S3Service;
import hekireki.sanjijiksong.global.security.dto.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class StoreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StoreService storeService;

    @MockBean
    private S3Service s3Service;

    @Autowired
    private ObjectMapper objectMapper;

    private CustomUserDetails userDetails;

    @BeforeEach
    void setupSecurityContext() {
        User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("test123")
                .nickname("테스트유저")
                .role(Role.SELLER)
                .active(true)
                .build();

        userDetails = new CustomUserDetails(user);

        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @Test
    @DisplayName("가게 등록 성공")
    void createStore_success() throws Exception {
        // given
        StoreCreateRequest requestDto = new StoreCreateRequest("감자상점", "강원도 평창", "신선한 감자!", null);
        String requestJson = objectMapper.writeValueAsString(requestDto);

        MockMultipartFile store = new MockMultipartFile("store", "", "application/json", requestJson.getBytes());
        MockMultipartFile image = new MockMultipartFile("image", "감자.png", MediaType.IMAGE_PNG_VALUE, "fake-image".getBytes());

        String mockImageUrl = "https://s3.bucket/감자.png";

        when(s3Service.uploadImage(any())).thenReturn(mockImageUrl);

        when(storeService.create(any(), any())).thenReturn(
                new StoreResponse(1L, "감자상점", "강원도 평창", "신선한 감자!", mockImageUrl, true)
        );

        // when & then
        mockMvc.perform(multipart("/api/v1/stores")
                        .file(store)
                        .file(image)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("감자상점"))
                .andExpect(jsonPath("$.image").value(mockImageUrl));
    }
}
