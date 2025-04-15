package hekireki.sanjijiksong.domain.item.service;

import hekireki.sanjijiksong.domain.item.dto.ItemCreateRequest;
import hekireki.sanjijiksong.domain.item.dto.ItemResponse;
import hekireki.sanjijiksong.domain.item.dto.ItemUpdateRequest;
import hekireki.sanjijiksong.domain.item.entity.Item;
import hekireki.sanjijiksong.domain.item.entity.ItemStatus;
import hekireki.sanjijiksong.domain.item.repository.ItemRepository;
import hekireki.sanjijiksong.domain.order.entity.OrderList;
import hekireki.sanjijiksong.domain.order.repository.OrderListRepository;
import hekireki.sanjijiksong.domain.store.entity.Store;
import hekireki.sanjijiksong.domain.user.entity.Role;
import hekireki.sanjijiksong.domain.user.entity.User;
import hekireki.sanjijiksong.domain.user.repository.UserRepository;
import hekireki.sanjijiksong.global.common.exception.StoreException;
import hekireki.sanjijiksong.global.common.exception.UserException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;


public class ItemServiceTest {
    @InjectMocks
    private ItemService itemService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private OrderListRepository orderListRepository;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
    }

    private User createTestUser() {
        return User.builder()
                .id(1L)
                .email("test@naver.com")
                .password("encoded-password")
                .nickname("테스트유저")
                .address("서울특별시 강남구")
                .role(Role.SELLER)
                .active(true)
                .build();
    }

    private Store createTestStore(User user) {
        Store store = Store.builder()
                .id(1L)
                .user(user)
                .name("테스트 가게")
                .address("서울특별시 강남구")
                .description("테스트 설명입니다")
                .image("store.jpg")
                .active(true)
                .build();
        user.setStore(store); // 연관관계 설정
        return store;
    }
    private Item createTestItem(ItemCreateRequest itemCreateRequest, Store store) {
        return Item.builder()
                .store(store)
                .name(itemCreateRequest.itemName())
                .price(itemCreateRequest.price())
                .image(itemCreateRequest.image())
                .stock(itemCreateRequest.stock())
                .description(itemCreateRequest.description())
                .active(true)
                .itemStatus(ItemStatus.ONSALE)
                .category(itemCreateRequest.category())
                .build();
    }

    private OrderList createOrder(Store store, String itemName, int price, int count) {
        Item item = Item.builder()
                .store(store)
                .name(itemName)
                .price(price)
                .stock(100)
                .active(true)
                .itemStatus(ItemStatus.ONSALE)
                .category("과일")
                .build();

        return OrderList.builder()
                .store(store)
                .item(item)
                .count(count)
                .countPrice(price * count)
                .build();
    }

    @Test
    void STORE_NOT_FOUND_예외(){
        //given
        ItemCreateRequest itemCreateRequest = new ItemCreateRequest(
                "과일", "사과",1000,"image.jpg", 10, "사과입니다"
        );

        when(userRepository.findByEmail("test@naver.com")).thenReturn(Optional.empty());

        //when, then
        assertThatThrownBy(() -> itemService.createItem(itemCreateRequest, "test@naver.com"))
                .isInstanceOf(UserException.class);
    }

    @Test
    void UNAUTHORIZED_STORE_OWNER_예외(){
        //given
        ItemCreateRequest itemCreateRequest = new ItemCreateRequest(
                "과일", "사과",1000,"image.jpg", 10, "사과입니다"
        );

        User user = User.builder()
                .email("test@naver.com")
                .password("encoded-password")
                .nickname("테스트유저")
                .address("서울특별시 강남구")
                .role(Role.SELLER)
                .active(true)
                .build();

        when(userRepository.findByEmail("test@naver.com")).thenReturn(Optional.of(user));

        //when, then
        assertThatThrownBy(() -> itemService.createItem(itemCreateRequest, "test@naver.com"))
                .isInstanceOf(StoreException.class);
    }

    @Test
    void 상품_생성_성공(){
        //given
        ItemCreateRequest itemCreateRequest = new ItemCreateRequest(
                "과일", "사과",1000,"image.jpg", 10, "사과입니다"
        );

        String email = "test@naver.com";
        User user = createTestUser();
        Store store = createTestStore(user);
        Item item = createTestItem(itemCreateRequest, store);
        ReflectionTestUtils.setField(item, "id", 1L);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        //when
        ItemResponse itemResponse = itemService.createItem(itemCreateRequest,email);

        //then
        assertThat(itemResponse.name()).isEqualTo("사과");
        verify(itemRepository).save(any());
    }
    @Test
    void 내상품_조회(){
        //given
        ItemCreateRequest itemCreateRequest = new ItemCreateRequest(
                "과일", "사과",1000,"image.jpg", 10, "사과입니다"
        );
        String email = "test@naver.com";
        User user = createTestUser();
        Store store = createTestStore(user);
        Item item = createTestItem(itemCreateRequest, store);
        ReflectionTestUtils.setField(item, "id", 1L);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(itemRepository.findAllByStoreId(store.getId())).thenReturn(List.of(item));

        //when
        List<ItemResponse> myItems = itemService.getMyItems(email);

        //then
        assertThat(myItems.get(0).name()).isEqualTo("사과");
    }
    @Test
    void 상품_상세_조회(){
        //given
        ItemCreateRequest itemCreateRequest = new ItemCreateRequest(
                "과일", "사과",1000,"image.jpg", 10, "사과입니다"
        );
        String email = "test@naver.com";
        User user = createTestUser();
        Store store = createTestStore(user);
        Item item = createTestItem(itemCreateRequest, store);
        ReflectionTestUtils.setField(item, "id", 1L);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(itemRepository.findByStoreIdAndId(store.getId(), item.getId())).thenReturn(Optional.of(item));

        //when
        ItemResponse itemDetail = itemService.getItemDetail(store.getId(), item.getId());

        //then
        assertThat(itemDetail.name()).isEqualTo("사과");
    }
    @Test
    void 상품_수정_성공(){
        //given
        ItemCreateRequest itemCreateRequest = new ItemCreateRequest(
                "과일", "사과",1000,"image.jpg", 10, "사과입니다"
        );
        String email = "test@naver.com";

        User user = createTestUser();
        Store store = createTestStore(user);
        Item item = createTestItem(itemCreateRequest, store);
        ReflectionTestUtils.setField(item, "id", 1L);

        ItemUpdateRequest itemUpdateRequest = new ItemUpdateRequest(
                "채소", "양파",10000,"image2.jpg", 20, "양파입니다", ItemStatus.SOLDOUT
        );

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(itemRepository.findByStoreIdAndId(store.getId(), item.getId())).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenReturn(item);

        //when
        ItemResponse itemResponse = itemService.updateItem(1L, email, itemUpdateRequest);

        //then
        assertThat(itemResponse.category()).isEqualTo("채소");
        assertThat(itemResponse.name()).isEqualTo("양파");
        assertThat(itemResponse.price()).isEqualTo(10000);
        assertThat(itemResponse.image()).isEqualTo("image2.jpg");
        assertThat(itemResponse.stock()).isEqualTo(20);
        assertThat(itemResponse.description()).isEqualTo("양파입니다");
        assertThat(itemResponse.itemStatus()).isEqualTo(ItemStatus.SOLDOUT);

    }
    @Test
    void 상품_비활성화_성공(){
        ItemCreateRequest itemCreateRequest = new ItemCreateRequest(
                "과일", "사과",1000,"image.jpg", 10, "사과입니다"
        );
        String email = "test@naver.com";

        User user = createTestUser();
        Store store = createTestStore(user);
        Item item = createTestItem(itemCreateRequest, store);
        ReflectionTestUtils.setField(item, "id", 1L);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(itemRepository.findByStoreIdAndId(store.getId(), item.getId())).thenReturn(Optional.of(item));

        //when
        itemService.deactivateItem(item.getId(),email);

        //then
        assertThat(item.getActive()).isEqualTo(false);
    }

    @Test
    void 매출_현황_조회() {
        String email = "test@naver.com";
        User user = createTestUser();
        Store store = createTestStore(user);
        user.setStore(store);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(orderListRepository.findAllByStoreId(store.getId())).thenReturn(List.of(
                createOrder(store, "사과", 1000, 2),
                createOrder(store, "바나나", 1500, 1)
        ));

        ResponseEntity<Map<String, Object>> response = itemService.getSalesOverview(email);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).containsKey("labels");
        assertThat(response.getBody())
                .extracting("labels")
                .asList()
                .contains("사과", "바나나"); // 가능하지만 타입 안정성 떨어짐
    }

    @Test
    void 베스트_상품_조회() {
        String email = "test@naver.com";
        User user = createTestUser();
        Store store = createTestStore(user);
        user.setStore(store);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(orderListRepository.findAllByStoreId(store.getId())).thenReturn(List.of(
                createOrder(store, "사과", 1000, 5),
                createOrder(store, "바나나", 3000, 1),
                createOrder(store, "귤", 500, 10)
        ));

        ResponseEntity<?> response = itemService.getTop5BestSellingProducts(email);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        Map<String, Object> chartData = (Map<String, Object>) response.getBody();
        assertThat(chartData).containsKey("xAxis");
    }

    @Test
    void 주간_매출_추이_조회() {
        String email = "test@naver.com";
        User user = createTestUser();
        Store store = createTestStore(user);
        user.setStore(store);

        OrderList order1 = createOrder(store, "사과", 1000, 1);
        ReflectionTestUtils.setField(order1, "createdAt", LocalDateTime.now().minusDays(1));

        OrderList order2 = createOrder(store, "배", 2000, 1);
        ReflectionTestUtils.setField(order2, "createdAt", LocalDateTime.now().minusDays(2));

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(orderListRepository.findAllByStoreId(store.getId())).thenReturn(List.of(order1, order2));

        ResponseEntity<?> response = itemService.getWeeklySalesTrend(email);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isInstanceOf(Map.class);
    }

    @Test
    void 일별_시간대별_매출_조회() {
        String email = "test@naver.com";
        User user = createTestUser();
        Store store = createTestStore(user);
        user.setStore(store);

        LocalDateTime now = LocalDateTime.now();

        OrderList order = createOrder(store, "참외", 4000, 1);
        ReflectionTestUtils.setField(order, "createdAt", now);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));
        when(orderListRepository.findAllByStoreIdAndDate(eq(store.getId()), any(), any()))
                .thenReturn(List.of(order));

        ResponseEntity<?> response = itemService.getDailyHourlySales(email, now);

        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getBody()).isInstanceOf(Map.class);
    }
}
