package hekireki.sanjijiksong.domain.store.service;

import hekireki.sanjijiksong.domain.store.dto.StoreCreateRequest;
import hekireki.sanjijiksong.domain.store.dto.StoreResponse;
import hekireki.sanjijiksong.domain.store.entity.Store;
import hekireki.sanjijiksong.domain.store.repository.StoreRepository;
import hekireki.sanjijiksong.domain.user.entity.User;
import hekireki.sanjijiksong.domain.user.repository.UserRepository;
import hekireki.sanjijiksong.global.common.exception.ErrorCode;
import hekireki.sanjijiksong.global.common.exception.StoreException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StoreService {
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;

    @Transactional
    public StoreResponse create(StoreCreateRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new StoreException(ErrorCode.USER_NOT_FOUND)); //유저가 없으면 예외 발생

        if (storeRepository.existsByUser(user)) {
            throw new StoreException(ErrorCode.STORE_ALREADY_REGISTERED);
        }

        Store store = Store.builder()
                .user(user)
                .name(request.name())
                .address(request.address())
                .description(request.description())
                .image(request.image())
                .active(true)
                .build();
        Store saved = storeRepository.save(store);

        return new StoreResponse(
                saved.getId(),
                saved.getName(),
                saved.getAddress(),
                saved.getDescription(),
                saved.getImage(),
                saved.getActive()
        );
    }
}
