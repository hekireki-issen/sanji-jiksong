package hekireki.sanjijiksong.domain.user.service;

import hekireki.sanjijiksong.domain.user.dto.PasswordResetRequest;
import hekireki.sanjijiksong.domain.user.dto.UserRegisterRequest;
import hekireki.sanjijiksong.domain.user.dto.UserResponse;
import hekireki.sanjijiksong.domain.user.entity.User;
import hekireki.sanjijiksong.domain.user.repository.UserRepository;
import hekireki.sanjijiksong.global.common.exception.UserException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // 회원가입
    public UserResponse register(UserRegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
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

        return UserResponse.from(userRepository.save(user));
    }

    // 회원탈퇴
    public void deleteUser(Long userId, User currentUser) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserException.UserNotFoundException::new);

        if (!user.getId().equals(currentUser.getId())) {
            throw new UserException.UserUnauthorizedException();
        }

        user.deactivate(); // 내부에서 이미 탈퇴된 경우 예외 처리
    }

    // 회원복구
    public void restoreUser(Long userId, User currentUser) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserException.UserNotFoundException::new);

        if (!user.getId().equals(currentUser.getId())) {
            throw new UserException.UserUnauthorizedException();
        }

        user.restore(); // → 내부에서 복구 가능 여부 및 예외 처리까지 수행
    }

    // 비밀번호 재설정
    public void resetPassword(Long userId, PasswordResetRequest request, User currentUser) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserException.UserNotFoundException::new);

        if (!user.getId().equals(currentUser.getId())) {
            throw new UserException.UserUnauthorizedException(); // 본인 아님
        }

        if (!user.getActive()) {
            throw new UserException.UserAlreadyDeactivatedException(); // 탈퇴한 사용자
        }

        user.updatePassword(request.newPassword());
    }

    //admin method

    // 모든 유저 조회
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserResponse::from)
                .toList();
    }

    // 탈퇴 처리
    public void deactivateUserByAdmin(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserException.UserNotFoundException::new);

        if (!user.getActive()) {
            throw new UserException.UserAlreadyDeactivatedException();
        }

        user.deactivate();
    }

}
