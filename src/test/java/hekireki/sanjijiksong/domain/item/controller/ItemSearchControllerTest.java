package hekireki.sanjijiksong.domain.item.controller;

import hekireki.sanjijiksong.domain.item.service.ItemService;
import hekireki.sanjijiksong.domain.user.entity.Role;
import hekireki.sanjijiksong.domain.user.entity.User;
import hekireki.sanjijiksong.global.security.dto.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ItemSearchControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private ItemService itemService;

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
    void itemSearch_정상호출() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);
        when(itemService.itemSearch(eq("사과"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/v1/items/search")
                        .param("keyword", "사과")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    void categorySearch_정상호출() throws Exception {
        when(itemService.categorySearch(eq("과일"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/api/v1/categories/search")
                        .param("keyword", "과일")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }

    @Test
    void itemSearch_키워드없으면_400() throws Exception {
        mockMvc.perform(get("/api/v1/items/search"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void categorySearch_키워드없으면_400() throws Exception {
        mockMvc.perform(get("/api/v1/categories/search"))
                .andExpect(status().isBadRequest());
    }
}