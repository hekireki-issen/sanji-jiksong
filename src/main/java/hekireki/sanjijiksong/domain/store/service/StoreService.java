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
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StoreService {
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;

    //생성
    @Transactional
    public StoreResponse create(StoreCreateRequest request, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new StoreException(ErrorCode.USER_NOT_FOUND)); //존재하지 않는 유저일 때 예외

        if (storeRepository.existsByUser(user)) {
            throw new StoreException(ErrorCode.STORE_ALREADY_REGISTERED); //이미 등록된 가게가 있을 때 예외
        }

        Store store = Store.builder()
                .user(user)
                .name(request.name())
                .address(request.address())
                .description(request.description())
                .image(request.image())
                .active(true)
                .build();

        user.setStore(store);
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

    //가게 찾기
    public StoreResponse getById(Long storeId) {
        Store store = storeRepository.findById(storeId)
                .filter(Store::getActive)
                .orElseThrow(() -> new StoreException(ErrorCode.STORE_NOT_FOUND)); //가게 찾을 수 없을 때 예외

        return StoreResponse.of(store);
    }

    //비활성화
    @Transactional
    public void deactivate(Long storeId, Long userId) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreException(ErrorCode.STORE_NOT_FOUND)); //가게 찾을 수 없을 때 예외

        if (!store.getUser().getId().equals(userId)) {
            throw new StoreException(ErrorCode.STORE_FORBIDDEN); //가게 권한 없을 때 예외
        }

        store.deactivate(); //이미 비활성화 되어 있으면 예외 처리
    }

    //가게 조회
    public List<StoreResponse> getAllActiveStores() {
        return storeRepository.findAllByActiveTrue()
                .stream()
                .map(StoreResponse::of)
                .toList();
    }
  
  //가게 
    @Transactional
    public StoreResponse update(Long storeId, Long userId, StoreUpdateRequest request) {
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreException(ErrorCode.STORE_NOT_FOUND));

        if (!store.getUser().getId().equals(userId)) {
            throw new StoreException(ErrorCode.STORE_FORBIDDEN);
        }

        if (!store.getActive()) {
            throw new StoreException(ErrorCode.STORE_ALREADY_DEACTIVATED);
        }

        store.update(
                request.name(),
                request.address(),
                request.description(),
                request.image()
        );

        return StoreResponse.of(store);
    }

    //keyword 조회
    public List<StoreResponse> searchByKeyword(String keyword) {
        List<Store> matchedStores = storeRepository.findByNameContainingAndActiveTrue(keyword);
        return matchedStores.stream()
                .map(StoreResponse::of)
                .toList();
    }

}
