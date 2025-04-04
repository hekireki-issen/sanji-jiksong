package hekireki.sanjijiksong.global.security.service;

import hekireki.sanjijiksong.domain.user.entity.User;
import hekireki.sanjijiksong.global.security.dto.CustomUserDetails;
import hekireki.sanjijiksong.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    //Login seq : 2
    @Override//자동 호출
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User userData = userRepository.findByEmail(email);
        if (userData != null) {
            return new CustomUserDetails(userData);
        }
        return null;
    }
}
