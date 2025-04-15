package hekireki.sanjijiksong.domain.user.service;

import hekireki.sanjijiksong.domain.cart.repository.CartRepository;
import hekireki.sanjijiksong.domain.user.dto.PasswordResetRequest;
import hekireki.sanjijiksong.domain.user.dto.UserRegisterRequest;
import hekireki.sanjijiksong.domain.user.dto.UserResponse;
import hekireki.sanjijiksong.domain.user.entity.Role;
import hekireki.sanjijiksong.domain.user.entity.User;
import hekireki.sanjijiksong.domain.user.repository.UserRepository;
import hekireki.sanjijiksong.global.common.exception.UserException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void 회원가입_성공() {
        // given
        UserRegisterRequest request = new UserRegisterRequest(
                "test@example.com", "1234", "닉네임", "서울", Role.BUYER);

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("encodedPassword");

        User savedUser = User.builder()
                .id(1L)
                .email(request.email())
                .password("encodedPassword")
                .nickname(request.nickname())
                .address(request.address())
                .role(request.role())
                .active(true)
                .build();

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        // when
        UserResponse response = userService.register(request);

        // then
        assertThat(response.email()).isEqualTo("test@example.com");
        verify(userRepository).save(any());
    }

    @Test
    void 중복이메일_회원가입_실패() {
        // given
        UserRegisterRequest request = new UserRegisterRequest(
                "test@example.com", "1234", "닉네임", "서울", Role.SELLER);

        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        // when & then
        assertThatThrownBy(() -> userService.register(request))
                .isInstanceOf(UserException.UserEmailAlreadyExistsException.class);
    }

    @Test
    void 비밀번호_변경_성공() {
        // given
        User user = createUser(1L, true);
        PasswordResetRequest request = new PasswordResetRequest("newpass");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // when
        userService.resetPassword(1L, request, user);

        // then
        assertThat(user.getPassword()).isEqualTo("newpass");
    }

    @Test
    void 비밀번호_변경_실패_본인아님() {
        User 유저1 = createUser(1L, true);
        User 유저2 = createUser(2L, true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(유저1));

        assertThatThrownBy(() -> userService.resetPassword(1L, new PasswordResetRequest("abc"), 유저2))
                .isInstanceOf(UserException.UserUnauthorizedException.class);
    }

    @Test
    void 비밀번호_변경_실패_탈퇴된유저() {
        User user = createUser(1L, false); // 탈퇴된 유저
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> userService.resetPassword(1L, new PasswordResetRequest("abc"), user))
                .isInstanceOf(UserException.UserAlreadyDeactivatedException.class);
    }

    @Test
    void 탈퇴_성공() {
        User user = createUser(1L, true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.deleteUser(1L, user);

        assertThat(user.getActive()).isFalse();
    }

    @Test
    void 탈퇴_실패_본인아님() {
        User 유저1 = createUser(1L, true);
        User 유저2 = createUser(2L, true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(유저1));

        assertThatThrownBy(() -> userService.deleteUser(1L, 유저2))
                .isInstanceOf(UserException.UserUnauthorizedException.class);
    }

    @Test
    void 복구_성공() {
        User 탈퇴유저 = createUser(1L, false); // active=false

        ReflectionTestUtils.setField(탈퇴유저, "modifiedAt", LocalDateTime.now().minusDays(5));

        when(userRepository.findById(1L)).thenReturn(Optional.of(탈퇴유저));

        userService.restoreUser(1L, 탈퇴유저);

        assertThat(탈퇴유저.getActive()).isTrue();
    }

    @Test
    void 복구_실패_이미복구됨() {
        User 유저 = createUser(1L, true);
        when(userRepository.findById(1L)).thenReturn(Optional.of(유저));

        assertThatThrownBy(() -> userService.restoreUser(1L, 유저))
                .isInstanceOf(UserException.UserAlreadyRestoredException.class);
    }

    private User createUser(Long id, boolean active) {
        return User.builder()
                .id(id)
                .email("test@example.com")
                .password("pw")
                .nickname("tester")
                .address("서울")
                .role(Role.BUYER)
                .active(active)
                .build();
    }
}
