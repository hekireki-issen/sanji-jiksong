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
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class StoreService {
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;


    @Transactional
    public StoreResponse create(StoreCreateRequest request, Long userId) {
        log.info("가게 등록 요청 - userId={}, name={}", userId, request.name());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new StoreException(ErrorCode.USER_NOT_FOUND)); //존재하지 않는 유저일 때 예외

        if (storeRepository.existsByUser(user)) {
            log.warn("등록 실패 - 이미 등록된 가게가 있음: userId={}", userId);
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
        log.info("가게 등록 완료 - storeId={}, userId={}", saved.getId(), userId);

        return new StoreResponse(
                saved.getId(),
                saved.getName(),
                saved.getAddress(),
                saved.getDescription(),
                saved.getImage(),
                saved.getActive()
        );
    }


    public StoreResponse getById(Long storeId) {
        Store store = storeRepository.findById(storeId)
                .filter(Store::getActive)
                .orElseThrow(() -> new StoreException(ErrorCode.STORE_NOT_FOUND)); //가게 찾을 수 없을 때 예외

        return StoreResponse.of(store);
    }


    @Transactional
    public void deactivate(Long storeId, Long userId) {
        log.info("가게 비활성화 요청 - storeId={}, userId={}", storeId, userId);
        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreException(ErrorCode.STORE_NOT_FOUND)); //가게 찾을 수 없을 때 예외

        if (!store.getUser().getId().equals(userId)) {
            log.warn("수정 실패 - 권한 없음 : userId={}", userId);
            throw new StoreException(ErrorCode.STORE_FORBIDDEN); //가게 권한 없을 때 예외
        }

        store.deactivate();
    }


    public Page<StoreResponse> getAllActiveStores(Pageable pageable) {
        return storeRepository.findByActiveTrue(pageable)
                .map(StoreResponse::of);
    }
  

    @Transactional
    public StoreResponse update(Long storeId, Long userId, StoreUpdateRequest request) {
        log.info("가게 수정 요청 - storeId={}, userId={}, 변경 내용: name={}, address={}, description={}, image={}",
                storeId, userId,
                request.name(), request.address(), request.description(), request.image());

        Store store = storeRepository.findById(storeId)
                .orElseThrow(() -> new StoreException(ErrorCode.STORE_NOT_FOUND));

        if (!store.getUser().getId().equals(userId)) {
            log.warn("가게 수정 실패 - 권한 없음: 요청자 userId={}, 실제 소유자 userId={}", userId, store.getUser().getId());
            throw new StoreException(ErrorCode.STORE_FORBIDDEN);
        }

        if (!store.getActive()) {
            log.warn("가게 수정 실패 - 비활성화된 가게: storeId={}", storeId);
            throw new StoreException(ErrorCode.STORE_ALREADY_DEACTIVATED);
        }

        store.update(
                request.name(),
                request.address(),
                request.description(),
                request.image()
        );
        log.info("가게 수정 성공 - storeId={}", storeId);
        return StoreResponse.of(store);
    }


    public List<StoreResponse> searchByKeyword(String keyword) {
        log.info("가게 검색 요청 - keyword={}", keyword);
        List<Store> matchedStores = storeRepository.findByNameContainingAndActiveTrue(keyword);
        return matchedStores.stream()
                .map(StoreResponse::of)
                .toList();
    }

}
