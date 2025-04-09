package hekireki.sanjijiksong.domain.store.service;

import hekireki.sanjijiksong.domain.store.dto.StoreCreateRequest;
import hekireki.sanjijiksong.domain.store.dto.StoreResponse;
import hekireki.sanjijiksong.domain.store.entity.Store;
import hekireki.sanjijiksong.domain.store.repository.StoreRepository;
import hekireki.sanjijiksong.domain.user.entity.Role;
import hekireki.sanjijiksong.domain.user.entity.User;
import hekireki.sanjijiksong.domain.user.repository.UserRepository;
import hekireki.sanjijiksong.global.common.exception.StoreException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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

        testUser = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("pass")
                .nickname("tester")
                .role(Role.SELLER)
                .active(true)
                .build();
    }

    @Test
    @DisplayName("가게 등록 성공")
    void createStore_Success() {
        // given
        StoreCreateRequest request = new StoreCreateRequest("상점명", "서울시", "설명", "image.png");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(storeRepository.existsByUser(testUser)).thenReturn(false);

        Store savedStore = Store.builder()
                .id(1L)
                .user(testUser)
                .name(request.name())
                .address(request.address())
                .description(request.description())
                .image(request.image())
                .active(true)
                .build();

        when(storeRepository.save(any(Store.class))).thenReturn(savedStore);

        // when
        StoreResponse response = storeService.create(request, 1L);

        // then
        assertEquals("상점명", response.name());
        assertEquals("서울시", response.address());
        assertEquals("설명", response.description());
        assertEquals("image.png", response.image());
        assertTrue(response.active());
    }

    @Test
    @DisplayName("가게 등록 실패 - 존재하지 않는 유저")
    void createStore_Fails_WhenUserNotFound() {
        // given
        StoreCreateRequest request = new StoreCreateRequest("상점명", "서울시", "설명", "image.png");
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // expect
        assertThrows(StoreException.class, () -> storeService.create(request, 1L));
    }

    @Test
    @DisplayName("가게 등록 실패 - 이미 등록된 가게가 있음")
    void createStore_Fails_WhenStoreAlreadyRegistered() {
        // given
        StoreCreateRequest request = new StoreCreateRequest("상점명", "서울시", "설명", "image.png");

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(storeRepository.existsByUser(testUser)).thenReturn(true);

        // expect
        assertThrows(StoreException.class, () -> storeService.create(request, 1L));
    }
}
