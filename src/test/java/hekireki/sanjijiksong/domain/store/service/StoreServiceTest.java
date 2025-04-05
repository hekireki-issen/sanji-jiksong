package hekireki.sanjijiksong.domain.store.service;

import hekireki.sanjijiksong.domain.store.dto.StoreCreateRequest;
import hekireki.sanjijiksong.domain.store.dto.StoreResponse;
import hekireki.sanjijiksong.domain.store.entity.Store;
import hekireki.sanjijiksong.domain.store.repository.StoreRepository;
import hekireki.sanjijiksong.domain.user.entity.Role;
import hekireki.sanjijiksong.domain.user.entity.User;
import hekireki.sanjijiksong.domain.user.repository.UserRepository;
import hekireki.sanjijiksong.global.common.exception.ErrorCode;
import hekireki.sanjijiksong.global.common.exception.StoreException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class StoreServiceTest {

    private StoreRepository storeRepository;
    private UserRepository userRepository;
    private StoreService storeService;

    @BeforeEach
    void setUp() {
        storeRepository = mock(StoreRepository.class);
        userRepository = mock(UserRepository.class);
        storeService = new StoreService(storeRepository, userRepository);
    }

    @Test
    @DisplayName("가게 등록 성공")
    void createStore_success() {
        // given
        Long userId = 1L;
        User user = new User(userId, "test@example.com", "pw", "닉네임", "서울", Role.SELLER, true);

        StoreCreateRequest request = new StoreCreateRequest(
                "가게명", "주소", "설명", "이미지"
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(storeRepository.existsByUser(user)).thenReturn(false);
        when(storeRepository.save(any(Store.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        StoreResponse response = storeService.create(request, userId);

        // then
        assertThat(response.name()).isEqualTo("가게명");
        assertThat(response.address()).isEqualTo("주소");
        assertThat(response.active()).isTrue();

        verify(storeRepository).save(any(Store.class));
    }

    @Test
    @DisplayName("유저가 존재하지 않으면 예외 발생")
    void createStore_userNotFound() {
        // given
        Long userId = 99L;
        StoreCreateRequest request = new StoreCreateRequest(
                "가게명", "주소", "설명", "이미지"
        );

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> storeService.create(request, userId))
                .isInstanceOf(StoreException.class)
                .hasMessageContaining(ErrorCode.USER_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("이미 가게를 등록한 경우 예외 발생")
    void createStore_alreadyRegistered() {
        // given
        Long userId = 1L;
        User user = new User(userId, "test@example.com", "pw", "닉네임", "서울", Role.SELLER, true);

        StoreCreateRequest request = new StoreCreateRequest(
                "가게명", "주소", "설명", "이미지"
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(storeRepository.existsByUser(user)).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> storeService.create(request, userId))
                .isInstanceOf(StoreException.class)
                .hasMessageContaining(ErrorCode.STORE_ALREADY_REGISTERED.getMessage());
    }
}
