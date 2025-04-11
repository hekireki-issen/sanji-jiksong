package hekireki.sanjijiksong.domain.user.service;

import hekireki.sanjijiksong.domain.cart.entity.Cart;
import hekireki.sanjijiksong.domain.cart.repository.CartRepository;
import hekireki.sanjijiksong.domain.user.dto.PasswordResetRequest;
import hekireki.sanjijiksong.domain.user.dto.UserRegisterRequest;
import hekireki.sanjijiksong.domain.user.dto.UserResponse;
import hekireki.sanjijiksong.domain.user.entity.User;
import hekireki.sanjijiksong.domain.user.repository.UserRepository;
import hekireki.sanjijiksong.global.common.exception.UserException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final PasswordEncoder passwordEncoder;

    // 회원가입
    public UserResponse register(UserRegisterRequest request) {
        log.info("회원가입 요청 - email={}, nickname={}", request.email(), request.nickname());

        if (userRepository.existsByEmail(request.email())) {
            log.warn("회원가입 실패 - 중복 이메일: {}", request.email());
            throw new UserException.UserEmailAlreadyExistsException();
        }

        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password())) // 암호화
                .nickname(request.nickname())
                .address(request.address())
                .role(request.role())
                .active(true)
                .build();

        userRepository.save(user);
        cartRepository.save(new Cart(user));

        log.info("회원가입 성공 - userId={}, email={}", user.getId(), user.getEmail());
        return UserResponse.from(user);
    }

    // 회원탈퇴
    public void deleteUser(Long userId, User currentUser) {
        log.info("회원탈퇴 요청 - targetUserId={}, requesterUserId={}", userId, currentUser.getId());

        User user = userRepository.findById(userId)
                .orElseThrow(UserException.UserNotFoundException::new);

        if (!user.getId().equals(currentUser.getId())) {
            log.warn("회원탈퇴 실패 - 권한 없음: requesterUserId={}", currentUser.getId());
            throw new UserException.UserUnauthorizedException();
        }

        user.deactivate(); // 내부에서 이미 탈퇴된 경우 예외 처리
        log.info("회원탈퇴 성공 - userId={}", user.getId());
    }

    // 회원복구
    public void restoreUser(Long userId, User currentUser) {
        log.info("회원복구 요청 - targetUserId={}, requesterUserId={}", userId, currentUser.getId());

        User user = userRepository.findById(userId)
                .orElseThrow(UserException.UserNotFoundException::new);

        if (!user.getId().equals(currentUser.getId())) {
            log.warn("회원복구 실패 - 권한 없음: requesterUserId={}", currentUser.getId());
            throw new UserException.UserUnauthorizedException();
        }

        user.restore(); // → 내부에서 복구 가능 여부 및 예외 처리까지 수행
        log.info("회원복구 성공 - userId={}", user.getId());
    }

    // 비밀번호 재설정
    public void resetPassword(Long userId, PasswordResetRequest request, User currentUser) {
        log.info("비밀번호 재설정 요청 - targetUserId={}, requesterUserId={}", userId, currentUser.getId());

        User user = userRepository.findById(userId)
                .orElseThrow(UserException.UserNotFoundException::new);

        if (!user.getId().equals(currentUser.getId())) {
            log.warn("비밀번호 재설정 실패 - 권한 없음: requesterUserId={}", currentUser.getId());
            throw new UserException.UserUnauthorizedException(); // 본인 아님
        }

        if (!user.getActive()) {
            log.warn("비밀번호 재설정 실패 - 탈퇴한 사용자: userId={}", userId);
            throw new UserException.UserAlreadyDeactivatedException(); // 탈퇴한 사용자
        }

        user.updatePassword(request.newPassword());
        log.info("비밀번호 재설정 성공 - userId={}", user.getId());
    }

    //admin method

    // 모든 유저 조회
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        log.info("모든 사용자 조회 요청 - page={}, size={}", pageable.getPageNumber(), pageable.getPageSize());
        return userRepository.findAll(pageable)
                .map(UserResponse::from);
    }

    // 탈퇴 처리
    public void deactivateUserByAdmin(Long userId) {
        log.info("관리자에 의한 탈퇴 요청 - userId={}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(UserException.UserNotFoundException::new);

        if (!user.getActive()) {
            log.warn("관리자 탈퇴 실패 - 이미 비활성화된 사용자: userId={}", userId);
            throw new UserException.UserAlreadyDeactivatedException();
        }

        user.deactivate();
        log.info("관리자 탈퇴 성공 - userId={}", userId);
    }

}
