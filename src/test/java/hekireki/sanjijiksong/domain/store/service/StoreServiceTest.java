package hekireki.sanjijiksong.domain.store.service;

import hekireki.sanjijiksong.domain.store.dto.StoreCreateRequest;
import hekireki.sanjijiksong.domain.store.dto.StoreResponse;
import hekireki.sanjijiksong.domain.store.dto.StoreUpdateRequest;
import hekireki.sanjijiksong.domain.store.entity.Store;
import hekireki.sanjijiksong.domain.store.repository.StoreRepository;
import hekireki.sanjijiksong.domain.user.entity.User;
import hekireki.sanjijiksong.domain.user.repository.UserRepository;
import hekireki.sanjijiksong.global.common.exception.ErrorCode;
import hekireki.sanjijiksong.global.common.exception.StoreException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class StoreServiceTest {

    @Mock
    private StoreRepository storeRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private StoreService storeService;

    private User testUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testUser = User.builder().id(1L).build();
    }

    @Test
    @DisplayName("가게 등록 성공")
    void createStore_Success() {
        StoreCreateRequest request = new StoreCreateRequest("가게이름", "서울시", "설명", "image.jpg");
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(storeRepository.existsByUser(testUser)).thenReturn(false);

        Store savedStore = Store.builder()
                .id(1L).user(testUser)
                .name(request.name())
                .address(request.address())
                .description(request.description())
                .image(request.image())
                .active(true)
                .build();

        when(storeRepository.save(any(Store.class))).thenReturn(savedStore);

        StoreResponse response = storeService.create(request, 1L);

        assertNotNull(response);
        assertEquals("가게이름", response.name());
    }

    @Test
    @DisplayName("가게 등록 실패 - 유저 없음")
    void createStore_Fails_WhenUserNotFound() {
        StoreCreateRequest request = new StoreCreateRequest("상점명", "서울시", "설명", "image.png");
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(StoreException.class, () -> storeService.create(request, 1L));
    }

    @Test
    @DisplayName("가게 등록 실패 - 이미 등록됨")
    void createStore_Fails_WhenAlreadyExists() {
        StoreCreateRequest request = new StoreCreateRequest("상점명", "서울시", "설명", "image.png");
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(storeRepository.existsByUser(testUser)).thenReturn(true);

        assertThrows(StoreException.class, () -> storeService.create(request, 1L));
    }

    @Test
    @DisplayName("가게 조회 성공")
    void getStoreById_Success() {
        Store store = Store.builder().id(1L).name("가게").active(true).build();
        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));

        StoreResponse response = storeService.getById(1L);
        assertEquals("가게", response.name());
    }

    @Test
    @DisplayName("가게 조회 실패 - 비활성화")
    void getStoreById_Fails_WhenInactive() {
        Store store = Store.builder().id(1L).active(false).build();
        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));

        assertThrows(StoreException.class, () -> storeService.getById(1L));
    }

    @Test
    @DisplayName("가게 전체 조회 성공")
    void getAllActiveStores_Success() {
        Store store = Store.builder().id(1L).name("가게").active(true).build();
        Page<Store> mockPage = new PageImpl<>(List.of(store));

        when(storeRepository.findByActiveTrue(any(Pageable.class))).thenReturn(mockPage);

        Page<StoreResponse> responses = storeService.getAllActiveStores(PageRequest.of(0, 10));
        assertEquals(1, responses.getTotalElements());
    }


    @Test
    @DisplayName("가게 수정 성공")
    void updateStore_Success() {
        Store store = Store.builder()
                .id(1L).user(testUser).active(true).build();
        StoreUpdateRequest request = new StoreUpdateRequest("수정이름", "주소", "설명", "image.png");

        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));

        StoreResponse response = storeService.update(1L, 1L, request);

        assertNotNull(response);
        assertEquals("수정이름", response.name());
    }

    @Test
    @DisplayName("가게 수정 실패 - 권한 없음")
    void updateStore_Fails_WhenUnauthorized() {
        User anotherUser = User.builder().id(2L).build();
        Store store = Store.builder().id(1L).user(anotherUser).active(true).build();
        StoreUpdateRequest request = new StoreUpdateRequest("수정이름", "주소", "설명", "image.png");

        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));

        assertThrows(StoreException.class, () -> storeService.update(1L, 1L, request));
    }

    @Test
    @DisplayName("가게 비활성화 성공")
    void deactivateStore_Success() {
        Store store = Store.builder().id(1L).user(testUser).active(true).build();
        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));

        storeService.deactivate(1L, 1L);
        assertFalse(store.getActive());
    }

    @Test
    @DisplayName("가게 비활성화 실패 - 권한 없음")
    void deactivateStore_Fails_WhenUnauthorized() {
        User anotherUser = User.builder().id(2L).build();
        Store store = Store.builder().id(1L).user(anotherUser).active(true).build();
        when(storeRepository.findById(1L)).thenReturn(Optional.of(store));

        assertThrows(StoreException.class, () -> storeService.deactivate(1L, 1L));
    }

    @Test
    @DisplayName("키워드 검색 성공")
    void searchStoreByKeyword_Success() {
        Store store = Store.builder().id(1L).name("한강분식").active(true).build();
        when(storeRepository.findByNameContainingAndActiveTrue("분식"))
                .thenReturn(List.of(store));

        List<StoreResponse> results = storeService.searchByKeyword("분식");
        assertEquals(1, results.size());
        assertEquals("한강분식", results.get(0).name());
    }
}
