package hekireki.sanjijiksong.domain.item.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hekireki.sanjijiksong.domain.item.dto.ItemCreateRequest;
import hekireki.sanjijiksong.domain.item.dto.ItemResponse;
import hekireki.sanjijiksong.domain.item.dto.ItemUpdateRequest;
import hekireki.sanjijiksong.domain.item.entity.ItemStatus;
import hekireki.sanjijiksong.domain.item.service.ItemService;
import hekireki.sanjijiksong.domain.user.entity.Role;
import hekireki.sanjijiksong.domain.user.entity.User;
import hekireki.sanjijiksong.global.common.s3.S3Service;
import hekireki.sanjijiksong.global.security.dto.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ItemControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean
    private ItemService itemService;
    @MockitoBean
    private S3Service s3Service;
    @Autowired private ObjectMapper objectMapper;

    private CustomUserDetails userDetails;

    @BeforeEach
    void setUp() {
        User user = User.builder()
                .id(1L)
                .email("test@naver.com")
                .password("encoded")
                .role(Role.SELLER)
                .nickname("테스트")
                .active(true)
                .address("서울")
                .build();

        userDetails = new CustomUserDetails(user);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    void createItem() throws Exception {
        ItemCreateRequest req = new ItemCreateRequest("과일", "사과", 1000, null, 10, "사과입니다");
        ItemResponse res = new ItemResponse(1L, 1L, "사과", 1000, "url", 10, "사과입니다", true, ItemStatus.ONSALE, "과일");
        when(s3Service.uploadImage(any())).thenReturn("url");
        when(itemService.createItem(any(), any())).thenReturn(res);

        MockMultipartFile itemPart = new MockMultipartFile("item", "item", "application/json", objectMapper.writeValueAsBytes(req));
        MockMultipartFile image = new MockMultipartFile("image", "img.jpg", MediaType.IMAGE_JPEG_VALUE, "fake".getBytes());

        mockMvc.perform(multipart("/api/v1/stores/items")
                        .file(itemPart).file(image)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    void getMyItems() throws Exception {
        when(itemService.getMyItems(any())).thenReturn(List.of());
        mockMvc.perform(get("/api/v1/stores/items"))
                .andExpect(status().isOk());
    }

    @Test
    void getItemDetail() throws Exception {
        when(itemService.getItemDetail(any(), any())).thenReturn(null);
        mockMvc.perform(get("/api/v1/stores/1/items/1"))
                .andExpect(status().isOk());
    }

    @Test
    void updateItem() throws Exception {
        ItemUpdateRequest req = new ItemUpdateRequest("과일", "수정사과", 1500, "old.jpg", 20, "수정", ItemStatus.ONSALE);
        ItemResponse res = new ItemResponse(1L, 1L, "수정사과", 1500, "new.jpg", 20, "수정", true, ItemStatus.ONSALE, "과일");
        when(s3Service.uploadImage(any())).thenReturn("new.jpg");
        when(itemService.updateItem(any(), any(), any())).thenReturn(res);

        MockMultipartFile itemPart = new MockMultipartFile("item", "item", "application/json", objectMapper.writeValueAsBytes(req));
        MockMultipartFile image = new MockMultipartFile("image", "img.jpg", MediaType.IMAGE_JPEG_VALUE, "fake".getBytes());

        mockMvc.perform(multipart("/api/v1/stores/items/1")
                        .file(itemPart).file(image)
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .with(csrf())
                        .with(request -> {
                            request.setMethod("PATCH");
                            return request;
                        }))
                .andExpect(status().isOk());
    }

    @Test
    void deactivateItem() throws Exception {
        mockMvc.perform(patch("/api/v1/stores/items/1/deactivate").with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    void getSalesOverview() throws Exception {
        when(itemService.getSalesOverview(any())).thenReturn(ResponseEntity.ok().build());
        mockMvc.perform(get("/api/v1/stores/statistics"))
                .andExpect(status().isOk());
    }

    @Test
    void getTop5BestSellingProducts() throws Exception {
        when(itemService.getTop5BestSellingProducts(any())).thenReturn(ResponseEntity.ok().build());
        mockMvc.perform(get("/api/v1/stores/best-products"))
                .andExpect(status().isOk());
    }

    @Test
    void getWeeklySalesTrend() throws Exception {
        when(itemService.getWeeklySalesTrend(any())).thenReturn(ResponseEntity.ok().build());
        mockMvc.perform(get("/api/v1/stores/weekly-sales"))
                .andExpect(status().isOk());
    }

    @Test
    void getDailyHourlySales() throws Exception {
        when(itemService.getDailyHourlySales(any(), any())).thenReturn(ResponseEntity.ok().build());
        mockMvc.perform(get("/api/v1/stores/hourly-sales")
                        .param("localDateTime", LocalDateTime.now().toString()))
                .andExpect(status().isOk());
    }
}