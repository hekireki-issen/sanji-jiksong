package hekireki.sanjijiksong.domain.item.service;

import hekireki.sanjijiksong.domain.item.dto.ItemCreateRequest;
import hekireki.sanjijiksong.domain.item.dto.ItemResponse;
import hekireki.sanjijiksong.domain.item.entity.Item;
import hekireki.sanjijiksong.domain.item.repository.ItemRepository;
import hekireki.sanjijiksong.domain.store.entity.Store;
import hekireki.sanjijiksong.domain.user.entity.User;
import hekireki.sanjijiksong.domain.user.repository.UserRepository;
import hekireki.sanjijiksong.global.common.exception.ErrorCode;
import hekireki.sanjijiksong.global.common.exception.StoreException;
import hekireki.sanjijiksong.global.common.exception.UserException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

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
        return ItemResponse.of(savedItem);
    }
}
