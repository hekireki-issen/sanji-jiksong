package hekireki.sanjijiksong.domain.store.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hekireki.sanjijiksong.domain.store.dto.StoreCreateRequest;
import hekireki.sanjijiksong.domain.store.dto.StoreResponse;
import hekireki.sanjijiksong.domain.store.dto.StoreUpdateRequest;
import hekireki.sanjijiksong.domain.store.service.StoreService;
import hekireki.sanjijiksong.global.common.s3.S3Service;
import hekireki.sanjijiksong.domain.user.entity.Role;
import hekireki.sanjijiksong.domain.user.entity.User;
import hekireki.sanjijiksong.global.security.dto.CustomUserDetails;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class StoreControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StoreService storeService;

    @MockBean
    private S3Service s3Service;

    private final User mockSeller = User.builder()
            .id(1L)
            .email("seller@example.com")
            .password("pass")
            .nickname("판매자")
            .address("서울")
            .role(Role.SELLER)
            .active(true)
            .build();

    @Test
    @WithMockUser(username = "seller@example.com", roles="SELLER")
    @DisplayName("가게 등록 성공")
    void createStore() throws Exception {
        StoreCreateRequest request = new StoreCreateRequest("카페 산지", "서울", "설명", null);
        String json = objectMapper.writeValueAsString(request);

        MockMultipartFile store = new MockMultipartFile("store", "", "application/json", json.getBytes());
        MockMultipartFile image = new MockMultipartFile("image", "file.jpg", MediaType.IMAGE_JPEG_VALUE, "image-data".getBytes());

        when(s3Service.uploadImage(any())).thenReturn("https://image.url");
        when(storeService.create(any(), eq(1L))).thenReturn(
                new StoreResponse(1L, "카페 산지", "서울", "설명", "https://image.url", true)
        );


        CustomUserDetails userDetails = new CustomUserDetails(mockSeller);

        mockMvc.perform(multipart("/api/v1/stores")
                        .file(store)
                        .file(image)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(
                                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
                        ))
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("카페 산지"));
    }
    @Test
    @DisplayName("가게 등록 - 이미지 없이도 성공")
    void createStoreWithoutImage() throws Exception {
        StoreCreateRequest request = new StoreCreateRequest("카페 무이미지", "서울", "설명", null);
        String json = objectMapper.writeValueAsString(request);
        MockMultipartFile store = new MockMultipartFile("store", "", "application/json", json.getBytes());

        when(storeService.create(any(), anyLong())).thenReturn(
                new StoreResponse(2L, "카페 무이미지", "서울", "설명", "", true)
        );

        CustomUserDetails userDetails = new CustomUserDetails(mockSeller);

        mockMvc.perform(multipart("/api/v1/stores")
                        .file(store)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(
                                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
                        ))
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("카페 무이미지"));
    }
    @Test
    @DisplayName("가게 조회 - 존재하지 않는 storeId")
    void getStoreById_notFound() throws Exception {
        when(storeService.getById(999L)).thenReturn(null);

        mockMvc.perform(get("/api/v1/stores/999"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("가게 전체 조회 성공")
    void getAllStores_success() throws Exception {
        StoreResponse mockStore = new StoreResponse(1L, "카페 귤", "서울시 종로구", "맛있는 집", "image.jpg", true);
        Page<StoreResponse> mockPage = new PageImpl<>(List.of(mockStore), PageRequest.of(0, 10), 1);

        when(storeService.getAllActiveStores(any(Pageable.class))).thenReturn(mockPage);

        mockMvc.perform(get("/api/v1/stores")
                        .param("page", "0")
                        .param("size", "10")
                        .with(SecurityMockMvcRequestPostProcessors.user("admin@example.com").roles("ADMIN")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("카페 귤"));
    }

    @Test
    @DisplayName("특정 가게 조회 성공")
    void getStoreById() throws Exception {
        when(storeService.getById(1L)).thenReturn(
                new StoreResponse(1L, "카페 산지", "서울", "설명", "image.jpg", true)
        );

        mockMvc.perform(get("/api/v1/stores/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @WithMockUser(username = "seller@example.com", roles = "SELLER")
    @DisplayName("가게 등록 실패 - 예외 발생")
    void createStore_throwsException() throws Exception {
        StoreCreateRequest request = new StoreCreateRequest("카페", "서울", "설명", null);
        String json = objectMapper.writeValueAsString(request);
        MockMultipartFile store = new MockMultipartFile("store", "", "application/json", json.getBytes());

        when(storeService.create(any(), eq(1L))).thenThrow(new RuntimeException("DB 오류"));

        CustomUserDetails userDetails = new CustomUserDetails(mockSeller);

        mockMvc.perform(multipart("/api/v1/stores")
                        .file(store)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(
                                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
                        ))
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isInternalServerError());
    }


    
    @Test
    @DisplayName("가게 검색 성공")
    void searchStore() throws Exception {
        when(storeService.searchByKeyword("카페")).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/stores/search?keyword=카페"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("가게 비활성화 성공")
    void deactivateStore() throws Exception {
        CustomUserDetails userDetails = new CustomUserDetails(mockSeller);

        mockMvc.perform(patch("/api/v1/stores/1/deactivate")
                        .with(SecurityMockMvcRequestPostProcessors.authentication(
                                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
                        )))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("가게 수정 성공")
    void updateStore() throws Exception {
        StoreUpdateRequest request = new StoreUpdateRequest("수정상점", "부산", "수정된 설명", "기존.png");
        String json = objectMapper.writeValueAsString(request);
        MockMultipartFile store = new MockMultipartFile("store", "", "application/json", json.getBytes());
        MockMultipartFile image = new MockMultipartFile("image", "updated.png", MediaType.IMAGE_PNG_VALUE, "image".getBytes());

        when(s3Service.uploadImage(any())).thenReturn("https://image.updated.url");
        when(storeService.update(anyLong(), anyLong(), any())).thenReturn(
                new StoreResponse(1L, "수정상점", "부산", "수정된 설명", "https://image.updated.url", true)
        );

        CustomUserDetails userDetails = new CustomUserDetails(mockSeller);

        mockMvc.perform(multipart("/api/v1/stores/1")
                        .file(store)
                        .file(image)
                        .with(SecurityMockMvcRequestPostProcessors.authentication(
                                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities())
                        ))
                        .with(req -> {
                            req.setMethod("PATCH");
                            return req;
                        })
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("수정상점"));
    }

}