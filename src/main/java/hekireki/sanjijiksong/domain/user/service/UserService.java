package hekireki.sanjijiksong.domain.user.service;

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

//        if (!user.getId().equals(currentUser.getId())) {
//            throw new 본인만 탈퇴 가능
//        }

        user.deactivate(); // 내부에서 이미 탈퇴된 경우 예외 처리
    }

}
