package hekireki.sanjijiksong.domain.user.service;

import hekireki.sanjijiksong.domain.user.dto.PasswordResetRequest;
import hekireki.sanjijiksong.domain.user.dto.UserRegisterRequest;
import hekireki.sanjijiksong.domain.user.dto.UserResponse;
import hekireki.sanjijiksong.domain.user.entity.User;
import hekireki.sanjijiksong.domain.user.repository.UserRepository;
import hekireki.sanjijiksong.global.common.exception.UserException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public UserResponse register(UserRegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new UserException.UserEmailAlreadyExistsException();
        }

        User user = User.builder()
                .email(request.email())
                .password(request.password()) // 평문 비밀번호 (암호화 X)
                .nickname(request.nickname())
                .address(request.address())
                .role(request.role())
                .active(true)
                .build();

        return UserResponse.from(userRepository.save(user));
    }

    public void deleteUser(Long userId, User currentUser) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserException.UserNotFoundException::new);

        if (!user.getId().equals(currentUser.getId())) {
            throw new UserException.UserUnauthorizedException();
        }

        user.deactivate(); // 내부에서 이미 탈퇴된 경우 예외 처리
    }

    public void restoreUser(Long userId, User currentUser) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserException.UserNotFoundException::new);

        if (!user.getId().equals(currentUser.getId())) {
            throw new UserException.UserUnauthorizedException();
        }

        user.restore(); // → 내부에서 복구 가능 여부 및 예외 처리까지 수행
    }

    public void resetPassword(Long userId, PasswordResetRequest request, User currentUser) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserException.UserNotFoundException::new);

        if (!user.getId().equals(currentUser.getId())) {
            throw new UserException.UserUnauthorizedException(); // 본인 아님
        }

        if (!user.getActive()) {
            throw new UserException.UserAlreadyDeactivatedException(); // 탈퇴한 사용자
        }

        user.updatePassword(request.newPassword()); // setter 없이 처리
    }

}
