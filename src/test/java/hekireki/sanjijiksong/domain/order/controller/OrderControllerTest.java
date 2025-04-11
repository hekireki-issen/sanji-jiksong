package hekireki.sanjijiksong.domain.order.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import hekireki.sanjijiksong.domain.item.entity.Item;
import hekireki.sanjijiksong.domain.item.entity.ItemStatus;
import hekireki.sanjijiksong.domain.item.repository.ItemRepository;
import hekireki.sanjijiksong.domain.order.dto.OrderListUpdateRequest;
import hekireki.sanjijiksong.domain.order.dto.OrderRequest;
import hekireki.sanjijiksong.domain.order.repository.OrderRepository;
import hekireki.sanjijiksong.domain.store.entity.Store;
import hekireki.sanjijiksong.domain.store.repository.StoreRepository;
import hekireki.sanjijiksong.domain.user.entity.Role;
import hekireki.sanjijiksong.domain.user.entity.User;
import hekireki.sanjijiksong.domain.user.repository.UserRepository;
import hekireki.sanjijiksong.global.security.dto.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class OrderControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private UserRepository userRepository;
    @Autowired private StoreRepository storeRepository;
    @Autowired private ItemRepository itemRepository;
    @Autowired private OrderRepository orderRepository;

    private Long itemId;

    @BeforeEach
    void setUp() {
        User user = userRepository.save(User.builder()
                .email("test@aaa.com")
                .password("1234")
                .nickname("nickname")
                .role(Role.BUYER)
                .address("대한민국")
                .build());

        Store store = storeRepository.save(Store.builder()
                .name("가게")
                .address("서울시")
                .description("설명")
                .image("image.jpg")
                .active(true)
                .user(user)
                .build());

        Item item = itemRepository.save(Item.builder()
                .name("상품")
                .price(10000)
                .description("설명")
                .stock(100)
                .active(true)
                .category("카테고리")
                .image("img.png")
                .itemStatus(ItemStatus.ONSALE)
                .store(store)
                .build());

        itemId = item.getId();

        CustomUserDetails customUserDetails = new CustomUserDetails(user);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(customUserDetails, null, customUserDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    @DisplayName("주문 생성 테스트")
    void 주문_생성_테스트() throws Exception {
        OrderRequest orderRequest = new OrderRequest(List.of(
                new OrderRequest.OrderListRequest(itemId, 2)
        ));

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("주문 항목 수정 테스트")
    void 주문_항목_수정_테스트() throws Exception {
        // 주문 생성
        OrderRequest orderRequest = new OrderRequest(List.of(
                new OrderRequest.OrderListRequest(itemId, 2)
        ));

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isCreated());

        Long orderId = orderRepository.findAll().get(0).getId();

        // 주문 항목 수정 요청
        OrderListUpdateRequest request = new OrderListUpdateRequest(orderId, List.of(
                new OrderListUpdateRequest.OrderListItemUpdate(itemId, 3)
        ));

        mockMvc.perform(patch("/api/v1/orders/{orderId}/items/{itemId}", orderId, itemId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("주문 취소 테스트")
    void 주문_취소_테스트() throws Exception {
        // 주문 생성
        OrderRequest orderRequest = new OrderRequest(List.of(
                new OrderRequest.OrderListRequest(itemId, 2)
        ));

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isCreated());

        Long orderId = orderRepository.findAll().get(0).getId();

        mockMvc.perform(patch("/api/v1/orders/{orderId}/cancel", orderId))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("단일 주문 조회 테스트")
    void 단일_주문_조회_테스트() throws Exception {
        // 주문 생성
        OrderRequest orderRequest = new OrderRequest(List.of(
                new OrderRequest.OrderListRequest(itemId, 2)
        ));

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isCreated());

        Long orderId = orderRepository.findAll().get(0).getId();

        // 단일 주문 조회 요청
        mockMvc.perform(get("/api/v1/orders/{orderId}", orderId))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("내 주문 목록 조회 테스트")
    void 내_주문_목록_조회_테스트() throws Exception {
        // 주문 두 개 생성
        OrderRequest orderRequest = new OrderRequest(List.of(
                new OrderRequest.OrderListRequest(itemId, 2)
        ));
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest)))
                .andExpect(status().isCreated());

        // 내 주문 목록 조회 요청
        mockMvc.perform(get("/api/v1/orders"))
                .andExpect(status().isOk());
    }
}
