package hekireki.sanjijiksong.domain.item.service;

import hekireki.sanjijiksong.domain.item.dto.ItemCreateRequest;
import hekireki.sanjijiksong.domain.item.dto.ItemResponse;
import hekireki.sanjijiksong.domain.item.dto.ItemUpdateRequest;
import hekireki.sanjijiksong.domain.item.entity.Item;
import hekireki.sanjijiksong.domain.item.entity.ItemStatus;
import hekireki.sanjijiksong.domain.item.repository.ItemRepository;
import hekireki.sanjijiksong.domain.store.entity.Store;
import hekireki.sanjijiksong.domain.store.repository.StoreRepository;
import hekireki.sanjijiksong.domain.user.entity.User;
import hekireki.sanjijiksong.domain.user.repository.UserRepository;
import hekireki.sanjijiksong.global.common.exception.ErrorCode;
import hekireki.sanjijiksong.global.common.exception.ItemException;
import hekireki.sanjijiksong.global.common.exception.StoreException;
import hekireki.sanjijiksong.global.common.exception.UserException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final StoreRepository storeRepository;

    public ItemResponse createItem(Long storeId, ItemCreateRequest itemCreateRequest, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

        Store userStore = user.getStore();

        if(userStore == null){
            throw new StoreException(ErrorCode.STORE_NOT_FOUND);
        }

        if(!userStore.getId().equals(storeId)){
            throw new StoreException(ErrorCode.UNAUTHORIZED_STORE_OWNER);
        }

        Item savedItem = itemRepository.save(itemCreateRequest.toEntity(userStore));
        return ItemResponse.from(savedItem);
    }

    public List<ItemResponse> getMyItems(Long storeId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

        Store userStore = user.getStore();

        if(userStore == null){
            throw new StoreException(ErrorCode.STORE_NOT_FOUND);
        }

        if(!userStore.getId().equals(storeId)){
            throw new StoreException(ErrorCode.UNAUTHORIZED_STORE_OWNER);
        }

        List<Item> AllItem = itemRepository.findAllByStoreId(storeId);
        return AllItem.stream()
                .map(ItemResponse::from)
                .collect(Collectors.toList());
    }

    public ItemResponse getItemDetail(Long storeId, Long itemId) {
        Item item = itemRepository.findByStoreIdAndId(storeId, itemId)
                .orElseThrow(() -> new ItemException(ErrorCode.ITEM_NOT_FOUND));
        return ItemResponse.from(item);
    }

    @Transactional
    public ItemResponse updateItem(Long storeId, Long itemId, String email, ItemUpdateRequest itemUpdateRequest) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

        Store store = storeRepository.findByUserId(user.getId());
        if(!store.getId().equals(storeId)){
            throw new StoreException(ErrorCode.UNAUTHORIZED_STORE_OWNER);
        }

        Item item = itemRepository.findByStoreIdAndId(storeId, itemId)
                .orElseThrow(() -> new ItemException(ErrorCode.ITEM_NOT_FOUND));
        item.updateIfChanged(itemUpdateRequest);

        return ItemResponse.from(item);
    }

    @Transactional
    public void deactivateItem(Long storeId, Long itemId, String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserException(ErrorCode.USER_NOT_FOUND));

        Store store = storeRepository.findByUserId(user.getId());
        if(!store.getId().equals(storeId)){
            throw new StoreException(ErrorCode.UNAUTHORIZED_STORE_OWNER);
        }

        Item item = itemRepository.findByStoreIdAndId(storeId, itemId)
                .orElseThrow(() -> new ItemException(ErrorCode.ITEM_NOT_FOUND));
        item.deactivate();
    }

    public Page<ItemResponse> itemSearch(String keyword, Pageable pageable) {
        return itemRepository
                .findAllByNameContainingAndItemStatusAndActiveIsTrue(keyword, ItemStatus.ONSALE, pageable)
                .map(ItemResponse::from);
    }

    public Page<ItemResponse> categorySearch(String keyword, Pageable pageable) {
        return itemRepository
                .findAllByCategoryContainingAndItemStatusAndActiveIsTrue(keyword, ItemStatus.ONSALE, pageable)
                .map(ItemResponse::from);
    }
}
